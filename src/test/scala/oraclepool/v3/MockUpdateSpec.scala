package oraclepool.v3

import kiosk.ErgoUtil
import kiosk.encoding.ScalaErgoConverters
import kiosk.encoding.ScalaErgoConverters.stringToGroupElement
import kiosk.ergo.{KioskLong, _}
import kiosk.tx.TxUtil
import oraclepool.v3.OraclePool
import org.ergoplatform.appkit.{BlockchainContext, ConstantsBuilder, ContextVar, ErgoToken, ErgoValue, FileMockedErgoClient, HttpClientTesting, InputBox, PreHeader}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scalan.util.FileUtil
import special.collection.Coll
import special.sigma.GroupElement

import java.{lang, util}
import scala.jdk.CollectionConverters._

class MockUpdateSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks with HttpClientTesting {
  val ergoClient = createMockedErgoClient(MockData(Nil, Nil))

  property("Mock Update") {
    ergoClient.execute { implicit ctx: BlockchainContext =>
      val pool = new OraclePool {}
      val newPool = new OraclePool {
        override def oracleReward = 5000000 // Nano ergs. One reward per data point to be paid to oracle
      }
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
      // R5 the box id of this box [Coll[Byte]]
      // R6 the value voted for [Coll[Byte]]

      // Dummy address to use for voting
      // address "9eiuh5bJtw9oWDVcfJnwTm1EHfK5949MEm5DStc2sD1TLwDSrpx"
      // group element "021ae6ece10590e79fb74f8bdd1305e4ce479aad52b277751cccbf92d4c5bba2bf"
      // private key "37cc5cb5b54f98f92faef749a53b5ce4e9921890d9fb902b4456957d50791bd0"

      val dummyGroupElement = KioskGroupElement(
        stringToGroupElement(
          ErgoUtil.addressToGroupElement("9eiuh5bJtw9oWDVcfJnwTm1EHfK5949MEm5DStc2sD1TLwDSrpx")
        )
      )

      val dummyDLog = "37cc5cb5b54f98f92faef749a53b5ce4e9921890d9fb902b4456957d50791bd0"

      val oldPreheader = ctx.newTxBuilder().getPreHeader

      val newPreHeader: PreHeader = ctx
        .createPreHeader()
        .height(oldPreheader.getHeight - 3)
        .minerPk(oldPreheader.getMinerPk)
        .nBits(oldPreheader.getNBits)
        .timestamp(oldPreheader.getTimestamp - 1)
        .votes(oldPreheader.getVotes)
        .parentId(oldPreheader.getParentId)
        .build()

      val ballotBoxCandidate = ctx
        .newTxBuilder()
        .preHeader(newPreHeader)
        .outBoxBuilder
        .value(10000000)
        .tokens(new ErgoToken(pool.ballotToken, 1))
        .contract(ctx.newContract(ScalaErgoConverters.getAddressFromString(pool.ballotAddress).script))
        .registers(
          dummyGroupElement.getErgoValue,
          KioskCollByte(updateBoxIn.getId.getBytes).getErgoValue,
          KioskCollByte(newPool.epochPrepScriptHash.decodeHex).getErgoValue
        )
        .build()

      val ballotBox0 = ballotBoxCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 0)
      val ballotBox1 = ballotBoxCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 1)
      val ballotBox2 = ballotBoxCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 2)
      val ballotBox3 = ballotBoxCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 3)
      val ballotBox4 = ballotBoxCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 4)
      val ballotBox5 = ballotBoxCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 5)
      val ballotBox6 = ballotBoxCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 6)
      val ballotBox7 = ballotBoxCandidate.convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 7)

      println("ballot " + ballotBox0.toJson(false))
      println("update " + updateBoxIn.toJson(false))
      println("pool " + poolBoxIn.toJson(false))
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

//      val ballotBoxOut0 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> ballotBox0.getTokens.get(0).getValue))
//      val ballotBoxOut1 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> ballotBox1.getTokens.get(0).getValue))
//      val ballotBoxOut2 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> ballotBox2.getTokens.get(0).getValue))
//      val ballotBoxOut3 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> ballotBox3.getTokens.get(0).getValue))
//      val ballotBoxOut4 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> ballotBox4.getTokens.get(0).getValue))
//      val ballotBoxOut5 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> ballotBox5.getTokens.get(0).getValue))
//      val ballotBoxOut6 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> ballotBox6.getTokens.get(0).getValue))
//      val ballotBoxOut7 = KioskBox(pool.ballotAddress, value = 10000000L, registers = Array(dummyGroupElement), tokens = Array(pool.ballotToken -> ballotBox7.getTokens.get(0).getValue))

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
          updateBoxIn.withContextVars(new ContextVar(0.toByte, KioskCollByte(Array[Byte](2, 3, 4, 5, 6, 7, 8, 9)).getErgoValue)),
          poolBoxIn,
          dummyFundingBox
        ),
        dataInputs = Array(
          ballotBox0,
//          ballotBox1,
          ballotBox2,
          ballotBox3,
          ballotBox4,
          ballotBox5,
          ballotBox6,
          ballotBox7
        ) sortWith {
          case (left, right) => BigInt(left.getId.getBytes) > BigInt(right.getId.getBytes)
        },
        boxesToCreate = Array(
          updateBoxOut,
          poolBoxOut
        ),
        1000000L,
        changeAddress,
        proveDlogSecrets = Array[String](dummyDLog),
        dhtData = Array(),
        broadcast = false
      )
    }
  }

}
