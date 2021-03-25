package oraclepool.v3

import kiosk.encoding.ScalaErgoConverters
import kiosk.ergo.{KioskType, _}
import kiosk.script.ScriptUtil
import scorex.crypto.hash.Blake2b256
import sigmastate.Values

import scala.collection.mutable.{Map => MMap}

trait Contracts {
  def livePeriod: Int // blocks
  def prepPeriod: Int // blocks
  val epochPeriod: Int = livePeriod + prepPeriod

  def buffer: Int // blocks
  def maxDeviation: Int // percent 0 to 100 (what the first and last data point should differ max by)
  def minOracleBoxes: Int // percent 0 to 100

  def poolNFT: String
  def oracleToken: String

  def updateNFT: String
  def ballotToken: String
  def minVotes: Int
  def INF = Long.MaxValue
  def votingBuffer: Int = 1

  def oracleReward: Long // Nano ergs. One reward per data point to be paid to oracle
  def minPoolBoxValue: Long // how much min must exist in oracle pool box
  def minStorageRent: Long

  val env = MMap[String, KioskType[_]]()

  import kiosk.script.ScriptUtil._

  env.setCollByte("oracleTokenId", oracleToken.decodeHex)
  env.setCollByte("poolNFT", poolNFT.decodeHex)

  val liveEpochScript: String =
    s"""{ // This box:
       |  // R4: The latest finalized datapoint (from the previous epoch)
       |  // R5: Block height that the current epoch will finish on
       |  // R6: Address of the "Epoch Preparation" stage contract.
       |
       |  // Oracle box:
       |  // R4: Public key (group element)
       |  // R5: Epoch box Id (this box's Id)
       |  // R6: Data point
       |
       |
       |  val oracleBoxes = CONTEXT.dataInputs.filter{(b:Box) =>
       |    b.R5[Coll[Byte]].get == SELF.id &&
       |    b.tokens(0)._1 == oracleTokenId
       |  }
       |
       |  val pubKey = oracleBoxes.map{(b:Box) => proveDlog(b.R4[GroupElement].get)}(OUTPUTS(1).R4[Int].get)
       |
       |  val sum = oracleBoxes.fold(0L, { (t:Long, b: Box) => t + b.R6[Long].get })
       |
       |  val average = sum / oracleBoxes.size
       |
       |  val firstOracleDataPoint = oracleBoxes(0).R6[Long].get
       |
       |  def getPrevOracleDataPoint(index:Int) = if (index <= 0) firstOracleDataPoint else oracleBoxes(index - 1).R6[Long].get
       |
       |  val rewardAndOrderingCheck = oracleBoxes.fold((1, true), {
       |      (t:(Int, Boolean), b:Box) =>
       |         val currOracleDataPoint = b.R6[Long].get
       |         val prevOracleDataPoint = getPrevOracleDataPoint(t._1 - 1)
       |          
       |         (t._1 + 1, t._2 &&
       |                    OUTPUTS(t._1).propositionBytes == proveDlog(b.R4[GroupElement].get).propBytes &&
       |                    OUTPUTS(t._1).value >= $oracleReward &&
       |                    prevOracleDataPoint >= currOracleDataPoint 
       |         )
       |     }
       |  )
       |
       |  val lastDataPoint = getPrevOracleDataPoint(rewardAndOrderingCheck._1 - 1)
       |  val firstDataPoint = oracleBoxes(0).R6[Long].get
       |  val delta = firstDataPoint * $maxDeviation / 100
       |
       |  val epochPrepScriptHash = SELF.R6[Coll[Byte]].get
       |
       |  sigmaProp(
       |    blake2b256(OUTPUTS(0).propositionBytes) == epochPrepScriptHash &&
       |    oracleBoxes.size >= $minOracleBoxes &&
       |    OUTPUTS(0).tokens == SELF.tokens &&
       |    OUTPUTS(0).R4[Long].get == average &&
       |    OUTPUTS(0).R5[Int].get == SELF.R5[Int].get + $epochPeriod &&
       |    OUTPUTS(0).value >= SELF.value - (oracleBoxes.size + 1) * $oracleReward &&
       |    rewardAndOrderingCheck._2 &&
       |    lastDataPoint >= firstDataPoint - delta
       |  ) && pubKey
       |}
       |""".stripMargin

  env.setCollByte("updateNFT", updateNFT.decodeHex)

