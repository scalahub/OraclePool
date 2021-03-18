package oraclepool

import kiosk.encoding.ScalaErgoConverters
import kiosk.ergo.KioskType
import kiosk.script.ScriptUtil
import scorex.crypto.hash.Blake2b256
import sigmastate.Values

import scala.collection.mutable.{Map => MMap}
import kiosk.ergo._

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
       |  val oracleBoxes = INPUTS.filter{(b:Box) =>
       |    b.R5[Coll[Byte]].get == SELF.id &&
       |    b.tokens(0)._1 == oracleTokenId
       |  }
       |
       |  val pubKey = oracleBoxes.map{(b:Box) => proveDlog(b.R4[GroupElement].get)}(OUTPUTS(1).R5[Int].get) // 1st output will be a datapoint box, and its R4 is locked to public key. Hence we will use R5
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
       |         (t._1 + 1, t._2 &&
       |                    OUTPUTS(t._1).propositionBytes == b.propositionBytes &&
       |                    OUTPUTS(t._1).R4[GroupElement].get == b.R4[GroupElement].get &&
       |                    OUTPUTS(t._1).value >= b.value + $oracleReward &&
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
       |      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
       |      OUTPUTS(0).R5[Int].get == SELF.R5[Int].get &&
       |      OUTPUTS(0).tokens == SELF.tokens &&
       |      OUTPUTS(0).value >= SELF.value &&
       |      isliveEpochOutput
       |    ) || ( // create new epoch
       |      epochOver &&
       |      enoughFunds &&
       |      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
       |      OUTPUTS(0).R5[Int].get >= minNewEpochHeight &&
       |      OUTPUTS(0).R5[Int].get <= maxNewEpochHeight &&
       |      OUTPUTS(0).tokens == SELF.tokens &&
       |      OUTPUTS(0).value >= SELF.value &&
       |      isliveEpochOutput
       |    )
       |  } else {
       |    ( // collect funds
       |      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
       |      OUTPUTS(0).R5[Int].get == SELF.R5[Int].get &&
       |      OUTPUTS(0).propositionBytes == SELF.propositionBytes &&
       |      OUTPUTS(0).tokens == SELF.tokens &&
       |      OUTPUTS(0).value > SELF.value
       |    )
       |  }
       |  
       |  val updateAction = INPUTS(1).tokens(0)._1 == updateNFT && CONTEXT.dataInputs.size == 0
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
       |  // We will NOT have any box in data inputs during commit. So the owner is free to put any value into the registers. 
       |  // The requirement of R5 having box id is checked in the collect datapoint action
       |  // Specifically the datapoint collection will check
       |  //  1. SELF.R5[Coll[Byte]].get == liveEpochBox.id
       |  //  2. SELF.R6[Long].get is a long value (its sign can be anything, since the consensus enforces correctness)
       |  
       |  val pubKey = SELF.R4[GroupElement].get
       |
       |  def isBasicCopy(b:Box) = b.R4[GroupElement].get == pubKey && 
       |                           b.propositionBytes == SELF.propositionBytes &&
       |                           b.tokens == SELF.tokens
       |                            
       |  def isCollectOut(b:Box) = isBasicCopy(b) && b.value > SELF.value
       |  
       |  val isCollect = INPUTS(0).tokens(0)._1 == poolNFT && OUTPUTS.exists(isCollectOut)
       |   
       |  val isCommit = proveDlog(pubKey) && OUTPUTS.exists(isBasicCopy) // committer should ensure that in addition to isBasicCopy, the output should be of correct form 
       |  
       |  sigmaProp(
       |    isCollect || 
       |    isCommit // can also extract accumulated Ergs here
       |  )
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

  val updateScript =
    s"""{ // This box:
       |  // Registers empty 
       |  // 
       |  // ballot boxes (Inputs)
       |  // R4 the new control value
       |  // R5 the box id of this box
       |
       |  // collect and update in one step now!
       |  val poolBoxIn = INPUTS(0) // pool is 1st input
       |  
       |  val poolBoxOut = OUTPUTS(0) // pool is the 1st output
       |  val updateBoxOut = OUTPUTS(1)
       |  
       |  val poolBoxOutHash = blake2b256(poolBoxOut.propositionBytes)
       |  
       |  val validPoolIn = poolBoxIn.tokens(0)._1 == poolNFT
       |  val validPoolOut = poolBoxIn.tokens == poolBoxOut.tokens && 
       |                     poolBoxIn.R4[Long].get == poolBoxOut.R4[Long].get &&
       |                     poolBoxIn.R5[Int].get == poolBoxOut.R5[Int].get &&
       |                     poolBoxIn.value == poolBoxOut.value
       |
       |  
       |  val validUpdateIn = SELF.id == INPUTS(1).id // this is 2nd input
       |  
       |  val validUpdateOut = SELF.tokens == updateBoxOut.tokens && 
       |                       SELF.propositionBytes == updateBoxOut.propositionBytes &&
       |                       SELF.value >= updateBoxOut.value
       |
       |  def validBallotSubmissionBox(b:Box) = b.tokens(0)._1 == ballotTokenId &&
       |                                        b.R4[Coll[Byte]].get == poolBoxOutHash && // ensure that vote is for the poolBoxOutHash
       |                                        b.R5[Coll[Byte]].get == SELF.id  // ensure that vote corresponds to this update only
       |  
       |  val ballotsIn = INPUTS.filter(validBallotSubmissionBox)
       |  
       |  val ballotCount = ballotsIn.fold(0L, { (accum: Long, box: Box) =>  accum + box.tokens(0)._2 })
       |  
       |  val voteAccepted = ballotCount >= $minVotes
       |
       |  sigmaProp(validPoolIn && validPoolOut && validUpdateIn && validUpdateOut && voteAccepted)
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

  val liveEpochAddress: String = getStringFromAddress(getAddressFromErgoTree(liveEpochErgoTree))
  val epochPrepAddress: String = getStringFromAddress(getAddressFromErgoTree(epochPrepErgoTree))
  val dataPointAddress: String = getStringFromAddress(getAddressFromErgoTree(dataPointErgoTree))
  val poolDepositAddress: String = getStringFromAddress(getAddressFromErgoTree(poolDepositErgoTree))
  val updateAddress: String = getStringFromAddress(getAddressFromErgoTree(updateErgoTree))
}
