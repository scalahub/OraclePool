package oraclepool.v1c

trait OraclePool extends Contracts {
  lazy val minBoxValue = 1000000 // min value to remain AFTER paying rewards. For min value to keep BEFORE rewards, see minPoolBoxValue
  override def livePeriod = 4 // blocks
  override def prepPeriod = 2 // blocks
  override def buffer = 2 // blocks
  override def maxDeviation: Int = 5 // percent
  override def minOracleBoxes: Int = 4
  override def oracleReward = 250000 // Nano ergs. One reward per data point to be paid to oracle
  lazy val maxNumOracles = 15
  lazy val minVotes = 8 // for update
  lazy val minStorageRent = 10000000L

  lazy val poolNFT = "011d3364de07e5a26f0c4eef0852cddb387039a921b7154ef3cab22c6eda887f" // 1 token issued
  lazy val oracleToken = "8c27dd9d8a35aac1e3167d58858c0a8b4059b277da790552e37eba22df9b9035" // 15 tokens issued
  lazy val updateNFT = "720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dde" // 1 token issued
  lazy val ballotToken = "053fefab5477138b760bc7ae666c3e2b324d5ae937a13605cb766ec5222e5518" // 15 tokens issued

  override def minPoolBoxValue: Long = oracleReward * (maxNumOracles + 1) + minBoxValue // min value allowed in live epoch box
}