  val epochPrepScript: String =
    s"""
       |{
       |  // This box:
       |  // R4: The finalized data point from collection
       |  // R5: Height the epoch will end
       |
       |  val canStartEpoch = HEIGHT > SELF.R5[Int].get - $livePeriod
       |  val epochNotOver = HEIGHT < SELF.R5[Int].get
       |  val epochOver = HEIGHT >= SELF.R5[Int].get
       |  val enoughFunds = SELF.value >= $minPoolBoxValue
       |
       |  val maxNewEpochHeight = HEIGHT + $epochPeriod + $buffer
       |  val minNewEpochHeight = HEIGHT + $epochPeriod
       |
       |  val poolAction = if (OUTPUTS(0).R6[Coll[Byte]].isDefined) {
       |    val isliveEpochOutput = OUTPUTS(0).R6[Coll[Byte]].get == blake2b256(SELF.propositionBytes) &&
       |                            blake2b256(OUTPUTS(0).propositionBytes) == liveEpochScriptHash
       |    ( // start next epoch
       |      epochNotOver && canStartEpoch && enoughFunds &&
       |      OUTPUTS(0).tokens == SELF.tokens &&
       |      OUTPUTS(0).value >= SELF.value &&
       |      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
       |      OUTPUTS(0).R5[Int].get == SELF.R5[Int].get &&
       |      isliveEpochOutput
       |    ) || ( // create new epoch
       |      epochOver &&
       |      enoughFunds &&
       |      OUTPUTS(0).tokens == SELF.tokens &&
       |      OUTPUTS(0).value >= SELF.value &&
       |      OUTPUTS(0).tokens == SELF.tokens &&
       |      OUTPUTS(0).value >= SELF.value &&
       |      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
       |      OUTPUTS(0).R5[Int].get >= minNewEpochHeight &&
       |      OUTPUTS(0).R5[Int].get <= maxNewEpochHeight &&
       |      isliveEpochOutput
       |    )
       |  } else {
       |    ( // collect funds
       |      OUTPUTS(0).propositionBytes == SELF.propositionBytes &&
       |      OUTPUTS(0).tokens == SELF.tokens &&
       |      OUTPUTS(0).value > SELF.value &&
       |      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
       |      OUTPUTS(0).R5[Int].get == SELF.R5[Int].get 
       |    )
       |  }
       |  
       |  val updateAction = INPUTS(0).tokens(0)._1 == updateNFT 
       |  
       |  sigmaProp(poolAction || updateAction)
       |}
       |""".stripMargin

  val dataPointScript: String =
    s"""
       |{
       |  // This box:
       |  // R4: The address of the oracle (never allowed to change after bootstrap).
       |  // R5: The box id of the latest Live Epoch box.
       |  // R6: The oracle's datapoint.
       |
       |  val pubKey = SELF.R4[GroupElement].get
       |
       |  val poolBox = CONTEXT.dataInputs(0)
       |
       |  // Allow datapoint box to contain box id of any box with pool NFT (i.e., either Live Epoch or Epoch Prep boxes)
       |  // Earlier we additionally required that the box have the live epoch script.
       |  // In summary:
       |  //    Earlier: (1st data-input has pool NFT) && (1st data-input has live epoch script) 
       |  //    Now:     (1st data-input has pool NFT) 
       |  //
       |  val validPoolBox = poolBox.tokens(0)._1 == poolNFT 
       | 
       |  sigmaProp(
       |    OUTPUTS(0).R4[GroupElement].get == pubKey &&
       |    OUTPUTS(0).R5[Coll[Byte]].get == poolBox.id &&
       |    OUTPUTS(0).R6[Long].get > 0 &&
       |    OUTPUTS(0).propositionBytes == SELF.propositionBytes &&
       |    OUTPUTS(0).tokens == SELF.tokens &&
       |    validPoolBox
       |  ) && proveDlog(pubKey)
       |}
       |""".stripMargin

  val poolDepositScript: String =
    s"""
       |{
       |  val allFundingBoxes = INPUTS.filter{(b:Box) =>
       |    b.propositionBytes == SELF.propositionBytes
       |  }
       |
       |  val totalFunds = allFundingBoxes.fold(0L, { (t:Long, b: Box) => t + b.value })
       |
       |  sigmaProp(
       |    INPUTS(0).tokens(0)._1 == poolNFT &&
       |    OUTPUTS(0).propositionBytes == INPUTS(0).propositionBytes &&
       |    OUTPUTS(0).value >= INPUTS(0).value + totalFunds &&
       |    OUTPUTS(0).tokens == INPUTS(0).tokens
       |  )
       |}
       |""".stripMargin

  env.setCollByte("ballotTokenId", ballotToken.decodeHex)

