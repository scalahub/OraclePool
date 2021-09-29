package oraclepool.v2

object OraclePool {
  val poolConfig = PoolConfig(
    epochLength = 30, // 30 blocks in 1 hour
    minDataPoints = 4,
    buffer = 4,
    maxDeviationPercent = 5,
    oracleTokenId = "2A472D4A614E645267556B58703273357638792F423F4528482B4D6250655368",
    refreshNFT = "546A576E5A7234753778214125442A472D4B614E645267556B58703273357638",
    updateNFT = "6251655468576D5A7134743777217A25432A462D4A404E635266556A586E3272",
    poolNFT = "472B4B6250655368566D597133743677397A24432646294A404D635166546A57",
    ballotTokenId = "3F4428472D4B6150645367566B5970337336763979244226452948404D625165",
    minVotes = 6,
    minStorageRent = 10000000
  )

  val pool = new Contracts(poolConfig)

  def main(args: Array[String]): Unit = {

    println("REFRESH CONTRACT")
    println(pool.refreshScript)
    println
    println(pool.refreshAddress)
    println("-------------------------------------------------")
    println("POOL CONTRACT")
    println(pool.poolScript)
    println
    println(pool.poolAddress)
    println("-------------------------------------------------")
    println("ORACLE CONTRACT")
    println(pool.oracleScript)
    println
    println(pool.oracleAddress)
    println("-------------------------------------------------")
    println("UPDATE CONTRACT")
    println(pool.updateScript)
    println
    println(pool.updateAddress)
    println("-------------------------------------------------")
    println("BALLOT CONTRACT")
    println(pool.ballotScript)
    println
    println(pool.ballotAddress)

  }

  // ToDo: Test following use-cases
  // 1. post data point
  // 2. collect data point
  // 3. transfer data point
  // 4. update pool box
  // 5. repeat 1, 2, 3 after update
}
