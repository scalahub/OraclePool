package oraclepool.v2.helpers

import kiosk.ErgoUtil.{addressToGroupElement => addr2Grp}
import kiosk.encoding.ScalaErgoConverters.{stringToGroupElement => str2Grp}
import kiosk.ergo.{DhtData, KioskBox, KioskCollByte, KioskGroupElement, KioskInt, KioskLong, StringToBetterString}
import kiosk.tx.TxUtil
import oraclepool.v2.{Contracts, OraclePool}
import oraclepool.v2.OraclePool.pool
import oraclepool.v2.OraclePool.pool.config._
import org.ergoplatform.appkit.{BlockchainContext, ConstantsBuilder, ContextVar, ErgoToken, ErgoValue, InputBox}
import scorex.crypto.hash.Blake2b256
import special.sigma.GroupElement

trait MockHelpers {
  private lazy val keyPairs: Array[(KioskGroupElement, BigInt)] = Array(
    "9eiuh5bJtw9oWDVcfJnwTm1EHfK5949MEm5DStc2sD1TLwDSrpx" -> "37cc5cb5b54f98f92faef749a53b5ce4e9921890d9fb902b4456957d50791bd0",
    "9f9q6Hs7vXZSQwhbrptQZLkTx15ApjbEkQwWXJqD2NpaouiigJQ" -> "5878ae48fe2d26aa999ed44437cffd2d4ba1543788cff48d490419aef7fc149d",
    "9fGp73EsRQMpFC7xaYD5JFy2abZeKCUffhDBNbQVtBtQyw61Vym" -> "3ffaffa96b2fd6542914d3953d05256cd505d4beb6174a2601a4e014c3b5a78e",
    "9fSqnSHKLzRz7sRkfwNW4Rqtmig2bHNaaspsQms1gY2sU6LA2Ng" -> "148bb91ada6ad5e6b1bba02fe70ecd96095e00cbaf0f1f9294f02fedf9855ea0",
    "9g3izpikC6xuvhnXxNHT1y5nwJNofMsoPiCgr4JXcZV6GUgWPqh" -> "148bb91ada6ad5e6b1bba02fe70ecd96095e00cbaf0f1f9294f02fedf9855ea1",
    "9g1RasRmLpijKSD1TWuGnCEmBfacRzGANqwHPKXNPcSQypsHCT5" -> "009b74e570880f0a558ed1231c280aff9d8afb2ba238e4ac2ea2d4f5507f01c6ae"
  ).map { case (address, secret) => KioskGroupElement(str2Grp(addr2Grp(address))) -> BigInt(secret, 16) }

  val pubKey1: KioskGroupElement = keyPairs(0)._1
  val pubKey2: KioskGroupElement = keyPairs(1)._1
  val pubKey3: KioskGroupElement = keyPairs(2)._1
  val pubKey4: KioskGroupElement = keyPairs(3)._1
  val pubKey5: KioskGroupElement = keyPairs(4)._1
  val pubKey6: KioskGroupElement = keyPairs(5)._1

  val privKey1: BigInt = keyPairs(0)._2
  val privKey2: BigInt = keyPairs(1)._2
  val privKey3: BigInt = keyPairs(2)._2
  val privKey4: BigInt = keyPairs(3)._2
  val privKey5: BigInt = keyPairs(4)._2
  val privKey6: BigInt = keyPairs(5)._2

  val changeAddress = "9f5ZKbECVTm25JTRQHDHGM5ehC8tUw5g1fCBQ4aaE792rWBFrjK"

  val dummyTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"
  val dummyBoxId = "5267556B58703273357638792F413F4428472B4B6250655368566D5971337436"
  val dummyIndex = 1.toShort

  val dummyEpochCounter = 1

  val rewardTokenId = "6D597133743677397A244326462948404D635166546A576E5A72347537782141"

  val junkTokenId = "34743777217A25432A46294A404E635266556A586E3272357538782F413F4428"
  val dummyScript = "sigmaProp(true)"
  val junkAddress = "4MQyML64GnzMxZgm" // sigmaProp(true)
  val dummyNanoErgs = 10000000000000L

  val newRefreshNFT = "404E635266556A586E327235753878214125442A472D4B6150645367566B5970"
  val newOracleTokenId = "3677397A244226452948404D635166546A576E5A7234753778214125442A462D"
  val newRewardTokenId = "472D4B6150645367566B59703373367639792F423F4528482B4D625165546857"

