package oraclepool.v2

import kiosk.ergo._
import kiosk.tx.TxUtil
import oraclepool.v2.helpers.MockHelpers
import org.ergoplatform.appkit._
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class UpdateSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with HttpClientTesting with MockHelpers {
  /*
  In Oracle Pool v2, the poolBox participates two spending transactions:
    1. refreshPoolBox
    2. updatePoolBox

  Here we will test the updatePoolBox
   */
  lazy val contracts = OraclePool.contracts
  import contracts._
  import config._

  require(contracts.poolAddress != newPool.poolAddress)
  require(contracts.refreshAddress != newPool.refreshAddress)
  require(contracts.oracleAddress == newPool.oracleAddress)
  require(contracts.updateAddress != newPool.updateAddress)
  require(contracts.ballotAddress != newPool.ballotAddress)

  require(config.refreshNFT != newConfig.refreshNFT)
  require(config.oracleTokenId != newConfig.oracleTokenId)
  require(config.updateNFT != newConfig.updateNFT)
  require(config.ballotTokenId != newConfig.ballotTokenId)

  require(config.poolNFT == newConfig.poolNFT)

  property("Update from fresh pool box") {
    createMockedErgoClient(MockData(Nil, Nil)).execute { implicit ctx: BlockchainContext =>
      val ballotBox1 = bootstrapBallotBox(pubKey1)
      val ballotBox2 = bootstrapBallotBox(pubKey2)
      val ballotBox3 = bootstrapBallotBox(pubKey3)
      val ballotBox4 = bootstrapBallotBox(pubKey4)
      val ballotBox5 = bootstrapBallotBox(pubKey5)
      val ballotBox6 = bootstrapBallotBox(pubKey6)

      val votedBox1 = voteForUpdate(newPool.poolErgoTree.bytes, 1, contracts.ballotAddress, config.minStorageRent, ballotBox1, privKey1, 0)
      val votedBox2 = voteForUpdate(newPool.poolErgoTree.bytes, 1, contracts.ballotAddress, config.minStorageRent, ballotBox2, privKey2, 0)
      val votedBox3 = voteForUpdate(newPool.poolErgoTree.bytes, 1, contracts.ballotAddress, config.minStorageRent, ballotBox3, privKey3, 0)
      val votedBox4 = voteForUpdate(newPool.poolErgoTree.bytes, 1, contracts.ballotAddress, config.minStorageRent, ballotBox4, privKey4, 0)
      val votedBox5 = voteForUpdate(newPool.poolErgoTree.bytes, 1, contracts.ballotAddress, config.minStorageRent, ballotBox5, privKey5, 0)
      val votedBox6 = voteForUpdate(newPool.poolErgoTree.bytes, 1, contracts.ballotAddress, config.minStorageRent, ballotBox6, privKey6, 0)

      val freshPoolBox = bootstrapPoolBox(0, 1, 1)

      val oracleBox1 = bootstrapOracleBox(pubKey1, 10)
      val oracleBox2 = bootstrapOracleBox(pubKey2, 20)
      val oracleBox3 = bootstrapOracleBox(pubKey3, 30)
      val oracleBox4 = bootstrapOracleBox(pubKey4, 40)
      val oracleBox5 = bootstrapOracleBox(pubKey5, 50)

      val dataPoint1 = createDataPoint(1, 0, contracts.oracleAddress, config.minStorageRent, oracleBox1, privKey1, 0, 10)
      val dataPoint2 = createDataPoint(1, 0, contracts.oracleAddress, config.minStorageRent, oracleBox2, privKey2, 0, 20)
      val dataPoint3 = createDataPoint(1, 0, contracts.oracleAddress, config.minStorageRent, oracleBox3, privKey3, 0, 30)
      val dataPoint4 = createDataPoint(1, 0, contracts.oracleAddress, config.minStorageRent, oracleBox4, privKey4, 0, 40)
      val dataPoint5 = createDataPoint(1, 0, contracts.oracleAddress, config.minStorageRent, oracleBox5, privKey5, 0, 50)

      val refreshedPoolBox: InputBox = TxUtil
        .createTx(
          Array(
            bootstrapPoolBox(0, 1, 0),
            bootstrapRefreshBox(1000000L).withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
            dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
            dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
            dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
            dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
            dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(contracts.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(contracts.refreshAddress, config.minStorageRent, Array.empty, Array((config.refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
            KioskBox(contracts.oracleAddress, config.minStorageRent, Array(pubKey1), Array((config.oracleTokenId, 1), (rewardTokenId, 16))),
            KioskBox(contracts.oracleAddress, config.minStorageRent, Array(pubKey2), Array((config.oracleTokenId, 1), (rewardTokenId, 21))),
            KioskBox(contracts.oracleAddress, config.minStorageRent, Array(pubKey3), Array((config.oracleTokenId, 1), (rewardTokenId, 31))),
            KioskBox(contracts.oracleAddress, config.minStorageRent, Array(pubKey4), Array((config.oracleTokenId, 1), (rewardTokenId, 41))),
            KioskBox(contracts.oracleAddress, config.minStorageRent, Array(pubKey5), Array((config.oracleTokenId, 1), (rewardTokenId, 51)))
          ),
          fee = 1500000,
          changeAddress,
          Array[String](privKey1.toString),
          Array.empty,
          false
        )
        .getOutputsToSpend
        .get(0)

      // update fresh pool box
      usingPoolBox(freshPoolBox)

      // update refreshed pool box
      usingPoolBox(refreshedPoolBox)

      def usingPoolBox(poolBox: InputBox) = {
        // proper update should work
        noException should be thrownBy
          TxUtil.createTx(
            Array(
              poolBox,
              bootstrapUpdateBox(Some(1)),
              votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
              votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
              votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
              votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
              votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
              votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
              dummyFundingBox
            ),
            Array.empty,
            Array(
              KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
              KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
            ),
            1500000,
            changeAddress,
            Array.empty,
            Array.empty,
            false
          )

        // should fail when wrong creation height in ballot boxes
        the[Exception] thrownBy
          TxUtil.createTx(
            Array(
              poolBox,
              bootstrapUpdateBox(Some(0)), // creation height is actually 0, while the one in ballots is 1
              votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
              votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
              votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
              votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
              votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
              votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
              dummyFundingBox
            ),
            Array.empty,
            Array(
              KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
              KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
            ),
            1500000,
            changeAddress,
            Array.empty,
            Array.empty,
            false
          ) should have message "Script reduced to false"

        // should fail when pool box does not have correct address
        the[Exception] thrownBy
          TxUtil.createTx(
            Array(
              poolBox,
              bootstrapUpdateBox(Some(1)),
              votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
              votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
              votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
              votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
              votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
              votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
              dummyFundingBox
            ),
            Array.empty,
            Array(
              KioskBox(junkAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
              KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
            ),
            1500000,
            changeAddress,
            Array.empty,
            Array.empty,
            false
          ) should have message "Script reduced to false"

        // Should fail when having insufficient votes (5)
        the[Exception] thrownBy
          TxUtil.createTx(
            Array(
              poolBox,
              bootstrapUpdateBox(Some(1)),
              votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
              votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
              votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
              votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
              votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
              dummyFundingBox
            ),
            Array.empty,
            Array(
              KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
              KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1)))
            ),
            1500000,
            changeAddress,
            Array.empty,
            Array.empty,
            false
          ) should have message "Script reduced to false"

        // Should fail when invalid context variable for a ballot box
        an[AssertionError] should be thrownBy
          TxUtil.createTx(
            Array(
              poolBox,
              bootstrapUpdateBox(Some(1)),
              votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
              votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
              votedBox3.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // wrong context variable (2 instead of 4)
              votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
              votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
              votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
              dummyFundingBox
            ),
            Array.empty,
            Array(
              KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
              KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
              KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
            ),
            1500000,
            changeAddress,
            Array.empty,
            Array.empty,
            false
          )

        // should fail when ballot box does not have same group element
        an[Exception] should be thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array.empty, Array((config.ballotTokenId, 1))), // does not have group element
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        )

        // should fail when ballot box has same other type in R4
        an[Exception] should be thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(KioskCollByte("someOtherType".getBytes())), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        )

        // should fail when ballot box does not have correct address
        the[Exception] thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(junkAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        ) should have message "Script reduced to false"

        // should fail when update box does not have correct address
        the[Exception] thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(junkAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        ) should have message "Script reduced to false"

        // should fail when update box does not have correct tokens
        the[Exception] thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((junkTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        ) should have message "Script reduced to false"

        // should fail when ballot box does not have correct tokens
        the[Exception] thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((junkTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        ) should have message "Script reduced to false"

        // should fail when ballot box has junk registers
        an[Exception] should be thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3, KioskCollByte("junk".getBytes)), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        )

        // should fail when pool box does not have correct registers
        the[Exception] thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(2), KioskInt(1)), Array((config.poolNFT, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        ) should have message "Script reduced to false"

        // should fail when pool box has junk registers
        an[Exception] should be thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1), KioskCollByte("junk".getBytes)), Array((config.poolNFT, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        )

        // should fail when pool box does not have correct tokens
        the[Exception] thrownBy TxUtil.createTx(
          Array(
            poolBox,
            bootstrapUpdateBox(Some(1)),
            votedBox1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to ballot1,
            votedBox2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to ballot2,
            votedBox3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to ballot3,
            votedBox4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to ballot4,
            votedBox5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to ballot5,
            votedBox6.withContextVars(new ContextVar(0, KioskInt(7).getErgoValue)), // output index 7 corresponds to ballot6,
            dummyFundingBox
          ),
          Array.empty,
          Array(
            KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((junkTokenId, 1))),
            KioskBox(contracts.updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
            KioskBox(contracts.ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
          ),
          1500000,
          changeAddress,
          Array.empty,
          Array.empty,
          false
        ) should have message "Script reduced to false"
      }
    }
  }
}
