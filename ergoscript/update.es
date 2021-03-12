{ // This box:
  // R4 the "control value" (such as the hash of a script of some other box)
  //
  // ballot boxes (data Inputs)
  // R4 the new control value
  // R5 the box id of this box

  val isUpdate = INPUTS(0).tokens(0)._1 == fromBase64("AIqUyMdruqHwo0ZpfReU6zHZSzflUzr5zAtpMr8Vkzk=")
  val updateBoxIn = if (isUpdate) INPUTS(1) else INPUTS(0)
  val updateBoxOut = if (isUpdate) OUTPUTS(1) else OUTPUTS(0)
  val validIn = SELF.id == updateBoxIn.id

  val voteSuccessPath = {
    val newValue = updateBoxOut.R4[Coll[Byte]].get
    val oldValue = updateBoxIn.R4[Coll[Byte]].get

    val validOut = updateBoxOut.propositionBytes == updateBoxIn.propositionBytes &&
                   updateBoxOut.value >= 10000000 &&
                   updateBoxOut.tokens == updateBoxIn.tokens &&
                   newValue != oldValue

    def validBallotSubmissionBox(b:Box) = b.tokens(0)._1 == fromBase64("9QKzjUAgj4+vDk3Z90lS4Xsk2Plqu5yfV/jpYdJajjc=") &&
                                          b.R4[Coll[Byte]].get == newValue && // ensure that vote is for the newValue
                                          b.R5[Coll[Byte]].get == SELF.id  // ensure that vote counts only once

    val ballots = CONTEXT.dataInputs.filter(validBallotSubmissionBox)

    val ballotCount = ballots.fold(0L, { (accum: Long, box: Box) => accum + box.tokens(0)._2 })

    val voteAccepted = ballotCount >= 6

    validOut && voteAccepted
  }

  val updatePath = {
    val epochPrepBoxIn = INPUTS(0)
    val epochPrepBoxOut = OUTPUTS(0)

    val storedNewHash = SELF.R4[Coll[Byte]].get
    val epochPrepBoxOutHash = blake2b256(epochPrepBoxOut.propositionBytes)

    val validPoolBox = epochPrepBoxIn.tokens(0)._1 == fromBase64("AIqUyMdruqHwo0ZpfReU6zHZSzflUzr5zAtpMr8Vkzk=") && // epochPrep box is first input
                       epochPrepBoxIn.tokens == epochPrepBoxOut.tokens &&
                       storedNewHash == epochPrepBoxOutHash &&
                       epochPrepBoxIn.propositionBytes != epochPrepBoxOut.propositionBytes &&
                       epochPrepBoxIn.R4[Long].get == epochPrepBoxOut.R4[Long].get &&
                       epochPrepBoxIn.R5[Int].get == epochPrepBoxOut.R5[Int].get &&
                       epochPrepBoxIn.value == epochPrepBoxOut.value

    val validUpdateBox = updateBoxIn.R4[Coll[Byte]].get == updateBoxOut.R4[Coll[Byte]].get &&
                         updateBoxIn.propositionBytes == updateBoxOut.propositionBytes &&
                         updateBoxIn.tokens == updateBoxOut.tokens &&
                         updateBoxIn.value == updateBoxOut.value

    validPoolBox &&
    validUpdateBox
  }

  sigmaProp(
    validIn && (
      voteSuccessPath ||
      updatePath
    )
  )
}