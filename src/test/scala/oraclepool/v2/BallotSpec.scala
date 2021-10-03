package oraclepool.v2

import oraclepool.v2.OraclePool.pool._
import oraclepool.v2.OraclePool.pool.config._
import oraclepool.v2.helpers.MockHelpers
import org.ergoplatform.appkit.{BlockchainContext, HttpClientTesting}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

// Tests ballot contract operations
//  1. Vote for update
//  2. Transfer token

class BallotSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with HttpClientTesting with MockHelpers {
  val ergoClient = createMockedErgoClient(MockData(Nil, Nil))

  val dataValue = 123456L

  ///////////////////////////
  //// Vote for update ////
  ///////////////////////////
  property("Vote for update v2") {
    ergoClient.execute { implicit ctx: BlockchainContext =>
      val ballotBox = bootstrapBallotBox(pubKey1)
      // using correct secret and context var should work
      noException should be thrownBy voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, ballotBox, privKey1, 0)

      // new box with different address should not work
      an[Exception] should be thrownBy voteForUpdate("dummyValue".getBytes, 1, poolAddress, minStorageRent, ballotBox, privKey1, 0)

      // new box with less amount should not work
      an[Exception] should be thrownBy voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent - 1, ballotBox, privKey1, 0)

      // new box with more amount should work
      noException should be thrownBy voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent + 1, ballotBox, privKey1, 0)

      // using wrong context var should not work
      an[Exception] should be thrownBy voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, ballotBox, privKey1, 1)

      // using wrong key should not work
      the[AssertionError] thrownBy voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, ballotBox, privKey2, 0) should have message "assertion failed: Tree root should be real but was UnprovenSchnorr(ProveDlog((1ae6ece10590e79fb74f8bdd1305e4ce479aad52b277751cccbf92d4c5bba2bf,653d3f029fa96df3b6ffb64c88eabe22d078f060661ce5c7b30e54ca5120818,1)),None,None,None,true,0)"

      // created box should again be spendable
      noException should be thrownBy voteForUpdate(
        "dummyValue".getBytes,
        1,
        ballotAddress,
        minStorageRent,
        voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, ballotBox, privKey1, 0),
        privKey1,
        0
      )
    }
  }

  /////////////////////////////
  ////// Transfer token ///////
  /////////////////////////////
  property("Transfer Ballot Token v2") {
    ergoClient.execute { implicit ctx: BlockchainContext =>
      val ballotBox1 = bootstrapBallotBox(pubKey1)

      // transfer to other pubKey2 should work
      noException should be thrownBy transferBallotBox(ballotBox1, privKey1, Some(pubKey2))

      // new box without r4 should not work
      the[Exception] thrownBy transferBallotBox(ballotBox1, privKey1, None) should have message "Script reduced to false"

      // ballot2 should be able to create data point after transfer from ballot 1
      noException should be thrownBy voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, transferBallotBox(ballotBox1, privKey1, Some(pubKey2)), privKey2, 0)

      // ballot2 should be able to transfer back to ballot 1 after transfer from ballot 1
      noException should be thrownBy transferBallotBox(transferBallotBox(ballotBox1, privKey1, Some(pubKey2)), privKey2, Some(pubKey1))

      // ballot1 should be able to transfer back to ballot 2 after transfer back to ballot 1 after transfer from ballot 1
      noException should be thrownBy voteForUpdate(
        "dummyValue".getBytes,
        1,
        ballotAddress,
        minStorageRent,
        transferBallotBox(transferBallotBox(ballotBox1, privKey1, Some(pubKey2)), privKey2, Some(pubKey1)),
        privKey1,
        0
      )

      // privKey1 should NOT be able to vote for update after transfer to ballot 2
      an[AssertionError] should be thrownBy voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, transferBallotBox(ballotBox1, privKey1, Some(pubKey2)), privKey1, 0)

      // create data point and transfer in one step
      noException should be thrownBy voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, ballotBox1, privKey1, 0, Some(pubKey2))

      // spend (by creating data point) after create and transfer in one step
      noException should be thrownBy voteForUpdate(
        "dummyValue".getBytes,
        1,
        ballotAddress,
        minStorageRent,
        voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, ballotBox1, privKey1, 0, Some(pubKey2)),
        privKey2,
        0
      )

      // spend (by transferring) after create and transfer in one step
      noException should be thrownBy transferBallotBox(voteForUpdate("dummyValue".getBytes, 1, ballotAddress, minStorageRent, ballotBox1, privKey1, 0, Some(pubKey2)), privKey2, Some(pubKey1))
    }
  }
}
