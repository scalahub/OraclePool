{
  // This box:
  // R4: The address of the oracle (never allowed to change after bootstrap).
  // R5: The box id of the latest Live Epoch box.
  // R6: The oracle's datapoint.

  val pubKey = SELF.R4[GroupElement].get

  val poolBox = CONTEXT.dataInputs(0)

  // Allow datapoint box to contain box id of any box with pool NFT (i.e., either Live Epoch or Epoch Prep boxes)
  // Earlier we additionally required that the box have the live epoch script.
  // In summary:
  //    Earlier: (1st data-input has pool NFT) && (1st data-input has live epoch script)
  //    Now:     (1st data-input has pool NFT)
  //
  val validPoolBox = poolBox.tokens(0)._1 == fromBase64("AIqUyMdruqHwo0ZpfReU6zHZSzflUzr5zAtpMr8Vkzk=")

  sigmaProp(
    OUTPUTS(0).R4[GroupElement].get == pubKey &&
    OUTPUTS(0).R5[Coll[Byte]].get == poolBox.id &&
    OUTPUTS(0).R6[Long].get > 0 &&
    OUTPUTS(0).propositionBytes == SELF.propositionBytes &&
    OUTPUTS(0).tokens == SELF.tokens &&
    validPoolBox
  ) && proveDlog(pubKey)
}