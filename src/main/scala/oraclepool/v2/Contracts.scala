package oraclepool.v2

import kiosk.encoding.ScalaErgoConverters
import kiosk.ergo._
import kiosk.script.ScriptUtil
import scorex.util.encode.Base64
import sigmastate.Values

object Contracts {
  val epochLength = 30 // 30 blocks in 1 hour
  val minDataPoints = 4
  val buffer = 4 // the possible delay in which a new pool box gets created, example 4
  val maxDeviationPercent = 5 // the max deviation in percent from first to last data point, example 5
  val dataPointTokenId = "8c27dd9d8a35aac1e3167d58858c0a8b4059b277da790552e37eba22df9b9035"
  val updateNFT = "720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dde"

  val poolScript: String =
    s"""
       |{ // This box (pool box)
       |  //   R4 Current data point (Long)
       |  //   epoch start height is stored in creation Height (R3)
       |  //
       |  //   tokens(0) pool token (NFT)
       |  //   tokens(1) reward tokens to be emitted (several)
       |  //
       |  //   When initializing the box, there must be one reward token. When claiming reward, one token must be left unclaimed
       |  //
       |
       |  val dataPointTokenId = fromBase64("${Base64.encode(dataPointTokenId.decodeHex)}") // TODO replace with actual
       |  val updateNFT = fromBase64("${Base64.encode(updateNFT.decodeHex)}") // TODO replace with actual
       |  val minDataPoints = $minDataPoints
       |  val buffer = $buffer
       |  val maxDeviationPercent = $maxDeviationPercent
       |  val epochLength = $epochLength
       |
       |  val poolAction = if (getVar[Any](0).isDefined) {
       |    val spenderIndex = getVar[Int](0).get // the index of the data-point box (NOT input!) belonging to spender
       |
       |    val minStartHeight = HEIGHT - epochLength
       |    val rewardTokens = SELF.tokens(1)
       |
       |    def isValidDataPoint(b: Box) = if (b.R5[Any].isDefined) {
       |      b.creationInfo._1    >= minStartHeight &&  // data point must not be too old
       |      b.tokens(0)._1       == dataPointTokenId && // first token id must be of participant token
       |      b.R5[Coll[Byte]].get == SELF.id // it must correspond to this epoch
       |    } else false
       |
       |    val dataPoints = INPUTS.filter(isValidDataPoint)
       |    val pubKey = dataPoints(spenderIndex).R6[GroupElement].get
       |
       |    val output = OUTPUTS(0)
       |
       |    val enoughDataPoints = dataPoints.size >= minDataPoints
       |    val rewardEmitted = dataPoints.size * 2 // one extra token for each collected box as reward to collector
       |    val epochOver = SELF.creationInfo._1 <= minStartHeight
       |
       |    val startData = 1L // we don't allow 0 data points
       |    val startSum = 0L
       |    // we expected datapoints to be sorted in INCREASING order
       |
       |    val lastSortedSum = dataPoints.fold((startData, (true, startSum)), {
       |        (t: (Long, (Boolean, Long)), b: Box) =>
       |           val currData = b.R4[Long].get
       |           val prevData = t._1
       |           val wasSorted = t._2._1
       |           val oldSum = t._2._2
       |           val newSum = oldSum + currData  // we don't have to worry about overflow, as it causes script to fail
       |
       |           val isSorted = wasSorted && prevData <= currData
       |
       |           (currData, (isSorted, newSum))
       |      }
       |    )
       |
       |    val lastData = lastSortedSum._1
       |    val isSorted = lastSortedSum._2._1
       |    val sum = lastSortedSum._2._2
       |    val average = sum / dataPoints.size
       |
       |    val maxDelta = lastData * maxDeviationPercent / 100
       |    val firstData = dataPoints(0).R4[Long].get
       |
       |    sigmaProp(proveDlog(pubKey))                                 &&
       |    enoughDataPoints                                             &&
       |    isSorted                                                     &&
       |    lastData - firstData    <= maxDelta                          &&
       |    output.R4[Long].get     == average                           &&
       |    output.tokens(0)        == SELF.tokens(0)                    && // pool NFT preserved
       |    output.tokens(1)._1     == SELF.tokens(1)._1                 && // reward token id preserved
       |    output.tokens(1)._2     == SELF.tokens(1)._2 - rewardEmitted && // reward token amount correctly reduced
       |    output.tokens.size      == 2                                 && // no more tokens
       |    output.propositionBytes == SELF.propositionBytes             && // script preserved
       |    output.value            >= SELF.value                        && // Ergs preserved &&
       |    output.creationInfo._1  >= HEIGHT - buffer                      // ensure that new box has correct start epoch height
       |  } else false
       |
       |  val updateAction = INPUTS(0).tokens(0)._1 == updateNFT
       |
       |  sigmaProp(poolAction || updateAction)
       |}
       |""".stripMargin

  val poolNFT = "011d3364de07e5a26f0c4eef0852cddb387039a921b7154ef3cab22c6eda887f" // 1 token issued

  val minStorageRent = 10000000

