{
  // This box:
  // R4: The finalized data point from collection
  // R5: Height the epoch will end

  val canStartEpoch = HEIGHT > SELF.R5[Int].get - 3
  val epochNotOver = HEIGHT < SELF.R5[Int].get
  val epochOver = HEIGHT >= SELF.R5[Int].get
  val enoughFunds = SELF.value >= 14000000

  val maxNewEpochHeight = HEIGHT + 5 + 2
  val minNewEpochHeight = HEIGHT + 5

  val poolAction = if (OUTPUTS(0).R6[Coll[Byte]].isDefined) {
    val isliveEpochOutput = OUTPUTS(0).R6[Coll[Byte]].get == blake2b256(SELF.propositionBytes) &&
                            blake2b256(OUTPUTS(0).propositionBytes) == fromBase64("DggVrcSDB/I3av1Nv7G0xRuiBL9f7pf4SDTbJ4UU33s=")
    ( // start next epoch
      epochNotOver && canStartEpoch && enoughFunds &&
      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
      OUTPUTS(0).R5[Int].get == SELF.R5[Int].get &&
      OUTPUTS(0).tokens == SELF.tokens &&
      OUTPUTS(0).value >= SELF.value &&
      isliveEpochOutput
    ) || ( // create new epoch
      epochOver &&
      enoughFunds &&
      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
      OUTPUTS(0).R5[Int].get >= minNewEpochHeight &&
      OUTPUTS(0).R5[Int].get <= maxNewEpochHeight &&
      OUTPUTS(0).tokens == SELF.tokens &&
      OUTPUTS(0).value >= SELF.value &&
      isliveEpochOutput
    )
  } else {
    ( // collect funds
      OUTPUTS(0).R4[Long].get == SELF.R4[Long].get &&
      OUTPUTS(0).R5[Int].get == SELF.R5[Int].get &&
      OUTPUTS(0).propositionBytes == SELF.propositionBytes &&
      OUTPUTS(0).tokens == SELF.tokens &&
      OUTPUTS(0).value > SELF.value
    )
  }

  // below value e44pLEqJ77UJyJsQERRoIjZ44KhVsgYHoLn86Aqa9pQ= is the base64 encoding of the hash of (the ergotree of) the live epoch script
  // to check yourself, first compile the live epoch script and then get its ergotree and then its hash, and finally convert to base64

  val updateAction = INPUTS(1).tokens(0)._1 == fromBase64("e44pLEqJ77UJyJsQERRoIjZ44KhVsgYHoLn86Aqa9pQ=") && CONTEXT.dataInputs.size == 0

  sigmaProp(poolAction || updateAction)
}