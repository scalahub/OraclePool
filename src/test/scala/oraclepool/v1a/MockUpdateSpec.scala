package oraclepool.v1a

import kiosk.ErgoUtil
import kiosk.encoding.ScalaErgoConverters
import kiosk.encoding.ScalaErgoConverters.stringToGroupElement
import kiosk.ergo.{KioskLong, _}
import kiosk.tx.TxUtil
import org.ergoplatform.appkit._
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class MockUpdateSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with HttpClientTesting {

  val ergoClient1 = createMockedErgoClient(MockData.empty)

  property("Mock Update") {
    ergoClient1.execute { implicit ctx: BlockchainContext =>
      val pool = new OraclePool {}
      val newPool = new OraclePool {
        override def oracleReward = 5000000 // Nano ergs. One reward per data point to be paid to oracle
      }

      pool.ballotAddress shouldBe "7gak11YHej2wnu7YFDxdNtoXZrvc2iysCubCkeAT1rTnuNxKSh35tZCynt1DMjBydS7eS1SJwEM6pruA9fk6MjZFMGpNmUFW61CgEFve5dYcDXBQxf98oh5rnL3LRz6V1QfubNzGt8WDbcijvtCHNSWxmbMmV8Dit4rzHjGjuVp6j2ZjdijmqaDwf7T1qfrW"
      pool.epochPrepAddress shouldNot be(newPool.epochPrepAddress)

      // now check voting.
      val poolBoxIn: InputBox = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(6786250000L)
        .tokens(
          new ErgoToken(pool.poolNFT, 1)
        )
        .registers(
          KioskLong(473933649L).getErgoValue,
          KioskInt(451338).getErgoValue
        )
        .contract(
          ctx.newContract(
            ScalaErgoConverters
              .getAddressFromString(pool.epochPrepAddress)
              .script)
        )
        .build()
        .convertToInputWith("0ce3903041628de4b2707bc40ca5aaeb6c55d455f716bd935da5f7258211bd8c", 0)

      val updateBoxIn: InputBox = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(1000000L)
        .tokens(
          new ErgoToken(pool.updateNFT, 1)
        )
        .registers(
          KioskCollByte("01".decodeHex).getErgoValue
        )
        .contract(
          ctx.newContract(
            ScalaErgoConverters
              .getAddressFromString(pool.updateAddress)
              .script)
        )
        .build()
        .convertToInputWith("c67bb073e10c0970fde4fdebd3389ccc490f8cfbb33dbc0963178639dce89633", 0)

      // ballot boxes (Inputs)
      // R4 the pub key of voter [GroupElement] (not used here)
      // R5 dummy int due to AOTC non-lazy evaluation (from the line marked ****)
      // R6 the box id of this box [Coll[Byte]]
      // R7 the value voted for [Coll[Byte]]

      // Dummy address to use for voting
      // address "9eiuh5bJtw9oWDVcfJnwTm1EHfK5949MEm5DStc2sD1TLwDSrpx"
      // group element "021ae6ece10590e79fb74f8bdd1305e4ce479aad52b277751cccbf92d4c5bba2bf"
      // private key "37cc5cb5b54f98f92faef749a53b5ce4e9921890d9fb902b4456957d50791bd0"

      val dummyGroupElement = KioskGroupElement(
        stringToGroupElement(
          ErgoUtil.addressToGroupElement("9eiuh5bJtw9oWDVcfJnwTm1EHfK5949MEm5DStc2sD1TLwDSrpx")
        )
      )

      val ballotBoxInCandidate = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(10000000)
        .tokens(new ErgoToken(pool.ballotToken, 4))
        .contract(ctx.newContract(ScalaErgoConverters.getAddressFromString(pool.ballotAddress).script))
        .registers(
          dummyGroupElement.getErgoValue,
          KioskInt(1).getErgoValue,
          KioskCollByte(updateBoxIn.getId.getBytes).getErgoValue,
          KioskCollByte(newPool.epochPrepScriptHash.decodeHex).getErgoValue
        )
        .build()

      val ballotBoxIn0 = ballotBoxInCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 0)
      val ballotBoxIn1 = ballotBoxInCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 1)
      val ballotBoxIn2 = ballotBoxInCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 2)
      val ballotBoxIn3 = ballotBoxInCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 3)
      val ballotBoxIn4 = ballotBoxInCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 4)
      val ballotBoxIn5 = ballotBoxInCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 5)
      val ballotBoxIn6 = ballotBoxInCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 6)
      val ballotBoxIn7 = ballotBoxInCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 7)

      // new update box
      val updateBoxOut = KioskBox(
        pool.updateAddress,
        value = updateBoxIn.getValue,
        registers = Array(),
        tokens = Array(pool.updateNFT -> 1L)
      )

      // new pool box
      val poolBoxOut = KioskBox(
        newPool.epochPrepAddress,
        value = poolBoxIn.getValue,
        registers = Array(KioskLong(473933649L), KioskInt(451338)),
        tokens = Array(pool.poolNFT -> 1L)
      )

      val ballotBoxOut0 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> 4L))
      val ballotBoxOut1 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> 4L))
      val ballotBoxOut2 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> 4L))
      val ballotBoxOut3 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> 4L))
      val ballotBoxOut4 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> 4L))
      val ballotBoxOut5 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> 4L))
      val ballotBoxOut6 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> 4L))
      val ballotBoxOut7 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> 4L))

      // dummy custom input box for funding various transactions
      val changeAddress = "9f5ZKbECVTm25JTRQHDHGM5ehC8tUw5g1fCBQ4aaE792rWBFrjK"
      val dummyTxId = "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"
      val dummyScript = "sigmaProp(true)"
      val dummyFundingBox = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(100000000000L)
        .contract(ctx.compileContract(ConstantsBuilder.empty(), dummyScript))
        .build()
        .convertToInputWith(dummyTxId, 0)

      TxUtil.createTx(
        inputBoxes = Array(
          updateBoxIn,
          poolBoxIn,
          ballotBoxIn0,
          ballotBoxIn1,
          ballotBoxIn2,
//          ballotBoxIn3,
//          ballotBoxIn4,
//          ballotBoxIn5,
//          ballotBoxIn6,
//          ballotBoxIn7,
          dummyFundingBox
        ),
        dataInputs = Array(),
        boxesToCreate = Array(
          updateBoxOut,
          poolBoxOut,
          ballotBoxOut0,
          ballotBoxOut1,
          ballotBoxOut2,
//          ballotBoxOut3,
//          ballotBoxOut4,
//          ballotBoxOut5,
//          ballotBoxOut6,
//          ballotBoxOut7
        ),
        1000000L,
        changeAddress,
        proveDlogSecrets = Array[String](),
        dhtData = Array(),
        broadcast = false
      )
    }
  }

}
