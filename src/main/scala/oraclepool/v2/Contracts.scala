package oraclepool.v2

import kiosk.encoding.ScalaErgoConverters.{getAddressFromErgoTree, getStringFromAddress}
import kiosk.ergo._
import kiosk.script.ScriptUtil
import scorex.util.encode.Base64
import sigmastate.Values

case class PoolConfig(epochLength: Int, // blocks
                      minDataPoints: Int,
                      buffer: Int, // the possible delay in which a new pool box gets created, example 4
                      maxDeviationPercent: Int, // the max deviation in percent from first to last data point, example 5
                      poolNFT: String,
                      refreshNFT: String,
                      oracleTokenId: String,
                      updateNFT: String,
                      ballotTokenId: String,
                      minVotes: Int,
                      minStorageRent: Long)

class Contracts(val config: PoolConfig) {
  import config._

  val poolScript =
    s"""
       |{
       |  // This box (pool box)
       |  //   epoch start height is stored in creation Height (R3)
       |  //   R4 Current data point (Long)
       |  //   R5 Current epoch counter (Int)
       |  //   tokens(0) pool token (NFT)
       |  
       |  val otherTokenId = INPUTS(1).tokens(0)._1
       |  val refreshNFT = fromBase64("${Base64.encode(refreshNFT.decodeHex)}") // TODO replace with actual
       |  val updateNFT = fromBase64("${Base64.encode(updateNFT.decodeHex)}") // TODO replace with actual
       |
       |  sigmaProp(otherTokenId == refreshNFT || otherTokenId == updateNFT)
       |}
       |""".stripMargin

  val refreshScript: String =
    s"""
       |{ // This box (refresh box)
       |  //   tokens(0) reward tokens to be emitted (several) 
       |  //   
       |  //   When initializing the box, there must be one reward token. When claiming reward, one token must be left unclaimed   
       |  
       |  val oracleTokenId = fromBase64("${Base64.encode(oracleTokenId.decodeHex)}") // TODO replace with actual
       |  val poolNFT = fromBase64("${Base64.encode(poolNFT.decodeHex)}") // TODO replace with actual 
       |  val epochLength = $epochLength // TODO replace with actual
       |  val minDataPoints = $minDataPoints // TODO replace with actual
       |  val buffer = $buffer // TODO replace with actual
       |  val maxDeviationPercent = $maxDeviationPercent // percent // TODO replace with actual
       |
       |  val minStartHeight = HEIGHT - epochLength
       |  val spenderIndex = getVar[Int](0).get // the index of the data-point box (NOT input!) belonging to spender    
       |    
       |  val poolIn = INPUTS(0)
       |  val poolOut = OUTPUTS(0)
       |  val selfOut = OUTPUTS(1)
       |
       |  def isValidDataPoint(b: Box) = if (b.R6[Long].isDefined) {
       |    b.creationInfo._1    >= minStartHeight &&  // data point must not be too old
       |    b.tokens(0)._1       == oracleTokenId  && // first token id must be of oracle token
       |    b.R5[Int].get        == poolIn.R5[Int].get // it must correspond to this epoch
       |  } else false 
       |          
       |  val dataPoints = INPUTS.filter(isValidDataPoint)    
       |  val pubKey = dataPoints(spenderIndex).R4[GroupElement].get
       |
       |  val enoughDataPoints = dataPoints.size >= minDataPoints    
       |  val rewardEmitted = dataPoints.size * 2 // one extra token for each collected box as reward to collector   
       |  val epochOver = poolIn.creationInfo._1 < minStartHeight
       |       
       |  val startData = 1L // we don't allow 0 data points
       |  val startSum = 0L 
       |  // we expect data-points to be sorted in INCREASING order
       |  
       |  val lastSortedSum = dataPoints.fold((startData, (true, startSum)), {
       |        (t: (Long, (Boolean, Long)), b: Box) =>
       |           val currData = b.R6[Long].get
       |           val prevData = t._1
       |           val wasSorted = t._2._1 
       |           val oldSum = t._2._2
       |           val newSum = oldSum + currData  // we don't have to worry about overflow, as it causes script to fail
       |
       |           val isSorted = wasSorted && prevData <= currData 
       |
       |           (currData, (isSorted, newSum))
       |    }
       |  )
       | 
       |  val lastData = lastSortedSum._1
       |  val isSorted = lastSortedSum._2._1
       |  val sum = lastSortedSum._2._2
       |  val average = sum / dataPoints.size 
       |
       |  val maxDelta = lastData * maxDeviationPercent / 100          
       |  val firstData = dataPoints(0).R6[Long].get
       |
       |  proveDlog(pubKey)                                             &&
       |  epochOver                                                     && 
       |  enoughDataPoints                                              &&    
       |  isSorted                                                      &&
       |  lastData - firstData     <= maxDelta                          && 
       |  poolIn.tokens(0)._1      == poolNFT                           &&
       |  poolOut.tokens           == poolIn.tokens                     && // preserve pool tokens
       |  poolOut.R4[Long].get     == average                           && // rate
       |  poolOut.R5[Int].get      == poolIn.R5[Int].get + 1            && // counter
       |  poolOut.propositionBytes == poolIn.propositionBytes           && // preserve pool script
       |  poolOut.value            >= poolIn.value                      &&
       |  poolOut.creationInfo._1  >= HEIGHT - buffer                   && // ensure that new box has correct start epoch height
       |  selfOut.tokens(0)        == SELF.tokens(0)                    && // refresh NFT preserved
       |  selfOut.tokens(1)._1     == SELF.tokens(1)._1                 && // reward token id preserved
       |  selfOut.tokens(1)._2     >= SELF.tokens(1)._2 - rewardEmitted && // reward token amount correctly reduced
       |  selfOut.propositionBytes == SELF.propositionBytes             && // script preserved
       |  selfOut.value            >= SELF.value                       
       |}
       |""".stripMargin

