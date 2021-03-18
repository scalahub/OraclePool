package oraclepool

trait OraclePool extends Contracts {
  lazy val minBoxValue = 1000000 // min value to remain AFTER paying rewards. For min value to keep BEFORE rewards, see minPoolBoxValue
  override def livePeriod = 4 // blocks
  override def prepPeriod = 2 // blocks
  override def buffer = 2 // blocks
  override def maxDeviation: Int = 5 // percent
  override def minOracleBoxes: Int = 3
  override def oracleReward = 1000000 // Nano ergs. One reward per data point to be paid to oracle
  lazy val maxNumOracles = 15
  lazy val minVotes = 8 // for update
  lazy val minStorageRent = 10000000L

  /*
    V0
    poolNFT           008a94c8c76bbaa1f0a346697d1794eb31d94b37e5533af9cc0b6932bf159339
    12 oracle Tokens  5579de48d16e54ddb34df789d418e1f10c119e15a824ea32dc21696c067f9fbe
    updateNFT         7b8e292c4a89efb509c89b10111468223678e0a855b20607a0b9fce80a9af694
    12 ballot tokens  f502b38d40208f8faf0e4dd9f74952e17b24d8f96abb9c9f57f8e961d25a8e37
   */

  // v1
  lazy val poolNFT = "008a94c8c76bbaa1f0a346697d1794eb31d94b37e5533af9cc0b6932bf159339" // SAME
  lazy val oracleToken = "b5e02eea9d3e3a604897a0ab05eb61c15c37fdecff02c6109dc0fe90a1918760" // NEW // 15
  lazy val updateNFT = "37eaa0932d7e250d8f260e36d50031e85a7d175b67f682e4c820cb6eabf93bcd" // NEW
  lazy val ballotToken = "45c9a9c807c2759b9bce774f7539b4749feb6650f9ef7fd2d3b549bab987fc58" // NEW // 15

  override def minPoolBoxValue: Long = oracleReward * (maxNumOracles + 1) + minBoxValue // min value allowed in live epoch box
}
