package oraclepool

import kiosk.ergo._
import org.scalatest.{Matchers, PropSpec}
import scorex.crypto.hash.Blake2b256

class AddressSpec extends PropSpec with Matchers {
  lazy val minBoxValue = 2000000 // min value to remain AFTER paying rewards. For min value to keep BEFORE rewards, see minPoolBoxValue
  val epochPoolLive = new OraclePool {}

  import epochPoolLive._

  property("Display Addresses") {
    println("minPoolBoxValue " + minPoolBoxValue)
    println(s"Live Epoch script length       : ${liveEpochErgoTree.bytes.length}")
    println(s"Live Epoch script complexity   : ${liveEpochErgoTree.complexity}")
    println(s"Epoch prep script length       : ${epochPrepErgoTree.bytes.length}")
    println(s"Epoch prep script complexity   : ${epochPrepErgoTree.complexity}")
    println(s"DataPoint script length        : ${dataPointErgoTree.bytes.length}")
    println(s"DataPoint script complexity    : ${dataPointErgoTree.complexity}")
    println(s"PoolDeposit script length      : ${poolDepositErgoTree.bytes.length}")
    println(s"PoolDeposit script complexity  : ${poolDepositErgoTree.complexity}")

    println("liveEpochAddress: " + liveEpochAddress)
    println("epochPrepAddress: " + epochPrepAddress)
    println("dataPointAddress: " + dataPointAddress)
    println("poolDepositAddress: " + poolDepositAddress)
    println("updateAddress: " + updateAddress)
    println("EpochPrepScriptHash: (for R6 of LiveEpochBox) " + Blake2b256(KioskErgoTree(epochPrepErgoTree).serialize).encodeHex)
    println("LiveEpochScriptHash: (for hard-coding in Datapoint script) " + Blake2b256(liveEpochErgoTree.bytes).encodeHex)
    println("Min box value: " + minPoolBoxValue)

    //    liveEpochAddress shouldEqual "NTkuk55NdwCVifH8NTWdK7o6Z4S6RcZPmprheumFHoc537SDnupXc6VNPGTYm1p93TRoSA1pqMkwuobnJDoUwDnx553shYcwgboPrYtYe5Xit4fDfXAtxvqoEDq8zFxVUnu2qJmy9BH7vN9kVPTkZLEaGvpmQQqcvvqhaCcazd7iapmXu2HsfhMwWshqJwDvxD4miYCGWtmfkppcn3RReFEhEFVZuLhZCFMEEJ8aTMtKvpmir54jk9hiiEfHE6drKNw3HQZyPng2UJq3gAyyPcH7KbZLwAxwHqeUvZvMuSS2ZaFUGn65yGGpzr1vSYjHvbDxRcUfh8B4cxzqJ2on9B9thuzPUDcPrLRvYgbpWQUqvcakhvgMWUDiJyJjKb5Q7y6qX2tiwxKyXH6crSxgz6kuJRKLAPDMKBPhawU5skbAkmcgbuAcpRxABnGqmT5Y5ZLckqSfrsEHyktPpkhrivi1yr5zVG35vmK6kNJrcMDzrGn6GuJanoUzKvqm7DNgmZjVdGDdvafXF8r83kgizHMNP7yzVXeYD7gt58oLa5JpvyXhBdN3vcS2qR"
    //    epochPrepAddress shouldEqual "2Qgfhy6m7Exe9deFZcZBDbN83eQmLQNCcYhuTFMRKLNsrKHgcRsZ9Y2mdEyFumYY7CqRDpAVDrQ1L28vYQEt5yyVEabpRQfvukeKLaAXE4g4Rz67wUa7bCtNvyJDVsJebc1wKy37LwTktSUZKLaxUd1TQShu9jN3rD3h7xRJvuqznTwm3h3r2Hz8GRKu3L8pFUCGCXBZsGMCVyXPrhc3ztDSL9V8cWnKU7HSVrA4YS3BA38ATicDzsoFhLAN6H7jjEZxa1jBEc91ncVdR9zrzXPNJi683GAWShjFyLqfiNHLtsoj152vdrLDJgoUem1gPntLPd3LFZQgkHupqfRDgAkxDFArpdQi2LboaWPhg7yiDPsLWD5gKTgervQ619NKbYrMi2CUGqx4XVv7VGXsxXJHtQ7TzwWpx4K6g6tyFRfFAVB8Zr2nvu4Tt8SbRPXpPSGg1moVM8bkeCaxxfmW4TvdT5X5famkZwqAmF"
    liveEpochAddress shouldEqual "NTkuk55NdwCVifH8NTWdK7o6Z4S6RcZPmprheumFHoc537SDnupXc6VNPGTYm1p93TRoSA1pqMkwuobnJE7Q7w6UgXPRX9zpFrhEsCNFXVTNqxuduqLnQkMc8FSumuW6PA5KvNE3aq4iVMiMnt7eRS7BRnynCR5AKvuAhvWzvbqfNT9hRzxoybDFu8JNoc3nt25cpwh3Q4RyvodH51MHT91dWrVLUFjRAz8h2Fi12s7uU9XhRyDYFUhSmAVyRzBJ5sFxo25FzwKMSc9NK6KNPgXg6tEWSZs6nQUpz1Xfyfwh5NgDU8yDe3hhS76ze8hwXxTC1HwsCF9LPVAXn4C79CgJgJSTMj19HsCttD38stiGQSJLCdZejpEsDfXuq6VmnMdSMQw2eCfTzgRhQmGXQ9GSJx9y4rmMe5V4agaJeoWhSL5oao7Ee2wX2bgg4ydkYG6zaUC5yeLAVcQhtB56kN6uWttX7T66KNcgF8KjWapztQQT9vvaZoCUrDD3GGtu3pBU7zKsvjDTFn18NH9oKDCCGnrqNfUMuprmZM9onLawgjeHFXormH1uTg"
    epochPrepAddress shouldEqual "2Qgfhy6m7Exe9deFZcYsSVzhfk39CD8miVnxHuGPPb8J5534aDycpy44zPMPEgLspoiwdRyc5LkTaqeE3tVM3KagY7goHFHXdZrEobWkqXP3Rczj3skXr6g4N4gg6aWVU1DrUpgJWshpeFmPvnbDqXJSa3gejK7dsp4N53iLMsx2xQaf2SV7kqBVGbmMWVM7JH8aorNXyh94Wn18o7pN4SWySoN6fG5X2J5JiSzh7FwY7E2TFJLW1MvvFcgTXTJF79tkAxNSc18hDiVHig9yFxDGg3zPDSFD589Ew4C6TY2AWhe6dVdQCP1cCptJyxSTcRqW9JJFwkmZtMbCNsfXYwmY67RGwfwbfcYjuJJtd4u5K7FJ1EcwMr4WjpTf95NFgQgBXVvo7YfuwzvkVDhrgFPKKhje2U37fsmNbW2GKREmn6Yqhxoy16uQXHyb8p13yjs4QHNTqcV9zRAb1T3PRbh7Fk5NGSWjkCJeHB"
    dataPointAddress shouldEqual "AucEQEJ3Y5Uhmu4o8dnoztRiAKKTErrhugq6EyACQ4SrK7NFAds5u9B93Xvb7heGC9oGL88F8muu6T6MARfqKpHS2ce1jZ6x8Ju6j9n4AvWkcQxBaLUq36wHGKmiCqRDtKT5tbhZ7hQbK7WuMAejKD7aW91yTBrBNHAXDVmZznmYjzJqDQGuPMsRHQSYGGyW5H2p"
    poolDepositAddress shouldEqual "zLSQDVBaDRMCSFAjTeCYwnP9Peo1Lcj7VvqkoAMdFKHWTqBj2teyhZAzgVE5CRWb7YM49qaffWiiff7C5BoiFmoMX6bidcaYMt2U4FSSXBfmEAchcRDFJR5pGnP1dV75JhXjUCnUGuFFnXN3c21koeYtCJot3VcSC7MDPsqQ1M4j5N3mxz5LyjXsDa7PifQjcesS4UKodhjVe5NMzu1sdYovRdtv1xK6tPdnhZ3TTEkcfWMhWa"
    updateAddress shouldEqual "9vQZrzvghytfwhiSPKe6cWfq7pjQCk537UA3BWRMUCnAYVgVE9p5beEiX4WLtyFRtWptCqmzA8Ga9GFhZpRVJGRD87fCwVWBM3xxK7VaZDV8oWWZhTCRgCURxNkYHS5nUcSoc2Y8Up8dABy8ncyYznkvurCJaNH89XcwsJTZLN5WHtPJrJe1ESAKvbzcLqUqgnuTa7Ru1VzJ5kbWtudwCrH2YfhoQViipJNQCPkTfzSFVUiX3dLfTxTn647v2Li5iEDrJcTyvZYaafH9WJvoCp24PniKZQ6J5bb9Sv4pDmwZEnk3rvUELQYfCiaf4Npeg2vY11TFqwbjjvNZfq7EKyL59JhkSVUYP3KMJWQpcCLmWNfXwt5GwFBmff2sWfPCahfHQYbWhzc8NGsy5Ruc6XEgFAG8NPDbYwWdxUGnjUwGNaQCULBT2jpReSrJ2aKSf8AZKgLiaidHb5bFQ5u7Yt9jzbQdZgqYYpddEFqeiAaV1LQnskXhRWvsSz4pBKm7TDa2sWzuPTq9u11huTjy96wtKpsBTwFjegV"
    Blake2b256(epochPrepErgoTree.bytes).encodeHex shouldEqual "26115b846881709e6703ca4e3faeb2d98341e613ce2c417cd32ca41e6ab9fa6d"
    Blake2b256(liveEpochErgoTree.bytes).encodeHex shouldEqual "6e52618b5a721f5de9c1a466fef4400d0a9170c850e2b7bfbf03a8b221391607"
    minPoolBoxValue shouldEqual 14000000
  }
}