  val dataPointScript: String =
    s"""
       |{ // This box (participant box)
       |  //   R4 data point
       |  //   R5 box id of pool box
       |  //   R6 public key
       |  //
       |  //   tokens(0) participant token (one)
       |  //   tokens(1) reward tokens collected (one or more)
       |  //
       |  //   When initializing the box, there must be one reward token. When claiming reward, one token must be left unclaimed
       |
       |  val poolNFT = fromBase64("${Base64.encode(poolNFT.decodeHex)}") // TODO replace with actual
       |  val minStorageRent = $minStorageRent
       |  val selfPubKey = SELF.R6[GroupElement].get
       |  val selfIndex = getVar[Int](0).get
       |  val output = OUTPUTS(selfIndex)
       |  val outPubKey = output.R6[GroupElement].get   // output must have a public key (not necessarily the same)
       |
       |  val isSimpleCopy = output.tokens(0) == SELF.tokens(0) && // participant token is preserved
       |                     output.tokens(1)._1 == SELF.tokens(1)._1 && // reward tokenId is preserved
       |                     output.tokens.size == 2 && // exactly two token types
       |                     output.propositionBytes == SELF.propositionBytes && // script preserved
       |                     output.R7[Any].isDefined == false // no more registers
       |
       |  val collection = INPUTS(0).tokens(0)._1 == poolNFT && // first input must be pool box
       |                   output.tokens(1)._2 > SELF.tokens(1)._2 && // at least one reward token must be added
       |                   outPubKey == selfPubKey &&
       |                   output.value >= SELF.value // nanoErgs value preserved
       |
       |  val owner = proveDlog(selfPubKey) &&
       |              output.value >= minStorageRent
       |
       |  // owner can choose to transfer to another public key by setting different value in R6
       |  isSimpleCopy && (owner || collection)
       |}
       |""".stripMargin

  val ballotScript =
    s"""{ // This box (ballot box):
       |  // R4 the group element of the owner of the ballot token [GroupElement]
       |  // R5 dummy Int due to AOTC non-lazy evaluation (since pool box has Int at R5). Due to the line marked ****
       |  // R6 the box id of the update box [Coll[Byte]]
       |  // R7 the value voted for [Coll[Byte]]
       |
       |  val updateNFT = fromBase64("${Base64.encode(updateNFT.decodeHex)}") // TODO replace with actual
       |
       |  val pubKey = SELF.R4[GroupElement].get
       |
       |  val index = INPUTS.indexOf(SELF, 0)
       |
       |  val output = OUTPUTS(index)
       |
       |  val isBasicCopy = output.R4[GroupElement].get == pubKey &&
       |                    output.propositionBytes == SELF.propositionBytes &&
       |                    output.tokens == SELF.tokens &&
       |                    output.value >= $minStorageRent
       |
       |  sigmaProp(
       |    isBasicCopy && (
       |      proveDlog(pubKey) || (
       |         INPUTS(0).tokens(0)._1 == updateNFT &&
       |         output.value >= SELF.value
       |      )
       |    )
       |  )
       |}
       |""".stripMargin

  lazy val ballotToken = "053fefab5477138b760bc7ae666c3e2b324d5ae937a13605cb766ec5222e5518" // 15 tokens issued

  lazy val minVotes = 6

  val updateScript =
    s"""{ // This box (update box):
       |  // Registers empty
       |  //
       |  // ballot boxes (Inputs)
       |  // R4 the pub key of voter [GroupElement] (not used here)
       |  // R5 dummy int due to AOTC non-lazy evaluation (from the line marked ****)
       |  // R6 the box id of this box [Coll[Byte]]
       |  // R7 the value voted for [Coll[Byte]]
       |
       |  val poolNFT = fromBase64("${Base64.encode(poolNFT.decodeHex)}") // TODO replace with actual
       |
       |  val ballotTokenId = fromBase64("${Base64.encode(ballotToken.decodeHex)}") // TODO replace with actual
       |
       |  // collect and update in one step
       |  val updateBoxOut = OUTPUTS(0) // copy of this box is the 1st output
       |  val validUpdateIn = SELF.id == INPUTS(0).id // this is 1st input
       |
       |  val poolBoxIn = INPUTS(1) // pool box is 2nd input
       |  val poolBoxOut = OUTPUTS(1) // copy of pool box is the 2nd output
       |
       |  // compute the hash of the pool output box. This should be the value voted for
       |  val poolBoxOutHash = blake2b256(poolBoxOut.propositionBytes)
       |
       |  val validPoolIn = poolBoxIn.tokens(0)._1 == poolNFT
       |  val validPoolOut = poolBoxIn.tokens == poolBoxOut.tokens &&
       |                     poolBoxIn.value == poolBoxOut.value &&
       |                     poolBoxIn.R4[Long].get == poolBoxOut.R4[Long].get &&
       |                     poolBoxIn.R5[Int].get == poolBoxOut.R5[Int].get
       |
       |
       |  val validUpdateOut = SELF.tokens == updateBoxOut.tokens &&
       |                       SELF.propositionBytes == updateBoxOut.propositionBytes &&
       |                       SELF.value >= updateBoxOut.value
       |
       |  def isValidBallot(b:Box) = {
       |    b.tokens.size > 0 &&
       |    b.tokens(0)._1 == ballotTokenId &&
       |    b.R6[Coll[Byte]].get == SELF.id && // ensure vote corresponds to this box ****
       |    b.R7[Coll[Byte]].get == poolBoxOutHash // check value voted for
       |  }
       |
       |  val ballotBoxes = INPUTS.filter(isValidBallot)
       |
       |  val votesCount = ballotBoxes.fold(0L, {(accum: Long, b: Box) => accum + b.tokens(0)._2})
       |
       |  sigmaProp(validPoolIn && validPoolOut && validUpdateIn && validUpdateOut && votesCount >= $minVotes)
       |}
       |""".stripMargin

  import ScalaErgoConverters._

  val poolErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), poolScript)
  val dataPointErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), dataPointScript)
  val updateErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), updateScript)
  val ballotErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), ballotScript)

  val poolAddress: String = getStringFromAddress(
    getAddressFromErgoTree(poolErgoTree))
  val dataPointAddress: String = getStringFromAddress(getAddressFromErgoTree(dataPointErgoTree))
  val updateAddress: String = getStringFromAddress(getAddressFromErgoTree(updateErgoTree))
  val ballotAddress: String = getStringFromAddress(getAddressFromErgoTree(ballotErgoTree))
}