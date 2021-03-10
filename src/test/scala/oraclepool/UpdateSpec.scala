package oraclepool

import kiosk.encoding.ScalaErgoConverters
import kiosk.ergo._
import kiosk.tx.TxUtil
import org.ergoplatform.appkit._
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scorex.crypto.hash.Blake2b256

class UpdateSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with HttpClientTesting {
  /*
  In Oracle Pool v7, the epochPrepScript has two spending paths:
  1. poolAction
  2. updateAction

  This class tests the updateAction
   */
  val changeAddress = "9f5ZKbECVTm25JTRQHDHGM5ehC8tUw5g1fCBQ4aaE792rWBFrjK"
  val dummyTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"
  val dummyScript = "sigmaProp(true)"

  val ergoClient = createMockedErgoClient(MockData(Nil, Nil))

  val fee = 1500000

  property("Update") {
    ergoClient.execute { implicit ctx: BlockchainContext =>
      val poolBoxIn = 2518217000L

      val epochPool = new OraclePool {}
      val newOraclePool = new OraclePool {
        override lazy val maxNumOracles = 13
        override def livePeriod = 12 // blocks          CHANGED!!
        override def prepPeriod = 6 // blocks           CHANGED!!
        override def buffer = 4 // blocks               CHANGED!!
      }

      require(epochPool.epochPrepAddress != newOraclePool.epochPrepAddress)

      require(epochPool.liveEpochAddress != newOraclePool.liveEpochAddress)

      // datapoint script should not change
      require(epochPool.dataPointAddress == newOraclePool.dataPointAddress)
      // dummy custom input box for funding various transactions
      val dummyFundingBox = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(fee)
        .registers(KioskCollByte(Array(1)).getErgoValue)
        .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
        .build()
        .convertToInputWith(dummyTxId, 0)

      // old pool box
      val epochPrepBoxIn = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(poolBoxIn)
        .tokens(
          new ErgoToken(epochPool.poolNFT, 1)
        )
        .contract(ctx.newContract(ScalaErgoConverters.getAddressFromString(epochPool.epochPrepAddress).script))
        .registers(KioskLong(100).getErgoValue, KioskInt(100).getErgoValue)
        .build()
        .convertToInputWith(dummyTxId, 0)

      // new pool box
      val epochPrepBoxOut = KioskBox(
        newOraclePool.epochPrepAddress,
        value = epochPrepBoxIn.getValue,
        registers = Array(KioskLong(100), KioskInt(100)),
        tokens = Array(epochPool.poolNFT -> 1L)
      )

      val updatedPoolScriptHash = KioskCollByte(Blake2b256(newOraclePool.epochPrepErgoTree.bytes))

      // old update box
      val updateBoxIn = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(10000000)
        .tokens(new ErgoToken(epochPool.updateNFT, 1))
        .contract(ctx.newContract(ScalaErgoConverters.getAddressFromString(epochPool.updateAddress).script))
        .registers(updatedPoolScriptHash.getErgoValue)
        .build()
        .convertToInputWith(dummyTxId, 0)

      // new update box
      val updateBoxOut = KioskBox(
        epochPool.updateAddress,
        value = updateBoxIn.getValue,
        registers = Array(updatedPoolScriptHash),
        tokens = Array(epochPool.updateNFT -> 1L)
      )

      TxUtil.createTx(
        inputBoxes = Array(epochPrepBoxIn, updateBoxIn, dummyFundingBox),
        dataInputs = Array(),
        boxesToCreate = Array(epochPrepBoxOut, updateBoxOut),
        fee,
        changeAddress,
        Array[String](),
        Array[DhtData](),
        false
      )
    }
  }
}