  val ballotScript =
    s"""{ // This box (ballot box):
       |  // R4 the group element of the owner of the ballot token [GroupElement]
       |  // R5 the box id of the update box [Coll[Byte]]
       |  // R6 the value voted for [Coll[Byte]]
       |  
       |  val pubKey = SELF.R4[GroupElement].get
       |  
       |  val output = OUTPUTS(0)
       |  
       |  // Suggestion: Remove the requirement of isBasicCopy and make the voting tokens "free" assets (in contrast to bounded assets)
       |  val isBasicCopy = output.R4[GroupElement].get == pubKey && 
       |                    output.propositionBytes == SELF.propositionBytes &&
       |                    output.tokens == SELF.tokens && 
       |                    output.value == SELF.value
       |  
       |  sigmaProp(
       |    proveDlog(pubKey) &&
       |    output.creationInfo._1 == HEIGHT &&
       |    SELF.creationInfo._1 < HEIGHT &&
       |    isBasicCopy  
       |  )
       |}
       |""".stripMargin

  val updateScript =
    s"""{ // This box (update box):
       |  // Registers empty 
       |  // 
       |  // ballot boxes (Data Inputs)
       |  // R4 the pub key of voter [GroupElement] (not used here)
       |  // R5 the box id of this box [Coll[Byte]]
       |  // R6 the value voted for [Coll[Byte]]
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
       |  def isBallot(b: Box) = b.tokens.size > 0 && 
       |                         b.tokens(0)._1 == ballotTokenId &&  
       |                         b.R5[Coll[Byte]].get == SELF.id && // ensure vote corresponds to this box ****
       |                         b.R6[Coll[Byte]].get == poolBoxOutHash  
       |  
       |  val ballots = CONTEXT.dataInputs.filter(isBallot)
       |  
       |  def toLong(array: Coll[Byte]) = byteArrayToLong(array.slice(0, 8))
       |  
       |  val preBoxId = toLong(ballots(0).id) + 1
       |
       |  val orderCheck = ballots.fold((preBoxId, true), { (t: (Long, Boolean), box: Box) =>
       |       val currBoxId = toLong(box.id)
       |       val oldBoxId = t._1
       |       val valid = t._2
       |       (currBoxId, valid && oldBoxId > currBoxId)
       |    }
       |  )
       |  
       |  val uniqueDataInputs = orderCheck._2
       |  
       |  val votesCount = ballots.fold(0L, { (accum:Long, box:Box) => accum + box.tokens(0)._2 } )
       |  
       |  sigmaProp(validPoolIn && validPoolOut && validUpdateIn && validUpdateOut && votesCount >= $minVotes)
       |}
       |""".stripMargin

  import ScalaErgoConverters._

  val liveEpochErgoTree: Values.ErgoTree = ScriptUtil.compile(env.toMap, liveEpochScript)
  env.setCollByte("liveEpochScriptHash", Blake2b256(liveEpochErgoTree.bytes))
  val epochPrepErgoTree: Values.ErgoTree = ScriptUtil.compile(env.toMap, epochPrepScript)
  val dataPointErgoTree: Values.ErgoTree = ScriptUtil.compile(env.toMap, dataPointScript)
  env.setCollByte("epochPrepScriptHash", Blake2b256(epochPrepErgoTree.bytes))
  val poolDepositErgoTree: Values.ErgoTree = ScriptUtil.compile(env.toMap, poolDepositScript)
  val updateErgoTree: Values.ErgoTree = ScriptUtil.compile(env.toMap, updateScript)
  val ballotErgoTree: Values.ErgoTree = ScriptUtil.compile(env.toMap, ballotScript)

  val liveEpochAddress: String = getStringFromAddress(getAddressFromErgoTree(liveEpochErgoTree))
  val epochPrepAddress: String = getStringFromAddress(getAddressFromErgoTree(epochPrepErgoTree))
  val dataPointAddress: String = getStringFromAddress(getAddressFromErgoTree(dataPointErgoTree))
  val poolDepositAddress: String = getStringFromAddress(getAddressFromErgoTree(poolDepositErgoTree))
  val updateAddress: String = getStringFromAddress(getAddressFromErgoTree(updateErgoTree))
  val ballotAddress: String = getStringFromAddress(getAddressFromErgoTree(ballotErgoTree))

  val epochPrepScriptHash = Blake2b256(epochPrepErgoTree.bytes).encodeHex
  val liveEpochScriptHash = Blake2b256(liveEpochErgoTree.bytes).encodeHex
}