  val oracleScript: String =
    s"""
       |{ // This box (oracle box)
       |  //   R4 public key (GroupElement) 
       |  //   R5 epoch counter of current epoch (Int)
       |  //   R6 data point (Long) or empty
       |
       |  //   tokens(0) oracle token (one)
       |  //   tokens(1) reward tokens collected (one or more) 
       |  //   
       |  //   When publishing a datapoint, there must be at least one reward token at index 1 
       |  //  
       |  //   We will connect this box to pool NFT in input #0 (and not the refresh NFT in input #1)
       |  //   This way, we can continue to use the same box after updating pool
       |  //   This *could* allow the oracle box to be spent during an update
       |  //   However, this is not an issue because the update contract ensures that tokens and registers (except script) of the pool box are preserved
       |
       |  //   Private key holder can do following things:
       |  //     1. Change group element (public key) stored in R4
       |  //     2. Store any value of type in or delete any value from R4 to R9 
       |  //     3. Store any token or none at 2nd index 
       |
       |  //   In order to connect this oracle box to a different refreshNFT after an update, 
       |  //   the oracle should keep at least one new reward token at index 1 when publishing data-point
       |  
       |  val poolNFT = fromBase64("${Base64.encode(poolNFT.decodeHex)}") // TODO replace with actual 
       |  
       |  val otherTokenId = INPUTS(0).tokens(0)._1
       |  
       |  val minStorageRent = ${minStorageRent}L
       |  val selfPubKey = SELF.R4[GroupElement].get
       |  val outIndex = getVar[Int](0).get
       |  val output = OUTPUTS(outIndex)
       |  
       |  val isSimpleCopy = output.tokens(0) == SELF.tokens(0)                && // oracle token is preserved
       |                     output.propositionBytes == SELF.propositionBytes  && // script preserved
       |                     output.R4[GroupElement].isDefined                 && // output must have a public key (not necessarily the same)
       |                     output.value >= minStorageRent                       // ensure sufficient Ergs to ensure no garbage collection
       |                     
       |  val collection = otherTokenId == poolNFT                    && // first input must be pool box
       |                   output.tokens(1)._1 == SELF.tokens(1)._1   && // reward tokenId is preserved (oracle should ensure this contains a reward token)
       |                   output.tokens(1)._2 > SELF.tokens(1)._2    && // at least one reward token must be added 
       |                   output.R4[GroupElement].get == selfPubKey  && // for collection preserve public key
       |                   output.value >= SELF.value                 && // nanoErgs value preserved
       |                   ! (output.R5[Any].isDefined)                  // no more registers; prevents box from being reused as a valid data-point
       |
       |  val owner = proveDlog(selfPubKey)  
       |
       |  // owner can choose to transfer to another public key by setting different value in R4
       |  isSimpleCopy && (owner || collection) 
       |}
       |""".stripMargin

