{
  val allFundingBoxes = INPUTS.filter{(b:Box) =>
    b.propositionBytes == SELF.propositionBytes
  }

  val totalFunds = allFundingBoxes.fold(0L, { (t:Long, b: Box) => t + b.value })

  sigmaProp(
    INPUTS(0).tokens(0)._1 == fromBase64("AIqUyMdruqHwo0ZpfReU6zHZSzflUzr5zAtpMr8Vkzk=") &&
    OUTPUTS(0).propositionBytes == INPUTS(0).propositionBytes &&
    OUTPUTS(0).value >= INPUTS(0).value + totalFunds &&
    OUTPUTS(0).tokens(0)._1 == fromBase64("AIqUyMdruqHwo0ZpfReU6zHZSzflUzr5zAtpMr8Vkzk=")
  )
}