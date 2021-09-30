package oraclepool.v2

import kiosk.ergo.{KioskBox, KioskCollByte, KioskGroupElement, KioskInt, KioskLong}
import kiosk.tx.TxUtil
import oraclepool.v2.helpers.MockHelpers
import org.ergoplatform.appkit.{BlockchainContext, ContextVar, HttpClientTesting}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import oraclepool.v2.OraclePool.pool._
import oraclepool.v2.OraclePool.pool.config._

class RefreshSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with HttpClientTesting with MockHelpers {
  val pubKey1: KioskGroupElement = keyPairs(0)._1
  val pubKey2: KioskGroupElement = keyPairs(1)._1
  val pubKey3: KioskGroupElement = keyPairs(2)._1
  val pubKey4: KioskGroupElement = keyPairs(3)._1
  val pubKey5: KioskGroupElement = keyPairs(4)._1

  val privKey1: BigInt = keyPairs(0)._2
  val privKey2: BigInt = keyPairs(1)._2
  val privKey3: BigInt = keyPairs(2)._2
  val privKey4: BigInt = keyPairs(3)._2
  val privKey5: BigInt = keyPairs(4)._2

  property("Refresh pool box v2") {
    createMockedErgoClient(MockData(Nil, Nil)).execute { implicit ctx: BlockchainContext =>
      val refreshBox = bootstrapRefreshBox(1000000L)
      val poolBox = bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1)
      val poolBoxNotExpired = bootstrapPoolBox(ctx.getHeight - epochLength + 1, 1)

      val oracleBox1 = bootstrapOracleBox(pubKey1, 10)
      val oracleBox2 = bootstrapOracleBox(pubKey2, 20)
      val oracleBox3 = bootstrapOracleBox(pubKey3, 30)
      val oracleBox4 = bootstrapOracleBox(pubKey4, 40)
      val oracleBox5 = bootstrapOracleBox(pubKey5, 50)

      val dataPoint1 = createDataPoint(1000, 0, oracleAddress, minStorageRent, oracleBox1, privKey1, 0, 10)
      val dataPoint2 = createDataPoint(1001, 0, oracleAddress, minStorageRent, oracleBox2, privKey2, 0, 20)
      val dataPoint3 = createDataPoint(1002, 0, oracleAddress, minStorageRent, oracleBox3, privKey3, 0, 30)
      val dataPoint4 = createDataPoint(1003, 0, oracleAddress, minStorageRent, oracleBox4, privKey4, 0, 40)
      val dataPoint5 = createDataPoint(1004, 0, oracleAddress, minStorageRent, oracleBox5, privKey5, 0, 50)

      // proper collection #1 (pubKey1 collects)
      noException should be thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // cannot create pool box with different address
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(junkAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // cannot create pool box with different token
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(junkAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((junkTokenId, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // cannot create pool box with wrong counter
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(junkAddress, minStorageRent, Array(KioskLong(1002), KioskInt(0)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // proper collection #2 (pubKey3 collects)
      noException should be thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // 3rd dataPoint box (dataPoint3) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // ...
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)),
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)),
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)),
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 11))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 36))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey3.toString),
        Array.empty,
        false
      )

      // proper collection #2 (pubKey3 collects) with different ordering of output should work
      noException should be thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // 3rd dataPoint box (dataPoint3) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // ...
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)),
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)),
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)),
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 36))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 11))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey3.toString),
        Array.empty,
        false
      )

      // wrong ordering of data points should fail
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // 3rd dataPoint box (dataPoint3) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // ...
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)),
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)),
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)),
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 36))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 11))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey3.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // wrong context variable for refresh box should fail
      the[AssertionError] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(1).getErgoValue)), // 1st dataPoint box (dataPoint3) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // ...
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)),
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)),
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)),
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 36))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 11))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey3.toString),
        Array.empty,
        false
      ) should have message "assertion failed: Tree root should be real but was UnprovenSchnorr(ProveDlog((537ccf0c41124008961b06c1a39d5ca4cdc4a2568587bbd0d333292d7a6a2f69,f6127dac06c156a14413e4e9c6469bd01f2b972aca62bcdce241597d411bd0d6,1)),None,None,None,true,0)"

      // spender can remove and take less tokens
      noException should be thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // 3rd dataPoint box (dataPoint3) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // ...
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)),
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)),
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)),
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 5))), // removed only 5
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))), // took only 1
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 11))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey3.toString),
        Array.empty,
        false
      )

      // spender can remove correct tokens but take less (thereby burning them or storing them elsewhere)
      noException should be thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // 3rd dataPoint box (dataPoint3) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // ...
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)),
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)),
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)),
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))), // removed 10
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))), // took only 1
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 11))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey3.toString),
        Array.empty,
        false
      )

      // spender cannot take more tokens than allowed
      an[Exception] should be thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // 3rd dataPoint box (dataPoint3) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // ...
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)),
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)),
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)),
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 11))), // removed 11, allowed 10
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 11))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey3.toString),
        Array.empty,
        false
      )

      // wrong context variable for oracle box should fail
      an[AssertionError] should be thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // wrong context variable
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // wrong context variable
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // not incrementing oracle box tokens should fail
      an[AssertionError] should be thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 20))), // didn't increment token
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // wrong average value should fail
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1003), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // non-expired pool box should fail
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength + 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // expired data points should fail
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          createDataPoint(1000, 0, oracleAddress, minStorageRent, oracleBox1, privKey1, 0, 10, customCreationHeight = Some(ctx.getHeight - epochLength - 1))
            .withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          createDataPoint(1001, 0, oracleAddress, minStorageRent, oracleBox2, privKey2, 0, 20, customCreationHeight = Some(ctx.getHeight - epochLength - 1))
            .withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // data points with wrong epoch counter should fail
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          createDataPoint(1000, 1, oracleAddress, minStorageRent, oracleBox1, privKey1, 0, 10).withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          createDataPoint(1001, 1, oracleAddress, minStorageRent, oracleBox2, privKey2, 0, 20).withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // less number of data points should fail
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          poolBox,
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1001), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 6))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 14))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // wrong token id in pool box should fail
      the[Exception] thrownBy {
        val newPoolNFT = "5367566B59703373367639792442264529482B4D6251655468576D5A71347437"
        TxUtil.createTx(
          Array(
            bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1, newPoolNFT = Some(newPoolNFT)),
            refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
            dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
            dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
            dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
            dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
            dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((newPoolNFT, 1))),
            KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
          ),
          fee = 1500000,
          changeAddress,
          Array[String](privKey1.toString),
          Array.empty,
          false
        )
      } should have message "Script reduced to false"

      // decimal average correctly handled
      noException should be thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong((1000 + 1001 + 1002 + 1003) / 4), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 8))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 15))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // cannot create new pool box with less creation height
      the[Exception] thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong((1000 + 1001 + 1002 + 1003) / 4), KioskInt(1)), Array((poolNFT, 1)), creationHeight = Some(ctx.getHeight - buffer - 1)),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 8))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 15))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      ) should have message "Script reduced to false"

      // cannot add junk registers to pool box
      an[Exception] should be thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong((1000 + 1001 + 1002 + 1003) / 4), KioskInt(1), KioskCollByte("junk".getBytes())), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 8))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 15))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // cannot add junk registers to oracle box
      an[Exception] should be thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong((1000 + 1001 + 1002 + 1003) / 4), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 8))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 15))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4, KioskInt(1)), Array((oracleTokenId, 1), (rewardTokenId, 41))),
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // cannot add junk tokens to oracle box
      an[Exception] should be thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong((1000 + 1001 + 1002 + 1003) / 4), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 8))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 15))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41), (junkTokenId, 1))),
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // cannot add junk tokens to pool box
      an[Exception] should be thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong((1000 + 1001 + 1002 + 1003) / 4), KioskInt(1)), Array((poolNFT, 1), (junkTokenId, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 8))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 15))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // cannot add junk tokens to refresh box
      an[Exception] should be thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong((1000 + 1001 + 1002 + 1003) / 4), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 8), (junkTokenId, 1))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 15))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // cannot add junk registers to refresh box
      an[Exception] should be thrownBy TxUtil.createTx(
        Array(
          bootstrapPoolBox(ctx.getHeight - epochLength - 1, 1),
          refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong((1000 + 1001 + 1002 + 1003) / 4), KioskInt(1)), Array((poolNFT, 1))),
          KioskBox(refreshAddress, minStorageRent, Array(KioskCollByte("junk".getBytes)), Array((refreshNFT, 1), (rewardTokenId, 1000000L - 8))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 15))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )

      // delay due to congestion should fail
      the[Exception] thrownBy {
        val lastEpochHeight = 0 // lowest possible, to ensure epoch is over
        val dataPointHeight = ctx.getHeight - epochLength + 1
        val thisEpochStart = ctx.getHeight - buffer - 1 // due to congestion, more than buffer blocks have been mined before this tx was confirmed
        TxUtil.createTx(
          Array(
            bootstrapPoolBox(lastEpochHeight, 1),
            refreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
            createDataPoint(1000, 0, oracleAddress, minStorageRent, oracleBox1, privKey1, 0, 10, customCreationHeight = Some(dataPointHeight))
              .withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
            createDataPoint(1001, 0, oracleAddress, minStorageRent, oracleBox2, privKey2, 0, 20, customCreationHeight = Some(dataPointHeight))
              .withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
            createDataPoint(1002, 0, oracleAddress, minStorageRent, oracleBox3, privKey3, 0, 30, customCreationHeight = Some(dataPointHeight))
              .withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
            createDataPoint(1003, 0, oracleAddress, minStorageRent, oracleBox4, privKey4, 0, 40, customCreationHeight = Some(dataPointHeight))
              .withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
            createDataPoint(1004, 0, oracleAddress, minStorageRent, oracleBox5, privKey5, 0, 50, customCreationHeight = Some(dataPointHeight))
              .withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(poolAddress, minStorageRent, Array(KioskLong(1002), KioskInt(1)), Array((poolNFT, 1)), creationHeight = Some(thisEpochStart)),
            KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
            KioskBox(oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
          ),
          fee = 1500000,
          changeAddress,
          Array[String](privKey1.toString),
          Array.empty,
          false
        )
      } should have message "Script reduced to false"

    }
  }

}
