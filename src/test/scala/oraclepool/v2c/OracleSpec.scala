package oraclepool.v2c

import oraclepool.v2c.helpers.MockHelpers
import org.ergoplatform.appkit.{BlockchainContext, HttpClientTesting}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

// Tests oracle contract operations
//  1. Publish data-point
//  2. Transfer token
//  3. Redeem rewards

class OracleSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with HttpClientTesting with MockHelpers {
  val ergoClient = createMockedErgoClient(MockData(Nil, Nil))

  val dataValue = 123456L
  lazy val contracts = OraclePool.contracts
  import contracts._
  import config._

  ///////////////////////////
  //// Create data point ////
  ///////////////////////////
  property("Create data point v2") {
    ergoClient.execute { implicit ctx: BlockchainContext =>
      val oracleBox = bootstrapOracleBox(pubKey1, 10)
      // using correct secret and context var should work
      noException should be thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox, privKey1, 0, 1)

      // new box with different address should not work
      an[Exception] should be thrownBy createDataPoint(dataValue, dummyEpochCounter, poolAddress, minStorageRent, oracleBox, privKey1, 0, 1)

      // new box with less amount should not work
      an[Exception] should be thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent - 1, oracleBox, privKey1, 0, 1)

      // new box with more amount should work
      noException should be thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent + 1, oracleBox, privKey1, 0, 1)

      // new box with different reward token should work
      createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox, privKey1, 0, 1, customRewardTokenId = Some(junkTokenId)).getTokens
        .get(1)
        .getId
        .toString
        .toLowerCase shouldBe junkTokenId.toLowerCase

      // new box without any token at 2nd index should work
      noException should be thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox, privKey1, 0, 0)

      // adding reward token to one without reward tokens should work
      noException should be thrownBy createDataPoint(
        dataValue,
        dummyEpochCounter,
        oracleAddress,
        minStorageRent,
        createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox, privKey1, 0, 0),
        privKey1,
        0,
        1
      )

      // using wrong context var should not work
      an[Exception] should be thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox, privKey1, 1, 1)

      // using wrong key should not work
      the[AssertionError] thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox, privKey2, 0, 1) should have message "assertion failed: Tree root should be real but was UnprovenSchnorr(ProveDlog((1ae6ece10590e79fb74f8bdd1305e4ce479aad52b277751cccbf92d4c5bba2bf,653d3f029fa96df3b6ffb64c88eabe22d078f060661ce5c7b30e54ca5120818,1)),None,None,None,true,0)"

      // created box should again be spendable
      noException should be thrownBy createDataPoint(
        dataValue,
        dummyEpochCounter,
        oracleAddress,
        minStorageRent,
        createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox, privKey1, 0, 1),
        privKey1,
        0,
        1
      )
    }
  }

  /////////////////////////////
  ////// Transfer token ///////
  /////////////////////////////
  property("Transfer Oracle Token v2") {
    ergoClient.execute { implicit ctx: BlockchainContext =>
      val oracleBox1 = bootstrapOracleBox(pubKey1, 10)

      // transfer to other pubKey2 should work
      noException should be thrownBy transferOracleBox(oracleBox1, privKey1, Some(pubKey2), 1)

      // new box without reward tokens should work
      noException should be thrownBy transferOracleBox(oracleBox1, privKey1, Some(pubKey2), 0)

      // new box without r4 should not work
      the[Exception] thrownBy transferOracleBox(oracleBox1, privKey1, None, 1) should have message "Script reduced to false"

      // oracle2 should be able to create data point after transfer from oracle 1
      noException should be thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, transferOracleBox(oracleBox1, privKey1, Some(pubKey2), 1), privKey2, 0, 1)

      // oracle2 should be able to transfer back to oracle 1 after transfer from oracle 1
      noException should be thrownBy transferOracleBox(transferOracleBox(oracleBox1, privKey1, Some(pubKey2), 1), privKey2, Some(pubKey1), 1)

      // oracle1 should be able to transfer back to oracle 2 after transfer back to oracle 1 after transfer from oracle 1
      noException should be thrownBy createDataPoint(
        dataValue,
        dummyEpochCounter,
        oracleAddress,
        minStorageRent,
        transferOracleBox(transferOracleBox(oracleBox1, privKey1, Some(pubKey2), 1), privKey2, Some(pubKey1), 1),
        privKey1,
        0,
        1
      )

      // privKey1 should NOT be able to create data point after transfer to oracle 2
      an[AssertionError] should be thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, transferOracleBox(oracleBox1, privKey1, Some(pubKey2), 1), privKey1, 0, 1)

      // create data point and transfer in one step
      noException should be thrownBy createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox1, privKey1, 0, 1, Some(pubKey2))

      // spend (by creating data point) after create and transfer in one step
      noException should be thrownBy createDataPoint(
        dataValue,
        dummyEpochCounter,
        oracleAddress,
        minStorageRent,
        createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox1, privKey1, 0, 1, Some(pubKey2)),
        privKey2,
        0,
        1
      )

      // spend (by transferring) after create and transfer in one step
      noException should be thrownBy transferOracleBox(createDataPoint(dataValue, dummyEpochCounter, oracleAddress, minStorageRent, oracleBox1, privKey1, 0, 1, Some(pubKey2)),
                                                       privKey2,
                                                       Some(pubKey1),
                                                       1)
    }
  }
}
