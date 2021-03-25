package oraclepool.v1a

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

  lazy val poolNFT = "e0630fa2366d8d0682cc1dbc114ad57f023bc1f2948999079d0cd05fcbc4db01" // 1 token issued
  lazy val oracleToken = "cd00a6c593b562b7e7f485f2734d39a42111dd03c9f48eb82d6326661ad70978" // 15 tokens issued
  lazy val updateNFT = "dd26438230986cfe7305ad958451b69e55ad5ac37c8a355bfb08d810edd7a20f" // 1 token issued
  lazy val ballotToken = "b662db51cf2dc39f110a021c2a31c74f0a1a18ffffbf73e8a051a7b8c0f09ebc" // 15 tokens issued

  override def minPoolBoxValue: Long = oracleReward * (maxNumOracles + 1) + minBoxValue // min value allowed in live epoch box
}
//https://kioskweb.org/session/O0tOMyzuuCoGnFaylR4Wvim8F#kiosk.Wallet
