package oraclepool.v2.helpers

import kiosk.ErgoUtil.{addressToGroupElement => addr2Grp}
import kiosk.encoding.ScalaErgoConverters.{stringToGroupElement => str2Grp}
import kiosk.ergo.{DhtData, KioskBox, KioskCollByte, KioskGroupElement, KioskInt, KioskLong, StringToBetterString}
import kiosk.tx.TxUtil
import oraclepool.v2.OraclePool.pool
import oraclepool.v2.OraclePool.pool.config._
import org.ergoplatform.appkit.{BlockchainContext, ConstantsBuilder, ContextVar, ErgoToken, ErgoValue, InputBox}
import special.sigma.GroupElement

trait MockHelpers {
  lazy val keyPairs: Array[(KioskGroupElement, BigInt)] = Array(
    "9eiuh5bJtw9oWDVcfJnwTm1EHfK5949MEm5DStc2sD1TLwDSrpx" -> "37cc5cb5b54f98f92faef749a53b5ce4e9921890d9fb902b4456957d50791bd0",
    "9f9q6Hs7vXZSQwhbrptQZLkTx15ApjbEkQwWXJqD2NpaouiigJQ" -> "5878ae48fe2d26aa999ed44437cffd2d4ba1543788cff48d490419aef7fc149d",
    "9fGp73EsRQMpFC7xaYD5JFy2abZeKCUffhDBNbQVtBtQyw61Vym" -> "3ffaffa96b2fd6542914d3953d05256cd505d4beb6174a2601a4e014c3b5a78e",
    "9fSqnSHKLzRz7sRkfwNW4Rqtmig2bHNaaspsQms1gY2sU6LA2Ng" -> "148bb91ada6ad5e6b1bba02fe70ecd96095e00cbaf0f1f9294f02fedf9855ea0",
    "9g3izpikC6xuvhnXxNHT1y5nwJNofMsoPiCgr4JXcZV6GUgWPqh" -> "148bb91ada6ad5e6b1bba02fe70ecd96095e00cbaf0f1f9294f02fedf9855ea1"
  ).map { case (address, secret) => KioskGroupElement(str2Grp(addr2Grp(address))) -> BigInt(secret, 16) }

  val changeAddress = "9f5ZKbECVTm25JTRQHDHGM5ehC8tUw5g1fCBQ4aaE792rWBFrjK"

  val dummyTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"
  val dummyIndex = 1.toShort

  val dummyEpochCounter = 1

  val rewardTokenId = "6D597133743677397A244326462948404D635166546A576E5A72347537782141"
  val junkTokenId = "34743777217A25432A46294A404E635266556A586E3272357538782F413F4428"
  val dummyScript = "sigmaProp(true)"
  val junkAddress = "4MQyML64GnzMxZgm" // sigmaProp(true)
  val dummyNanoErgs = 10000000000000L

  def dummyFundingBox(implicit ctx: BlockchainContext) =
    ctx // for funding transactions
      .newTxBuilder()
      .outBoxBuilder
      .value(dummyNanoErgs)
      .tokens(new ErgoToken(junkTokenId, 1000000L))
      .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
      .build()
      .convertToInputWith(dummyTxId, dummyIndex)

  def bootstrapOracleBox(pubKey: KioskGroupElement, rewardTokenQty: Long)(implicit ctx: BlockchainContext) =
    TxUtil
      .createTx(
        Array(
          ctx // for funding transactions
            .newTxBuilder()
            .outBoxBuilder
            .value(dummyNanoErgs)
            .tokens(new ErgoToken(oracleTokenId, 1), new ErgoToken(rewardTokenId, rewardTokenQty))
            .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
            .build()
            .convertToInputWith(dummyTxId, dummyIndex)),
        Array[InputBox](),
        Array(
          KioskBox(
            pool.oracleAddress,
            value = minStorageRent,
            registers = Array(pubKey),
            tokens = Array((oracleTokenId, 1), (rewardTokenId, rewardTokenQty))
          )),
        fee = 1000000L,
        changeAddress,
        Array[String](),
        Array[DhtData](),
        false
      )
      .getOutputsToSpend
      .get(0)

