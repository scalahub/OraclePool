package oraclepool.v2

import kiosk.encoding.ScalaErgoConverters.{getAddressFromErgoTree, getStringFromAddress}
import kiosk.ergo._
import kiosk.tx.TxUtil
import oraclepool.v2.helpers.MockHelpers
import org.ergoplatform.appkit._
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sigmastate.Values

class UpdateSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with HttpClientTesting with MockHelpers {
  /*
  In Oracle Pool v2, the poolBox participates two spending transactions:
    1. refreshPoolBox
    2. updatePoolBox

  Here we will test the updatePoolBox
   */
  lazy val contracts = OraclePool.contracts
  import contracts.config

  def vote(newPoolErgoTree: Values.ErgoTree, ballotAddress: String, ballotTokenId: String)(implicit ctx: BlockchainContext) = {
    val ballotBox1 = bootstrapBallotBox(pubKey1, Some(ballotAddress), Some(ballotTokenId))
    val ballotBox2 = bootstrapBallotBox(pubKey2, Some(ballotAddress), Some(ballotTokenId))
    val ballotBox3 = bootstrapBallotBox(pubKey3, Some(ballotAddress), Some(ballotTokenId))
    val ballotBox4 = bootstrapBallotBox(pubKey4, Some(ballotAddress), Some(ballotTokenId))
    val ballotBox5 = bootstrapBallotBox(pubKey5, Some(ballotAddress), Some(ballotTokenId))
    val ballotBox6 = bootstrapBallotBox(pubKey6, Some(ballotAddress), Some(ballotTokenId))

    val votedBox1 = voteForUpdate(newPoolErgoTree.bytes, 1, ballotAddress, minStorageRent, ballotBox1, privKey1, 0, None, Some(ballotTokenId))
    val votedBox2 = voteForUpdate(newPoolErgoTree.bytes, 1, ballotAddress, minStorageRent, ballotBox2, privKey2, 0, None, Some(ballotTokenId))
    val votedBox3 = voteForUpdate(newPoolErgoTree.bytes, 1, ballotAddress, minStorageRent, ballotBox3, privKey3, 0, None, Some(ballotTokenId))
    val votedBox4 = voteForUpdate(newPoolErgoTree.bytes, 1, ballotAddress, minStorageRent, ballotBox4, privKey4, 0, None, Some(ballotTokenId))
    val votedBox5 = voteForUpdate(newPoolErgoTree.bytes, 1, ballotAddress, minStorageRent, ballotBox5, privKey5, 0, None, Some(ballotTokenId))
    val votedBox6 = voteForUpdate(newPoolErgoTree.bytes, 1, ballotAddress, minStorageRent, ballotBox6, privKey6, 0, None, Some(ballotTokenId))

    Array(votedBox1, votedBox2, votedBox3, votedBox4, votedBox5, votedBox6)
  }