  val ballotScript =
    s"""{ // This box (ballot box):
       |  // R4 the group element of the owner of the ballot token [GroupElement]
       |  // R5 the creation height of the update box [Int]
       |  // R6 the value voted for [Coll[Byte]]
       |
       |  val updateNFT = fromBase64("${Base64.encode(updateNFT.decodeHex)}") // TODO replace with actual 
       |
       |  val minStorageRent = ${minStorageRent}L  // TODO replace with actual
       |  
       |  val selfPubKey = SELF.R4[GroupElement].get
       |  val otherTokenId = INPUTS(1).tokens(0)._1
       |  
       |  val outIndex = getVar[Int](0).get
       |  val output = OUTPUTS(outIndex)
       |  
       |  val isSimpleCopy = output.R4[GroupElement].isDefined                && // ballot boxes are transferable by setting different value here 
       |                     output.propositionBytes == SELF.propositionBytes &&
       |                     output.tokens == SELF.tokens                     && 
       |                     output.value >= minStorageRent 
       |  
       |  val update = otherTokenId == updateNFT                 && // can only update when update box is the 2nd input
       |               output.R4[GroupElement].get == selfPubKey && // public key is preserved
       |               output.value >= SELF.value                && // value preserved or increased
       |               ! (output.R5[Any].isDefined)                 // no more registers; prevents box from being reused as a valid vote 
       |  
       |  val owner = proveDlog(selfPubKey)
       |  
       |  // unlike in collection, here we don't require spender to be one of the ballot token holders
       |  isSimpleCopy && (owner || update)
       |}
       |""".stripMargin

  val updateScript =
    s"""{ // This box (update box):
       |  // Registers empty 
       |  // 
       |  // ballot boxes (Inputs)
       |  // R4 the pub key of voter [GroupElement] (not used here)
       |  // R5 the creation height of this box [Int]
       |  // R6 the value voted for [Coll[Byte]] (hash of the new pool box script)
       |
       |  val poolNFT = fromBase64("${Base64.encode(poolNFT.decodeHex)}") // TODO replace with actual 
       |
       |  val ballotTokenId = fromBase64("${Base64.encode(ballotTokenId.decodeHex)}") // TODO replace with actual 
       |
       |  val minVotes = ${config.minVotes} // TODO replace with actual
       |  
       |  val poolIn = INPUTS(0) // pool box is 1st input
       |  val poolOut = OUTPUTS(0) // copy of pool box is the 1st output
       |
       |  val updateBoxOut = OUTPUTS(1) // copy of this box is the 2nd output
       |
       |  // compute the hash of the pool output box. This should be the value voted for
       |  val poolOutHash = blake2b256(poolOut.propositionBytes)
       |  
       |  val validPoolIn = poolIn.tokens(0)._1 == poolNFT
       |  
       |  val validPoolOut = poolIn.propositionBytes != poolOut.propositionBytes  && // script should not be preserved
       |                     poolIn.tokens == poolOut.tokens                      && // tokens preserved
       |                     poolIn.creationInfo._1 == poolOut.creationInfo._1    && // creation height preserved
       |                     poolIn.value == poolOut.value                        && // value preserved 
       |                     poolIn.R4[Long] == poolOut.R4[Long]                  && // rate preserved  
       |                     poolIn.R5[Int] == poolOut.R5[Int]                    && // counter preserved
       |                     ! (poolOut.R6[Any].isDefined)
       |
       |  
       |  val validUpdateOut = updateBoxOut.tokens == SELF.tokens                     &&
       |                       updateBoxOut.propositionBytes == SELF.propositionBytes &&
       |                       updateBoxOut.value >= SELF.value                       &&
       |                       updateBoxOut.creationInfo._1 > SELF.creationInfo._1    &&
       |                       ! (updateBoxOut.R4[Any].isDefined) 
       |
       |  def isValidBallot(b:Box) = if (b.tokens.size > 0) {
       |    b.tokens(0)._1 == ballotTokenId       &&
       |    b.R5[Int].get == SELF.creationInfo._1 && // ensure vote corresponds to this box by checking creation height
       |    b.R6[Coll[Byte]].get == poolOutHash      // check value voted for
       |  } else false
       |  
       |  val ballotBoxes = INPUTS.filter(isValidBallot)
       |  
       |  val votesCount = ballotBoxes.fold(0L, {(accum: Long, b: Box) => accum + b.tokens(0)._2})
       |  
       |  sigmaProp(validPoolIn && validPoolOut && validUpdateOut && votesCount >= minVotes)  
       |}
       |""".stripMargin

  val poolErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), poolScript)
  val refreshErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), refreshScript)
  val oracleErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), oracleScript)
  val updateErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), updateScript)
  val ballotErgoTree: Values.ErgoTree = ScriptUtil.compile(Map(), ballotScript)

  val poolAddress: String = getStringFromAddress(getAddressFromErgoTree(poolErgoTree))
  val refreshAddress: String = getStringFromAddress(getAddressFromErgoTree(refreshErgoTree))
  val oracleAddress: String = getStringFromAddress(getAddressFromErgoTree(oracleErgoTree))
  val updateAddress: String = getStringFromAddress(getAddressFromErgoTree(updateErgoTree))
  val ballotAddress: String = getStringFromAddress(getAddressFromErgoTree(ballotErgoTree))
}