  val newUpdateNFT = "452948404D635166546A576E5A7234753778217A25432A462D4A614E64526755"
  val newBallotTokenId = "703373367639792F423F4528482B4D6251655468576D5A7134743777217A2543"

  // NOTE: When updating refreshNFT:
  //    1. Must update oracleTokenId
  //    2. Must update rewardTokenID
  //    3. Must create new oracle boxes
  //    4. Must create new refresh box
  //
  // Similarly, when updating updateNFT:
  //    1. Must update ballotTokenId
  //    2. Must create new ballot boxes
  //    3. Must create new update box

  val newConfig = OraclePool.poolConfig.copy(refreshNFT = newRefreshNFT, oracleTokenId = newOracleTokenId, updateNFT = newUpdateNFT, ballotTokenId = newBallotTokenId, minDataPoints = 5)
  val newPool = new Contracts(newConfig)

  def dummyFundingBox(implicit ctx: BlockchainContext) =
    ctx // for funding transactions
      .newTxBuilder()
      .outBoxBuilder
      .value(dummyNanoErgs)
      .tokens(new ErgoToken(junkTokenId, 1000000L))
      .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
      .build()
      .convertToInputWith(dummyTxId, dummyIndex)

  def bootstrapBallotBox(pubKey: KioskGroupElement)(implicit ctx: BlockchainContext) =
    TxUtil
      .createTx(
        Array(
          ctx // for funding transactions
            .newTxBuilder()
            .outBoxBuilder
            .value(dummyNanoErgs)
            .tokens(new ErgoToken(ballotTokenId, 1))
            .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
            .build()
            .convertToInputWith(dummyTxId, dummyIndex)),
        Array[InputBox](),
        Array(
          KioskBox(
            pool.ballotAddress,
            value = minStorageRent,
            registers = Array(pubKey),
            tokens = Array((ballotTokenId, 1))
          )),
        fee = 1000000L,
        changeAddress,
        Array[String](),
        Array[DhtData](),
        false
      )
      .getOutputsToSpend
      .get(0)

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

  def bootstrapUpdateBox(customCreationHeight: Option[Int])(implicit ctx: BlockchainContext) =
    TxUtil
      .createTx(
        Array(
          ctx // for funding transactions
            .newTxBuilder()
            .outBoxBuilder
            .value(dummyNanoErgs)
            .tokens(new ErgoToken(updateNFT, 1))
            .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
            .build()
            .convertToInputWith(dummyTxId, dummyIndex)),
        Array[InputBox](),
        Array(
          KioskBox(
            pool.updateAddress,
            value = minStorageRent,
            registers = Array.empty,
            tokens = Array((updateNFT, 1)),
            creationHeight = customCreationHeight
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

  def transferBallotBox(inputBallotBox: InputBox, privKey: BigInt, newPubKey: Option[KioskGroupElement])(implicit ctx: BlockchainContext) =
    TxUtil
      .createTx(
        Array(inputBallotBox.withContextVars(new ContextVar(0, KioskInt(0).getErgoValue)), dummyFundingBox),
        Array[InputBox](),
        Array(
          KioskBox(
            pool.ballotAddress,
            value = minStorageRent,
            registers = newPubKey.toArray,
            tokens = Array((ballotTokenId, 1L))
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

  def voteForUpdate(votedValue: Array[Byte],
                    updateBoxCreationHeight: Int,
                    outAddress: String,
                    outValue: Long,
                    inputBallotBox: InputBox,
                    privKey: BigInt,
                    contextVarOutIndex: Int,
                    newPubKey: Option[KioskGroupElement] = None)(implicit ctx: BlockchainContext) = {
    TxUtil
      .createTx(
        Array(inputBallotBox.withContextVars(new ContextVar(0, KioskInt(contextVarOutIndex).getErgoValue)), dummyFundingBox),
        Array[InputBox](),
        Array(
          KioskBox(
            outAddress,
            value = outValue,
            registers = Array(
              newPubKey.getOrElse(KioskGroupElement(inputBallotBox.getRegisters.get(0).asInstanceOf[ErgoValue[GroupElement]].getValue)),
              KioskInt(updateBoxCreationHeight),
              KioskCollByte(Blake2b256(votedValue))
            ),
            tokens = Array((ballotTokenId, 1L))
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
}