  def refresh(oldPoolBox: InputBox, oldRefreshBox: InputBox, rewardTokenId: String, oracleTokenId: String, refreshNFT: String)(implicit ctx: BlockchainContext) = {
    val oracleBox1 = bootstrapOracleBox(pubKey1, 10, Some(rewardTokenId), optOracleTokenId = Some(oracleTokenId))
    val oracleBox2 = bootstrapOracleBox(pubKey2, 20, Some(rewardTokenId), optOracleTokenId = Some(oracleTokenId))
    val oracleBox3 = bootstrapOracleBox(pubKey3, 30, Some(rewardTokenId), optOracleTokenId = Some(oracleTokenId))
    val oracleBox4 = bootstrapOracleBox(pubKey4, 40, Some(rewardTokenId), optOracleTokenId = Some(oracleTokenId))
    val oracleBox5 = bootstrapOracleBox(pubKey5, 50, Some(rewardTokenId), optOracleTokenId = Some(oracleTokenId))

    val dataPoint1 = createDataPoint(1, 0, contracts.oracleAddress, minStorageRent, oracleBox1, privKey1, 0, 10, customRewardTokenId = Some(rewardTokenId), customOracleTokenId = Some(oracleTokenId))
    val dataPoint2 = createDataPoint(1, 0, contracts.oracleAddress, minStorageRent, oracleBox2, privKey2, 0, 20, customRewardTokenId = Some(rewardTokenId), customOracleTokenId = Some(oracleTokenId))
    val dataPoint3 = createDataPoint(1, 0, contracts.oracleAddress, minStorageRent, oracleBox3, privKey3, 0, 30, customRewardTokenId = Some(rewardTokenId), customOracleTokenId = Some(oracleTokenId))
    val dataPoint4 = createDataPoint(1, 0, contracts.oracleAddress, minStorageRent, oracleBox4, privKey4, 0, 40, customRewardTokenId = Some(rewardTokenId), customOracleTokenId = Some(oracleTokenId))
    val dataPoint5 = createDataPoint(1, 0, contracts.oracleAddress, minStorageRent, oracleBox5, privKey5, 0, 50, customRewardTokenId = Some(rewardTokenId), customOracleTokenId = Some(oracleTokenId))

    val poolAddress = getStringFromAddress(getAddressFromErgoTree(oldPoolBox.getErgoTree))
    val refreshAddress = getStringFromAddress(getAddressFromErgoTree(oldRefreshBox.getErgoTree))

    val r5: Int = oldPoolBox.getRegisters.get(1).asInstanceOf[ErgoValue[Int]].getValue
    val oldHeight = oldPoolBox.getCreationHeight
    require(oldHeight < ctx.getHeight - config.epochLength)
    val newHeight = ctx.getHeight
    TxUtil
      .createTx(
        Array(
          oldPoolBox,
          oldRefreshBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), // 1st dataPoint box (dataPoint1) is spender
          dataPoint1.withContextVars(new ContextVar(0, KioskInt(2).getErgoValue)), // output index 2 corresponds to dataPoint1
          dataPoint2.withContextVars(new ContextVar(0, KioskInt(3).getErgoValue)), // output index 3 corresponds to dataPoint2
          dataPoint3.withContextVars(new ContextVar(0, KioskInt(4).getErgoValue)), // output index 4 corresponds to dataPoint3
          dataPoint4.withContextVars(new ContextVar(0, KioskInt(5).getErgoValue)), // output index 5 corresponds to dataPoint4
          dataPoint5.withContextVars(new ContextVar(0, KioskInt(6).getErgoValue)), // output index 6 corresponds to dataPoint5
          dummyFundingBox
        ),
        Array.empty,
        Array(
          KioskBox(poolAddress, minStorageRent, Array(KioskLong(1), KioskInt(r5 + 1)), Array((config.poolNFT, 1)), creationHeight = Some(newHeight)),
          KioskBox(refreshAddress, minStorageRent, Array.empty, Array((refreshNFT, 1), (rewardTokenId, 1000000L - 10))),
          KioskBox(contracts.oracleAddress, minStorageRent, Array(pubKey1), Array((oracleTokenId, 1), (rewardTokenId, 16))),
          KioskBox(contracts.oracleAddress, minStorageRent, Array(pubKey2), Array((oracleTokenId, 1), (rewardTokenId, 21))),
          KioskBox(contracts.oracleAddress, minStorageRent, Array(pubKey3), Array((oracleTokenId, 1), (rewardTokenId, 31))),
          KioskBox(contracts.oracleAddress, minStorageRent, Array(pubKey4), Array((oracleTokenId, 1), (rewardTokenId, 41))),
          KioskBox(contracts.oracleAddress, minStorageRent, Array(pubKey5), Array((oracleTokenId, 1), (rewardTokenId, 51)))
        ),
        fee = 1500000,
        changeAddress,
        Array[String](privKey1.toString),
        Array.empty,
        false
      )
      .getOutputsToSpend
      .get(0)
  }

  property("Update pool box") {
    val newConfig = OraclePool.poolConfig.copy(refreshNFT = newRefreshNFT, oracleTokenId = newOracleTokenId, updateNFT = newUpdateNFT, ballotTokenId = newBallotTokenId, minDataPoints = 5)
    val newPool = new Contracts(newConfig)

    // ensure all addresses apart from oracle address are different (oracle address is same because it is tied only to pool-NFT, which is preserved in an update)
    require(contracts.oracleAddress == newPool.oracleAddress) // even though address is same, we still need to create new oracle boxes because the oracle token id is different
    require(contracts.poolAddress != newPool.poolAddress)
    require(contracts.refreshAddress != newPool.refreshAddress)
    require(contracts.updateAddress != newPool.updateAddress)
    require(contracts.ballotAddress != newPool.ballotAddress)

    // ensure all tokens except pool NFT are different
    require(config.poolNFT == newConfig.poolNFT)
    require(config.refreshNFT != newConfig.refreshNFT)
    require(config.oracleTokenId != newConfig.oracleTokenId)
    require(config.updateNFT != newConfig.updateNFT)
    require(config.ballotTokenId != newConfig.ballotTokenId)

    createMockedErgoClient(MockData(Nil, Nil)).execute { implicit ctx: BlockchainContext =>
      // update fresh pool box
      updatePoolBox(bootstrapPoolBox(0, 1, 1))

      // update refreshed pool box
      updatePoolBox(refresh(bootstrapPoolBox(0, 1, 0), bootstrapRefreshBox(1000000L), rewardTokenId, config.oracleTokenId, config.refreshNFT))

      def updatePoolBox(poolBox: InputBox) = {
        val votes = vote(newPool.poolErgoTree, contracts.ballotAddress, config.ballotTokenId)
        val votedBox1 = votes(0)
        val votedBox2 = votes(1)
        val votedBox3 = votes(2)
        val votedBox4 = votes(3)
        val votedBox5 = votes(4)
        val votedBox6 = votes(5)

        val oldCreationHeight = poolBox.getCreationHeight
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
              KioskBox(newPool.poolAddress, config.minStorageRent, Array(KioskLong(1), KioskInt(1)), Array((config.poolNFT, 1)), creationHeight = Some(oldCreationHeight)),
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
        an[Throwable] should be thrownBy
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

  def update(oldPoolBox: InputBox, oldUpdateBox: InputBox, newPoolErgoTree: Values.ErgoTree, ballotAddress: String, ballotTokenId: String)(implicit ctx: BlockchainContext) = {
    val votes = vote(newPoolErgoTree, ballotAddress, ballotTokenId)
    val votedBox1 = votes(0)
    val votedBox2 = votes(1)
    val votedBox3 = votes(2)
    val votedBox4 = votes(3)
    val votedBox5 = votes(4)
    val votedBox6 = votes(5)

    val updateAddress = getStringFromAddress(getAddressFromErgoTree(oldUpdateBox.getErgoTree))
    val newPoolAddress = getStringFromAddress(getAddressFromErgoTree(newPoolErgoTree))

    val oldCreationHeight = oldPoolBox.getCreationHeight
    val r4: Long = oldPoolBox.getRegisters.get(0).asInstanceOf[ErgoValue[Long]].getValue
    val r5: Int = oldPoolBox.getRegisters.get(1).asInstanceOf[ErgoValue[Int]].getValue

    TxUtil.createTx(
      Array(
        oldPoolBox,
        oldUpdateBox,
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
        KioskBox(newPoolAddress, config.minStorageRent, Array(KioskLong(r4), KioskInt(r5)), Array((config.poolNFT, 1)), creationHeight = Some(oldCreationHeight)),
        KioskBox(updateAddress, config.minStorageRent, Array.empty, Array((config.updateNFT, 1))),
        KioskBox(ballotAddress, config.minStorageRent, Array(pubKey1), Array((config.ballotTokenId, 1))),
        KioskBox(ballotAddress, config.minStorageRent, Array(pubKey2), Array((config.ballotTokenId, 1))),
        KioskBox(ballotAddress, config.minStorageRent, Array(pubKey3), Array((config.ballotTokenId, 1))),
        KioskBox(ballotAddress, config.minStorageRent, Array(pubKey4), Array((config.ballotTokenId, 1))),
        KioskBox(ballotAddress, config.minStorageRent, Array(pubKey5), Array((config.ballotTokenId, 1))),
        KioskBox(ballotAddress, config.minStorageRent, Array(pubKey6), Array((config.ballotTokenId, 1)))
      ),
      1500000,
      changeAddress,
      Array.empty,
      Array.empty,
      false
    )

  }

  // refresh of new pool box using same oracle tokens should be possible
  property("Refresh after Update") {
    val newConfig = OraclePool.poolConfig.copy(refreshNFT = newRefreshNFT, minDataPoints = 3) // change only refreshNFT (i.e., refresh box + reward tokens) and minDataPoints
    val newPool = new Contracts(newConfig)

    // ensure all address apart from refresh address and pool address are same
    require(contracts.oracleAddress == newPool.oracleAddress)
    require(contracts.updateAddress == newPool.updateAddress)
    require(contracts.ballotAddress == newPool.ballotAddress)
    require(contracts.poolAddress != newPool.poolAddress)
    require(contracts.refreshAddress != newPool.refreshAddress)

    // ensure all tokens except refresh NFT are same
    require(config.poolNFT == newConfig.poolNFT)
    require(config.oracleTokenId == newConfig.oracleTokenId)
    require(config.updateNFT == newConfig.updateNFT)
    require(config.ballotTokenId == newConfig.ballotTokenId)
    require(config.refreshNFT != newConfig.refreshNFT)

    createMockedErgoClient(MockData(Nil, Nil)).execute { implicit ctx: BlockchainContext =>
      val oldPoolBox = bootstrapPoolBox(0, 1, 0)
      val updateBox = bootstrapUpdateBox(Some(1))
      val updateTx = update(oldPoolBox, updateBox, newPool.poolErgoTree, contracts.ballotAddress, config.ballotTokenId)
      val newPoolBox = updateTx.getOutputsToSpend.get(0)
      val newRefreshBox = bootstrapRefreshBox(1000000L, Some(newRewardTokenId), Some(newPool.refreshAddress), Some(newConfig.refreshNFT))

      noException should be thrownBy refresh(newPoolBox, newRefreshBox, newRewardTokenId, config.oracleTokenId, newConfig.refreshNFT)
    }
  }

  // refresh of new pool box using different oracle tokens should be possible
  property("Refresh after Update with new oracle token id") {
    val newConfig = OraclePool.poolConfig.copy(refreshNFT = newRefreshNFT, minDataPoints = 3, oracleTokenId = newOracleTokenId)
    val newPool = new Contracts(newConfig)

    // ensure all address apart from refresh address and pool address are same
    require(contracts.oracleAddress == newPool.oracleAddress)
    require(contracts.updateAddress == newPool.updateAddress)
    require(contracts.ballotAddress == newPool.ballotAddress)
    require(contracts.poolAddress != newPool.poolAddress)
    require(contracts.refreshAddress != newPool.refreshAddress)

    // ensure all tokens except refresh NFT are same
    require(config.poolNFT == newConfig.poolNFT)
    require(config.oracleTokenId != newConfig.oracleTokenId)
    require(config.updateNFT == newConfig.updateNFT)
    require(config.ballotTokenId == newConfig.ballotTokenId)
    require(config.refreshNFT != newConfig.refreshNFT)

    createMockedErgoClient(MockData(Nil, Nil)).execute { implicit ctx: BlockchainContext =>
      val oldPoolBox = bootstrapPoolBox(0, 1, 0)
      val updateBox = bootstrapUpdateBox(Some(1))
      val updateTx = update(oldPoolBox, updateBox, newPool.poolErgoTree, contracts.ballotAddress, config.ballotTokenId)
      val newPoolBox = updateTx.getOutputsToSpend.get(0)
      val newRefreshBox = bootstrapRefreshBox(1000000L, Some(newRewardTokenId), Some(newPool.refreshAddress), Some(newConfig.refreshNFT))

      noException should be thrownBy refresh(newPoolBox, newRefreshBox, newRewardTokenId, newConfig.oracleTokenId, newConfig.refreshNFT)
    }
  }
}
