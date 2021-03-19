package oraclepool

import kiosk.ErgoUtil
import kiosk.encoding.ScalaErgoConverters
import kiosk.encoding.ScalaErgoConverters.{getAddressFromErgoTree, getStringFromAddress, stringToGroupElement}
import kiosk.ergo._
import kiosk.tx.TxUtil
import org.ergoplatform.appkit._
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scorex.crypto.hash.Blake2b256
import sigmastate.lang.exceptions.InterpreterException
import special.sigma.GroupElement

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
      val currentPool = new OraclePool {}
      val newPool = new OraclePool {
        override lazy val maxNumOracles = 13
        override def livePeriod = 12 // blocks          CHANGED!!
        override def prepPeriod = 6 // blocks           CHANGED!!
        override def buffer = 4 // blocks               CHANGED!!
      }

      require(currentPool.epochPrepAddress != newPool.epochPrepAddress)
      require(currentPool.liveEpochAddress != newPool.liveEpochAddress)

      require(currentPool.poolNFT == newPool.poolNFT)
      require(currentPool.updateNFT == newPool.updateNFT)
      require(currentPool.ballotToken == newPool.ballotToken)
      require(currentPool.oracleToken == newPool.oracleToken)

      require(currentPool.updateAddress == newPool.updateAddress)
      require(currentPool.ballotAddress == newPool.ballotAddress)
      require(currentPool.dataPointAddress == newPool.dataPointAddress)
      require(currentPool.poolDepositAddress == newPool.poolDepositAddress)

      // dummy custom input box for funding various transactions
      val dummyFundingBox = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(100000000000L)
        .registers(KioskCollByte(Array(1)).getErgoValue)
        .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
        .build()
        .convertToInputWith(dummyTxId, 0)

      object Voters {
        // define voters
        val addresses = Seq(
          "9eiuh5bJtw9oWDVcfJnwTm1EHfK5949MEm5DStc2sD1TLwDSrpx", // private key is 37cc5cb5b54f98f92faef749a53b5ce4e9921890d9fb902b4456957d50791bd0
          "9f9q6Hs7vXZSQwhbrptQZLkTx15ApjbEkQwWXJqD2NpaouiigJQ", // private key is 5878ae48fe2d26aa999ed44437cffd2d4ba1543788cff48d490419aef7fc149d
          "9fGp73EsRQMpFC7xaYD5JFy2abZeKCUffhDBNbQVtBtQyw61Vym", // private key is 3ffaffa96b2fd6542914d3953d05256cd505d4beb6174a2601a4e014c3b5a78e
        ).toArray

        val privateKey0 = "37cc5cb5b54f98f92faef749a53b5ce4e9921890d9fb902b4456957d50791bd0"
        val privateKey1 = "5878ae48fe2d26aa999ed44437cffd2d4ba1543788cff48d490419aef7fc149d"
        val privateKey2 = "3ffaffa96b2fd6542914d3953d05256cd505d4beb6174a2601a4e014c3b5a78e"

        val r4voter0 = KioskGroupElement(stringToGroupElement(ErgoUtil.addressToGroupElement(addresses(0))))
        val r4voter1 = KioskGroupElement(stringToGroupElement(ErgoUtil.addressToGroupElement(addresses(1))))
        val r4voter2 = KioskGroupElement(stringToGroupElement(ErgoUtil.addressToGroupElement(addresses(2))))

        val ballot0Box = KioskBox(currentPool.ballotAddress, value = 200000000, registers = Array(r4voter0), tokens = Array((currentPool.ballotToken, 3L)))
        val ballot1Box = KioskBox(currentPool.ballotAddress, value = 200000000, registers = Array(r4voter1), tokens = Array((currentPool.ballotToken, 4L)))
        val ballot2Box = KioskBox(currentPool.ballotAddress, value = 200000000, registers = Array(r4voter2), tokens = Array((currentPool.ballotToken, 1L)))
      }

      // old update box
      val updateOutBox = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(currentPool.minStorageRent)
        .tokens(new ErgoToken(currentPool.updateNFT, 1))
        .contract(ctx.newContract(ScalaErgoConverters.getAddressFromString(currentPool.updateAddress).script))
        .build()

      val updateBoxIn = updateOutBox.convertToInputWith(dummyTxId, 0)

      // old pool box
      val currentEpochPrepContract = ctx.newContract(ScalaErgoConverters.getAddressFromString(currentPool.epochPrepAddress).script)

      val poolBoxIn = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(10000000)
        .tokens(new ErgoToken(currentPool.poolNFT, 1))
        .contract(currentEpochPrepContract)
        .registers(KioskLong(100).getErgoValue, KioskInt(100).getErgoValue)
        .build()
        .convertToInputWith(dummyTxId, 0)

      // value to vote for; hash of new pool box script
      val valueVotedFor = KioskCollByte(Blake2b256(newPool.epochPrepErgoTree.bytes))

      val ballot0BoxToCreate = Voters.ballot0Box.copy(
        registers = Array(
          Voters.ballot0Box.registers(0),
          KioskInt(0), // dummy value due to AOTC non-lazy eval bug
          KioskCollByte(updateBoxIn.getId.getBytes),
          valueVotedFor
        )
      )

      val ballot1BoxToCreate = Voters.ballot1Box.copy(
        registers = Array(
          Voters.ballot1Box.registers(0),
          KioskInt(0), // dummy value due to AOTC non-lazy eval bug
          KioskCollByte(updateBoxIn.getId.getBytes),
          valueVotedFor
        )
      )

      val ballot2BoxToCreate = Voters.ballot2Box.copy(
        registers = Array(
          Voters.ballot2Box.registers(0),
          KioskInt(0), // dummy value due to AOTC non-lazy eval bug
          KioskCollByte(updateBoxIn.getId.getBytes),
          valueVotedFor
        )
      )

      // create ballots
      val ballot0 = TxUtil.createTx(
        inputBoxes = Array(Voters.ballot0Box.toInBox(dummyTxId, 0), dummyFundingBox),
        dataInputs = Array(),
        boxesToCreate = Array(ballot0BoxToCreate),
        fee,
        changeAddress,
        proveDlogSecrets = Array[String](Voters.privateKey0),
        Array[DhtData](),
        false
      ).getOutputsToSpend.get(0)

      val ballot1 = TxUtil.createTx(
        inputBoxes = Array(Voters.ballot1Box.toInBox(dummyTxId, 0), dummyFundingBox),
        dataInputs = Array(),
        boxesToCreate = Array(ballot1BoxToCreate),
        fee,
        changeAddress,
        proveDlogSecrets = Array[String](Voters.privateKey1),
        Array[DhtData](),
        false
      ).getOutputsToSpend.get(0)

      val ballot2 = TxUtil.createTx(
        inputBoxes = Array(Voters.ballot2Box.toInBox(dummyTxId, 0), dummyFundingBox),
        dataInputs = Array(),
        boxesToCreate = Array(ballot2BoxToCreate),
        fee,
        changeAddress,
        proveDlogSecrets = Array[String](Voters.privateKey2),
        Array[DhtData](),
        false
      ).getOutputsToSpend.get(0)

      // voting should fail with wrong private key
      the[AssertionError] thrownBy {
        TxUtil.createTx(
          inputBoxes = Array(Voters.ballot2Box.toInBox(dummyTxId, 0), dummyFundingBox),
          dataInputs = Array(),
          boxesToCreate = Array(ballot2BoxToCreate),
          fee,
          changeAddress,
          proveDlogSecrets = Array[String](Voters.privateKey0),
          Array[DhtData](),
          false
        )
      } should have message "assertion failed: Tree root should be real but was UnprovenSchnorr(ProveDlog((6357df04d57e9d4d0564e217a0e7e6d201df72787b5f89203744e9384402378d,567526ec80c82cca6bbb6d5324e65c6cf9b39704ad91e3eca93f84bf481bf38a,1)),None,None,None,true,0)"

      // new update box
      val updateBoxOut = KioskBox(
        currentPool.updateAddress,
        value = updateBoxIn.getValue,
        registers = Array(),
        tokens = Array(currentPool.updateNFT -> 1L)
      )

      // new pool box
      val poolBoxOut = KioskBox(
        newPool.epochPrepAddress,
        value = poolBoxIn.getValue,
        registers = Array(KioskLong(100), KioskInt(100)),
        tokens = Array(currentPool.poolNFT -> 1L)
      )

      // Should succeed with sufficient votes (8)
      TxUtil.createTx(
        inputBoxes = Array(updateBoxIn, poolBoxIn, ballot0, ballot1, ballot2, dummyFundingBox),
        dataInputs = Array(),
        boxesToCreate = Array(updateBoxOut, poolBoxOut, ballot0BoxToCreate, ballot1BoxToCreate, ballot2BoxToCreate),
        fee,
        changeAddress,
        proveDlogSecrets = Array[String](),
        Array[DhtData](),
        false
      )

      // should fail for invalid output pool box script
      the[Exception] thrownBy {
        TxUtil.createTx(
          inputBoxes = Array(updateBoxIn, poolBoxIn, ballot0, ballot1, ballot2, dummyFundingBox),
          dataInputs = Array(),
          boxesToCreate = Array(updateBoxOut, poolBoxOut.copy(address = currentPool.epochPrepAddress), ballot0BoxToCreate, ballot1BoxToCreate, ballot2BoxToCreate),
          fee,
          changeAddress,
          proveDlogSecrets = Array[String](),
          Array[DhtData](),
          false
        )
      } should have message "Script reduced to false"

      // should fail for invalid input update box Id
      val invalidUpdateBoxIn = updateOutBox.convertToInputWith(dummyTxId, 1) // different outputIndex

      the[Exception] thrownBy {
        TxUtil.createTx(
          inputBoxes = Array(invalidUpdateBoxIn, poolBoxIn, ballot0, ballot1, ballot2, dummyFundingBox),
          dataInputs = Array(),
          boxesToCreate = Array(updateBoxOut, poolBoxOut.copy(address = currentPool.epochPrepAddress), ballot0BoxToCreate, ballot1BoxToCreate, ballot2BoxToCreate),
          fee,
          changeAddress,
          proveDlogSecrets = Array[String](),
          Array[DhtData](),
          false
        )
      } should have message "Script reduced to false"

      // Should fail when having insufficient votes (7)
      the[Exception] thrownBy {
        TxUtil.createTx(
          inputBoxes = Array(updateBoxIn, poolBoxIn, ballot0, ballot1, dummyFundingBox),
          dataInputs = Array(),
          boxesToCreate = Array(updateBoxOut, poolBoxOut, ballot0BoxToCreate, ballot1BoxToCreate),
          fee,
          changeAddress,
          proveDlogSecrets = Array[String](),
          Array[DhtData](),
          false
        )
      } should have message "Script reduced to false"

    }
  }
}
