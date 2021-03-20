package oraclepool

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

  /*
    V0
    poolNFT           008a94c8c76bbaa1f0a346697d1794eb31d94b37e5533af9cc0b6932bf159339
    12 oracle Tokens  5579de48d16e54ddb34df789d418e1f10c119e15a824ea32dc21696c067f9fbe
    updateNFT         7b8e292c4a89efb509c89b10111468223678e0a855b20607a0b9fce80a9af694
    12 ballot tokens  f502b38d40208f8faf0e4dd9f74952e17b24d8f96abb9c9f57f8e961d25a8e37


  lazy val poolNFT = "c3f12f8055e697391d8173d867ddb2f6fc7000165bf386cd7705df95fd893b9f" // 1 token issued
  lazy val oracleToken = "834c46884299abecb5caab08a04e95b7303436672507daeef703b1b0cc22e2d4" // 15 tokens issued
  lazy val updateNFT = "551023bc00000965340153f6a602ff28b877b9569d4438ae35504d49f0aee77d" // 1 token issued
  lazy val ballotToken = "8f3cee01b8c34cb9d36fd4706dd6c0762e4dba4ed239a22ecbedce8d604c1833" // 15 tokens issued


new oracle pool launched ids
==========================================
PoolNFT 011d3364de07e5a26f0c4eef0852cddb387039a921b7154ef3cab22c6eda887f (quantity 1)
==========================================
Participant Token 8c27dd9d8a35aac1e3167d58858c0a8b4059b277da790552e37eba22df9b9035: 15
==========================================
UpdateNFT 720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dde (quantity 1)
==========================================
Ballot token 053fefab5477138b760bc7ae666c3e2b324d5ae937a13605cb766ec5222e5518: 15
   */

  lazy val poolNFT = "011d3364de07e5a26f0c4eef0852cddb387039a921b7154ef3cab22c6eda887f" // 1 token issued
  lazy val oracleToken = "8c27dd9d8a35aac1e3167d58858c0a8b4059b277da790552e37eba22df9b9035" // 15 tokens issued
  lazy val updateNFT = "720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dde" // 1 token issued
  lazy val ballotToken = "053fefab5477138b760bc7ae666c3e2b324d5ae937a13605cb766ec5222e5518" // 15 tokens issued

  override def minPoolBoxValue: Long = oracleReward * (maxNumOracles + 1) + minBoxValue // min value allowed in live epoch box
}
