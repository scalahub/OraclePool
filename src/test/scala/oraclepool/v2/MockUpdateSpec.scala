package oraclepool.v2

import kiosk.ErgoUtil
import kiosk.encoding.ScalaErgoConverters
import kiosk.encoding.ScalaErgoConverters.stringToGroupElement
import kiosk.ergo.{KioskLong, _}
import kiosk.tx.TxUtil
import oraclepool.v1.OraclePool
import org.ergoplatform.appkit.{BlockchainContext, ConstantsBuilder, ErgoToken, ErgoValue, FileMockedErgoClient, InputBox}
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scalan.util.FileUtil

import java.util
import scala.jdk.CollectionConverters._

class MockUpdateSpec extends PropSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  /*
  Following is a real ballot box
  JDE script:
  {
  "constants": [
    {
      "name": "myAddress",
      "type": "Address",
      "value": "9hz1B19M44TNpmVe8MS4xvXyycehh5uP5aCfj4a6iAowj88hkd2"
    },
    {
      "name": "ballotAddress",
      "type": "Address",
      "value": "7gak11YHej2wnu7YFDxdNmQibKHrXjysJkcb2wRyq3Kqxcvo75HtchnCtUdL4YxY86NE6kV4m4PsWaPc3gTGLiBDSudMb2K8Ep48eVnJaUotoQmtH9A4mBkTyTaYD12Cogt5iLqPegFQYkLjdsBbPUzoGaNBk2KSZNHqNY23roZ78Ey13mRTSZatdb7jDVU4"
    },
    {
      "name": "participantAddress",
      "type": "Address",
      "value": "9fPRvaMYzBPotu6NGvZn4A6N4J2jDmRGs4Zwc9UhFFeSXgRJ8pS"
    },
    {
      "name": "participantPubKey",
      "type": "Groupelement",
      "value": "02725e8878d5198ca7f5853dddf35560ddab05ab0a26adae7e664b84162c9962e5"
    },
    {
      "name": "ballotToken",
      "type": "CollByte",
      "value": "053fefab5477138b760bc7ae666c3e2b324d5ae937a13605cb766ec5222e5518"
    },
    {
      "name": "bootstrapNanoErgs",
      "type": "Long",
      "value": "10000000"
    },
    {
      "name": "one",
      "type": "Long",
      "value": "1"
    }
  ],
  "inputs": [
    {
      "address": {
        "value": "myAddress"
      },
      "tokens" : [
        {
          "id" : {
            "value": "ballotToken"
          }
        }
      ]
    }
  ],
  "outputs": [
    {
      "address": {
        "value": "ballotAddress"
      },
      "tokens": [
        {
          "index": 0,
          "id": {
            "value": "ballotToken"
          },
          "amount": {
            "value": "one"
          }
        }
      ],
      "registers": [
        {
          "num": "R4",
          "value": "participantPubKey",
          "type": "GroupElement"
        }
      ],
      "nanoErgs": {
        "value": "bootstrapNanoErgs"
      }
    }
  ],
  "unaryOps" : [
    {
      "name": "participantErgoTree",
      "op" : "proveDlog",
      "from" : "participantPubKey"
    },
    {
      "name": "participantAddressComputed",
      "op" : "toAddress",
      "from" : "participantErgoTree"
    }
  ],
  "postConditions" : [
    {
      "first": "participantAddressComputed",
      "second": "participantAddress",
      "op" : "Eq"
    }
  ],
  "fee": 10000000
}

Response:
  {
   "tx": {
      "id": "cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe",
      "inputs": [
         {
            "boxId": "5d20b2cfdc0c550bca7183e1b4e21841b14f664e84bd12cf7cb518f7bf7658a3",
            "spendingProof": {
               "proofBytes": "9545812a023e8b39302cee46b06dab44dcee258d6b2d89a2f817bf2d39a671d731c26753c8efc9185aa9a71f3f5abebf0b0173eb5eed4656",
               "extension": {}
            }
         },
         {
            "boxId": "ed9162542005b929aa6ecc66dc0dc4e4237936527bd35bbd56626ec58e93119b",
            "spendingProof": {
               "proofBytes": "0433c89a655ef4aad66d29fe697efe71170a432b2550585d66826f9120eecdb82c1068bd4041f85e3b9b235717877e9e4835f4c192f0b4a8",
               "extension": {}
            }
         }
      ],
      "dataInputs": [],
      "outputs": [
         {
            "boxId": "ee85d8cb16da8873c48a505c920773a099e345ef393ef7a44f016ff8d3f750bf",
            "value": 10000000,
            "ergoTree": "100504000580dac409040004000e20720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dded802d601b2a5dc0c1aa402a7730000d602e4c6a70407ea02d1ededed93e4c672010407720293c27201c2a793db63087201db6308a792c172017301eb02cd7202d1ed938cb2db6308b2a473020073030001730492c17201c1a7",
            "creationHeight": 451104,
            "assets": [
               {
                  "tokenId": "053fefab5477138b760bc7ae666c3e2b324d5ae937a13605cb766ec5222e5518",
                  "amount": 1
               }
            ],
            "additionalRegisters": {
               "R4": "0702725e8878d5198ca7f5853dddf35560ddab05ab0a26adae7e664b84162c9962e5"
            },
            "transactionId": "cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe",
            "index": 0
         },
         {
            "boxId": "32d952bd9ee4b28c4bb3ab74f827e728b3435e49866ce0f70ea16c3d59ae69a9",
            "value": 10000000,
            "ergoTree": "1005040004000e36100204a00b08cd0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798ea02d192a39a8cc7a701730073011001020402d19683030193a38cc7b2a57300000193c2b2a57301007473027303830108cdeeac93b1a57304",
            "creationHeight": 451104,
            "assets": [],
            "additionalRegisters": {},
            "transactionId": "cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe",
            "index": 1
         },
         {
            "boxId": "a2e9107ca4106e6a5d9a5a432b849157febdaacf1bf368334a8a203db4385194",
            "value": 113000000,
            "ergoTree": "0008cd03c843a6cb59a2aebc8bc2c4a2f6fbd526ebb2a1d9f4a90954b5e2128f2d08981d",
            "creationHeight": 451104,
            "assets": [
               {
                  "tokenId": "053fefab5477138b760bc7ae666c3e2b324d5ae937a13605cb766ec5222e5518",
                  "amount": 14
               }
            ],
            "additionalRegisters": {},
            "transactionId": "cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe",
            "index": 2
         }
      ]
   },
   "returned": []
}
   */

  // since we have to create custom boxes (i.e., with custom creation height, etc, we will not use Kiosk's test mockup. We will create our own below

  object HttpClientTesting {
    val responsesDir = "src/test/resources/mockwebserver"
    val addr1 = "9f4QF8AD1nQ3nJahQVkMj8hFSVVzVom77b52JU7EW71Zexg6N8v"

    def loadNodeResponse(name: String) = {
      FileUtil.read(FileUtil.file(s"$responsesDir/node_responses/$name"))
    }

    def loadExplorerResponse(name: String) = {
      FileUtil.read(FileUtil.file(s"$responsesDir/explorer_responses/$name"))
    }

    case class MockData(nodeResponses: Seq[String] = Nil, explorerResponses: Seq[String] = Nil) {
      def appendNodeResponses(moreResponses: Seq[String]): MockData = {
        this.copy(nodeResponses = this.nodeResponses ++ moreResponses)
      }
      def appendExplorerResponses(moreResponses: Seq[String]): MockData = {
        this.copy(explorerResponses = this.explorerResponses ++ moreResponses)
      }
    }

    object MockData {
      def empty = MockData()
    }

    def createMockedErgoClient(folder: String): FileMockedErgoClient = {
      val nodeResponses = IndexedSeq(loadNodeResponse(s"$folder/response_NodeInfo.json"), loadNodeResponse(s"$folder/response_LastHeaders.json"))
      val explorerResponses: Seq[String] = Nil
      new FileMockedErgoClient(nodeResponses.asJava, explorerResponses.asJava)
    }
  }

  val ergoClient1 = HttpClientTesting.createMockedErgoClient("1")

  property("Mock Update") {
    ergoClient1.execute { implicit ctx: BlockchainContext =>
      val pool = new OraclePool {}
      val newPool = new OraclePool {
        override def oracleReward = 5000000 // Nano ergs. One reward per data point to be paid to oracle
      }

      pool.ballotAddress shouldBe "7gak11YHej2wnu7YFDxdNmQibKHrXjysJkcb2wRyq3Kqxcvo75HtchnCtUdL4YxY86NE6kV4m4PsWaPc3gTGLiBDSudMb2K8Ep48eVnJaUotoQmtH9A4mBkTyTaYD12Cogt5iLqPegFQYkLjdsBbPUzoGaNBk2KSZNHqNY23roZ78Ey13mRTSZatdb7jDVU4"
      pool.epochPrepAddress shouldNot be(newPool.epochPrepAddress)

      val onchainBallotBox = ctx
        .newTxBuilder()
        .outBoxBuilder
        .value(10000000)
        .tokens(new ErgoToken(pool.ballotToken, 1))
        .contract(ctx.newContract(ScalaErgoConverters.getAddressFromString(pool.ballotAddress).script))
        .registers(KioskGroupElement(ScalaErgoConverters.stringToGroupElement("02725e8878d5198ca7f5853dddf35560ddab05ab0a26adae7e664b84162c9962e5")).getErgoValue)
        .build()
        .convertToInputWith("cb27ff805834cf7f7568dddce94bf470d92a82f4c189f0eeb25b80fababd9bbe", 0)

      // check that the bootstrapped box has the correct boxId as per JDE output above (in comments)
      onchainBallotBox.getId.getBytes.encodeHex shouldBe "ee85d8cb16da8873c48a505c920773a099e345ef393ef7a44f016ff8d3f750bf"

      // also mock a live oracle pool box
      /*
        https://api.ergoplatform.com/api/v0/transactions/boxes/e8057b02c6547747d05c85a6be2f2152995cfe9a4d866826f141a02e76d48953

        {
          "id": "e8057b02c6547747d05c85a6be2f2152995cfe9a4d866826f141a02e76d48953",
          "txId": "0ce3903041628de4b2707bc40ca5aaeb6c55d455f716bd935da5f7258211bd8c",
          "value": 6786250000,
          "index": 0,
          "creationHeight": 451334,
          "ergoTree": "100904000580ade204040c0e2077dffd47b690caa52fe13345aaf64ecdf7d55f2e7e3496e8206311f491aa46cd04080404040004000e20720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dded806d601b2a5730000d602c67201060ed603e4c6a70504d604c1a7d6059272047301d6069aa37302d1ec95e67202d801d607ed93e47202cbc2a793cbc272017303ecededededededed8fa3720391a39972037304720593db63087201db6308a792c17201720493e4c672010405e4c6a7040593e4c67201050472037207ededededededededed92a37203720593db63087201db6308a792c17201720493db63087201db6308a792c17201720493e4c672010405e4c6a7040592e4c672010504720690e4c6720105049a720673057207edededed93c27201c2a793db63087201db6308a791c17201720493e4c672010405e4c6a7040593e4c6720105047203938cb2db6308b2a4730600730700017308",
          "address": "EfS5abyDe4vKFrJ48K5HnwTqa1ksn238bWFPe84bzVvCGvK1h2B7sgWLETtQuWwzVdBaoRZ1HcyzddrxLcsoM5YEy4UnqcLqMU1MDca1kLw9xbazAM6Awo9y6UVWTkQcS97mYkhkmx2Tewg3JntMgzfLWz5mACiEJEv7potayvk6awmLWS36sJMfXWgnEfNiqTyXNiPzt466cgot3GLcEsYXxKzLXyJ9EfvXpjzC2abTMzVSf1e17BHre4zZvDoAeTqr4igV3ubv2PtJjntvF2ibrDLmwwAyANEhw1yt8C8fCidkf3MAoPE6T53hX3Eb2mp3Xofmtrn4qVgmhNonnV8ekWZWvBTxYiNP8Vu5nc6RMDBv7P1c5rRc3tnDMRh2dUcDD7USyoB9YcvioMfAZGMNfLjWqgYu9Ygw2FokGBPThyWrKQ5nkLJvief1eQJg4wZXKdXWAR7VxwNftdZjPCHcmwn6ByRHZo9kb4Emv3rjfZE",
          "assets": [
            {
              "tokenId": "011d3364de07e5a26f0c4eef0852cddb387039a921b7154ef3cab22c6eda887f",
              "index": 0,
              "amount": 1,
              "name": "ERGUSD-NFT",
              "decimals": 0,
              "type": "EIP-004"
            }
          ],
          "additionalRegisters": {
            "R4": "05a29dfdc303",
            "R5": "04948c37"
          },
          "spentTransactionId": "e05477ef552309f2e49d2b5e416a1ced1d7174e2f518744fe3502a62ca721e63",
          "mainChain": true
        }
       */

      val onchainPoolBox: InputBox = HttpClientTesting.createMockedErgoClient("2").execute { ctx2 =>
        ctx2
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
      }

      onchainPoolBox.getId.getBytes.encodeHex shouldBe "e8057b02c6547747d05c85a6be2f2152995cfe9a4d866826f141a02e76d48953"

      // also mock a live update box
      /*
        https://api.ergoplatform.com/api/v0/transactions/boxes/14679a09294072767077224e45424e8d61a32379e4e242f72c16a96133e8f17b

      {
        "id": "14679a09294072767077224e45424e8d61a32379e4e242f72c16a96133e8f17b",
        "txId": "c67bb073e10c0970fde4fdebd3389ccc490f8cfbb33dbc0963178639dce89633",
        "value": 1000000,
        "index": 0,
        "creationHeight": 449427,
        "ergoTree": "100c04020402040004000e20011d3364de07e5a26f0c4eef0852cddb387039a921b7154ef3cab22c6eda887f0400040004000e20053fefab5477138b760bc7ae666c3e2b324d5ae937a13605cb766ec5222e5518050004000510d805d601b2a4730000d602db63087201d603b2a5730100d604c5a7d605b2a5730200d1edededed938cb27202730300017304ededed937202db6308720393c17201c1720393e4c672010405e4c67203040593e4c672010504e4c672030504937204c5b2a4730500eded93db6308a7db6308720593c2a7c2720592c1a7c1720592b0b5a4d9010663d801d608db63087206ededed91b172087306938cb2720873070001730893e4c67206060e720493e4c67206070ecbc272037309d9010641639a8c7206018cb2db63088c720602730a0002730b",
        "address": "Su1rWdkh3KvNSBUiJDHjtzgeDL5cPyZYbvkpXGSUwehqLwUK6N7Vu4FUUzwajHikSpYNhTFRgabQN9CTNZJHZ53mn9ZY9SNTWVED65gxqGjoJKhQEFnwwNe6MSYxhMqVSy5Tf9qJstj7XwLfwe1skFWvCuLwCmxDDFekEXyqsscKaXuk7LDahpas336EEewxoqB4zsBiuCW7jzQ8uRHL7f7t2CrUVjiuTUe7DL7ihTs5rp2wK9zGUu8CGueQnvU5355wNu8sw42To1unxGJSV6VjrqLbdEcZtXDd2H1UwvVT5JDhDvF2GDfUqjQ7jREq4KaCs3wb6Yk3SCzzNWXsq77dbDGe2hj2svHk1iHUhbExu6vnxhguWrvaLA3HpftqNEBsEgQ59aPi4jjm3dMaqbib6nNh8e3Cs",
        "assets": [
          {
          "tokenId": "720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dde",
          "index": 0,
          "amount": 1,
          "name": "ERGUSD-U-NFT",
          "decimals": 0,
          "type": "EIP-004"
          }
        ],
        "additionalRegisters": {
          "R4": "0e0101"
        },
        "spentTransactionId": null,
        "mainChain": true
      }
       */
      val onchainUpdateBox: InputBox = HttpClientTesting.createMockedErgoClient("3").execute { ctx3 =>
        ctx3
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
      }

      onchainUpdateBox.getId.getBytes.encodeHex shouldBe "14679a09294072767077224e45424e8d61a32379e4e242f72c16a96133e8f17b"

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