  def bootstrapPoolBox(customCreationHeight: Int, rate: Long, counter: Int = 0, newPoolNFT: Option[String] = None)(implicit ctx: BlockchainContext) =
    TxUtil
      .createTx(
        Array(
          ctx // for funding transactions
            .newTxBuilder()
            .outBoxBuilder
            .value(dummyNanoErgs)
            .tokens(new ErgoToken(newPoolNFT.getOrElse(poolNFT), 1))
            .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
            .build()
            .convertToInputWith(dummyTxId, dummyIndex)),
        Array[InputBox](),
        Array(
          KioskBox(
            pool.poolAddress,
            value = minStorageRent,
            registers = Array(KioskLong(rate), KioskInt(counter)),
            tokens = Array((newPoolNFT.getOrElse(poolNFT), 1)),
            creationHeight = Some(customCreationHeight)
          )),
        fee = 1000000L,
        changeAddress,
        Array[String](),
        Array[DhtData](),
        false
      )
      .getOutputsToSpend
      .get(0)

  def bootstrapRefreshBox(rewardTokenQty: Long)(implicit ctx: BlockchainContext) =
    TxUtil
      .createTx(
        Array(
          ctx // for funding transactions
            .newTxBuilder()
            .outBoxBuilder
            .value(dummyNanoErgs)
            .tokens(new ErgoToken(refreshNFT, 1), new ErgoToken(rewardTokenId, rewardTokenQty))
            .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
            .build()
            .convertToInputWith(dummyTxId, dummyIndex)),
        Array[InputBox](),
        Array(
          KioskBox(
            pool.refreshAddress,
            value = minStorageRent,
            registers = Array.empty,
            tokens = Array((refreshNFT, 1), (rewardTokenId, rewardTokenQty))
          )),
        fee = 1000000L,
        changeAddress,
        Array[String](),
        Array[DhtData](),
        false
      )
      .getOutputsToSpend
      .get(0)

  private def getOracleTokensToAdd(rewardTokenQty: Long): Array[(String, Long)] = {
    if (rewardTokenQty > 0) Array((oracleTokenId, 1L), (rewardTokenId, rewardTokenQty)) else Array((oracleTokenId, 1L))
  }

  def createDataPoint(dataPointValue: Long,
                      epochCounter: Int,
                      outAddress: String,
                      outValue: Long,
                      inputOracleBox: InputBox,
                      privKey: BigInt,
                      contextVarOutIndex: Int,
                      rewardTokenQty: Long,
                      newPubKey: Option[KioskGroupElement] = None,
                      customCreationHeight: Option[Int] = None)(implicit ctx: BlockchainContext) = {
    TxUtil
      .createTx(
        Array(inputOracleBox.withContextVars(new ContextVar(0, KioskInt(contextVarOutIndex).getErgoValue)), dummyFundingBox),
        Array[InputBox](),
        Array(
          KioskBox(
            outAddress,
            value = outValue,
            registers = Array(
              newPubKey.getOrElse(KioskGroupElement(inputOracleBox.getRegisters.get(0).asInstanceOf[ErgoValue[GroupElement]].getValue)),
              KioskInt(epochCounter),
              KioskLong(dataPointValue)
            ),
            tokens = getOracleTokensToAdd(rewardTokenQty),
            creationHeight = customCreationHeight
          )),
        1500000,
        changeAddress,
        Array[String](privKey.toString),
        Array[DhtData](),
        false
      )
      .getOutputsToSpend
      .get(0)
  }

  def transferOracleBox(inputOracleBox: InputBox, privKey: BigInt, newPubKey: Option[KioskGroupElement], rewardTokenQty: Long)(implicit ctx: BlockchainContext) =
    TxUtil
      .createTx(
        Array(inputOracleBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), dummyFundingBox),
        Array[InputBox](),
        Array(
          KioskBox(
            pool.oracleAddress,
            value = minStorageRent,
            registers = newPubKey.toArray,
            tokens = getOracleTokensToAdd(rewardTokenQty) // at least one reward token has to be transferred
          )
        ),
        1500000,
        changeAddress,
        Array[String](privKey.toString),
        Array[DhtData](),
        false
      )
      .getOutputsToSpend
      .get(0)

}
