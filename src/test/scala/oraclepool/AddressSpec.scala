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

    // 1 oracle
    //    liveEpochAddress shouldEqual "NTkuk55NdwCVifH8NTWdK7o6Z4S6RcZPmprheumFHoc537SDnupXc6VNPGTYm1p93TRoSA1pqMkwuobnJDoUwDnx553shYcwgboPrYtYe5Xit4fDfXAtxvqoEDq8zFxVUnu2qJmy9BH7vN9kVPTkZLEaGvpmQQqcvvqhaCcazd7iapmXu2HsfhMwWshqJwDvxD4miYCGWtmfkppcn3RReFEhEFVZuLhZCFMEEJ8aTMtKvpmir54jk9hiiEfHE6drKNw3HQZyPng2UJq3gAyyPcH7KbZLwAxwHqeUvZvMuSS2ZaFUGn65yGGpzr1vSYjHvbDxRcUfh8B4cxzqJ2on9B9thuzPUDcPrLRvYgbpWQUqvcakhvgMWUDiJyJjKb5Q7y6qX2tiwxKyXH6crSxgz6kuJRKLAPDMKBPhawU5skbAkmcgbuAcpRxABnGqmT5Y5ZLckqSfrsEHyktPpkhrivi1yr5zVG35vmK6kNJrcMDzrGn6GuJanoUzKvqm7DNgmZjVdGDdvafXF8r83kgizHMNP7yzVXeYD7gt58oLa5JpvyXhBdN3vcS2qR"
    //    epochPrepAddress shouldEqual "2Qgfhy6m7Exe9deFZcZBDbN83eQmLQNCcYhuTFMRKLNsrKHgcRsZ9Y2mdEyFumYY7CqRDpAVDrQ1L28vYQEt5yyVEabpRQfvukeKLaAXE4g4Rz67wUa7bCtNvyJDVsJebc1wKy37LwTktSUZKLaxUd1TQShu9jN3rD3h7xRJvuqznTwm3h3r2Hz8GRKu3L8pFUCGCXBZsGMCVyXPrhc3ztDSL9V8cWnKU7HSVrA4YS3BA38ATicDzsoFhLAN6H7jjEZxa1jBEc91ncVdR9zrzXPNJi683GAWShjFyLqfiNHLtsoj152vdrLDJgoUem1gPntLPd3LFZQgkHupqfRDgAkxDFArpdQi2LboaWPhg7yiDPsLWD5gKTgervQ619NKbYrMi2CUGqx4XVv7VGXsxXJHtQ7TzwWpx4K6g6tyFRfFAVB8Zr2nvu4Tt8SbRPXpPSGg1moVM8bkeCaxxfmW4TvdT5X5famkZwqAmF"
    // 2 oracles (rest params same)
    //    liveEpochAddress shouldEqual "NTkuk55NdwCVifH8NTWdK7o6Z4S6RcZPmprheumFHoc537SDnupXc6VNPGTYm1p93TRoSA1pqMkwuobnJE7Q7w6UgXPRX9zpFrhEsCNFXVTNqxuduqLnQkMc8FSumuW6PA5KvNE3aq4iVMiMnt7eRS7BRnynCR5AKvuAhvWzvbqfNT9hRzxoybDFu8JNoc3nt25cpwh3Q4RyvodH51MHT91dWrVLUFjRAz8h2Fi12s7uU9XhRyDYFUhSmAVyRzBJ5sFxo25FzwKMSc9NK6KNPgXg6tEWSZs6nQUpz1Xfyfwh5NgDU8yDe3hhS76ze8hwXxTC1HwsCF9LPVAXn4C79CgJgJSTMj19HsCttD38stiGQSJLCdZejpEsDfXuq6VmnMdSMQw2eCfTzgRhQmGXQ9GSJx9y4rmMe5V4agaJeoWhSL5oao7Ee2wX2bgg4ydkYG6zaUC5yeLAVcQhtB56kN6uWttX7T66KNcgF8KjWapztQQT9vvaZoCUrDD3GGtu3pBU7zKsvjDTFn18NH9oKDCCGnrqNfUMuprmZM9onLawgjeHFXormH1uTg"
    //    epochPrepAddress shouldEqual "2Qgfhy6m7Exe9deFZcYsSVzhfk39CD8miVnxHuGPPb8J5534aDycpy44zPMPEgLspoiwdRyc5LkTaqeE3tVM3KagY7goHFHXdZrEobWkqXP3Rczj3skXr6g4N4gg6aWVU1DrUpgJWshpeFmPvnbDqXJSa3gejK7dsp4N53iLMsx2xQaf2SV7kqBVGbmMWVM7JH8aorNXyh94Wn18o7pN4SWySoN6fG5X2J5JiSzh7FwY7E2TFJLW1MvvFcgTXTJF79tkAxNSc18hDiVHig9yFxDGg3zPDSFD589Ew4C6TY2AWhe6dVdQCP1cCptJyxSTcRqW9JJFwkmZtMbCNsfXYwmY67RGwfwbfcYjuJJtd4u5K7FJ1EcwMr4WjpTf95NFgQgBXVvo7YfuwzvkVDhrgFPKKhje2U37fsmNbW2GKREmn6Yqhxoy16uQXHyb8p13yjs4QHNTqcV9zRAb1T3PRbh7Fk5NGSWjkCJeHB"
    // 4 oracles (rest params same)
    //    liveEpochAddress shouldEqual "NTkuk55NdwCVifH8NTWdK7o6Z4S6RcZPmprheumFHoc537SDnupXc6VNPGTYm1p93TRoSA1pqMkwuobnJEjEVMhXuS4XANkZQNUvtVKfJKJgmmQUQTgZJPPDvJgTMCbJBtRv6V8CU8dudLqaPsRS9drPjXHonRYF7w26yNKpnZHYwhv2VxJgbNutfdVTnwhWje7K3kgbAPkcGmEbewD14vZW64Usc5o98ThccArrBsb4Yo3ebmX9G8gts2BMqmGBcquopF5qDEc1PCn1avzAPq2ofTaqTMfRmY9X6tkJ88y26yXhrrjUycZSJdH93JfEkgufAetGCV5svXVvk6xm9Fj8d5Lb8jneAvkqZFumcsB7N5jVC3LFCWHB33zGr7LX78ge2B1e2hLSwV5rXPtCEEHWL1qEsosNHsfnaAnkCuMkoT23YazUHEvEiEWLg2kBTfdkDjgvDCXuXKTL11oaoEtgazVaMqC76bDqEeMVK42zxffAuzAa7ndTtmwbaPwKcK5R7SYMw2KKH4K91L5wy4sr48cX8w81KFCYXmrkCs9BCFsTPLhUTjucgA"
    //    epochPrepAddress shouldEqual "2Qgfhy6m7Exe9deFZcYQ9pWN92i4eaW8ceaoQm6aj43yVuhf6ZCVL2yt34GxgfKvLnrQ8vk2B1pwM3zGBt1DgyG7BZmKGtfc5Yo7wgATjuz3eqf5mHkTFFfafEDtTQwgNqS6EnaZAVXKQPAZSmYazje9mauLABEvr5qC3xsjbrYV5E6uwHNACMhVmcrP92hqDsNxiP5JUeLVkGAt3ktvy5hwA2nEC4GbkMb5Gu5Fdu4EzNq5kQdEhiLJtMwW8cav8J1yHNWPjJ23L9Wikr3frLQfSG9YghjQXoX7A2qVJ5yXMcGB678P1AtsgnukLgLC6aneoFzj2cRUqLYD4Ch6PquFJ4NRWySHKKz6Mrfhu95JuFu4gpdLr6mBfMriZAM99Br4UYtsfbLSmmp1D82h8czP2RNG5RW8p7uMPgCYTEABFrpVCCVqcF7hBM3CHni3NExghdZSvBUMkXqQuHYah2dhbYUx1xoLxp2LzZ"
    // 3 oracles (and live epoch length increased to 4 (from 3))
    liveEpochAddress shouldEqual "NTkuk55NdwCVifH8NTWdK7o6Z4S6RcZPmprheumFHoc537SDnupXc6VNPGTYm1p93TRoSA1pqMkwuobnJERKJeQ42nbfQzsMgqzCLrme22aaBWSvRHJ9271WQ1ppgwUtHr2nHW7CHx4sTofjkQbYhW5z2u21fkuzLxhFj8AnHrDQJHn4gunx15jUBidNrADSGsinhnDc2GZWCtV3Zak6fdjr9PnCNyE5ZNNfyWRLxNc5KQaSQFfnBiFK7ThG2SnWWBkPhzAASaWa5LuD2KopZg2GPUyS48GRVJiBVo3DgkVGDeeqSoCRZ9266gE1bEZAydx2F4tjMEGyaETCy7qdR4Nm6TjAsxCYMxemH4MKAvfb73VCuuvfJ9k3ENcnXW4FkrVdjaZXrXHUHwPiXAJPLZYXBAuiTZpRuUrJNvY13f3iYSEwhAX5JucB1Kq3aJ2A8dSeJnatNYb7WfpqqLuKhygeweiexxt4ua8pxitynvDGx6yzNZ6EjEaxSMpP9ZpERhir2bKnrk2ym74VFLvb5XqhpRFRThqfEok1DVMEZD2Pz6v9mJGHCRTDfo"
    epochPrepAddress shouldEqual "2Qgfhy6m7Exe9deGQMEBJqaeVKgSJJyvNU7kBAwyfjvqv1PMqg4SLh1aMDgobaqpqV6Bb8MfzyBVAMVHRw1aQGfyKXNwafriXH1GUmvKnR3uweks3F1u3rp9bPLfxykGKz81bniWeu5nYfjStHcwCJ2iGVqaKusTxbTPAJ38o2zpqKUgTyfovCbAYEtc2zX3A2efvSZdn2W5W1gKVACDTXC4oWzMAa9iqmCTSofwKxycJAV8sTMP94q3QeUpytckTdqsYatV5PhJxk8NeDaaPmsXbf4J6YNMqdyFwAHynggACv6gAL9xMqijxPt3KWiKV6XNwSDsViJRFkHeiLmX9LbYGiyQ1TYy2f6V7GfLjB5deg9gBbCcNNcRwUkhdH1WdRKnzkzmLUTiPQcFGu4JbHrFezgK9vaVd7uCxZJrTcgA871Aia6GrcFCcSSiXB5tDYXsTjE5BCULf2vbHJUgVWocWV5SV5qL6k368J"
    dataPointAddress shouldEqual "AucEQEJ3Y5Uhmu4o8dnoztRiAKKTErrhugq6EyACQ4SrK7NFAds5u9B93Xvb7heGC9oGL88F8muu6T6MARfqKpHS2ce1jZ6x8Ju6j9n4AvWkcQxBaLUq36wHGKmiCqRDtKT5tbhZ7hQbK7WuMAejKD7aW91yTBrBNHAXDVmZznmYjzJqDQGuPMsRHQSYGGyW5H2p"
    poolDepositAddress shouldEqual "3TVSZTMA5A3ApBRfyofswakiBTh5snekN1FmK2s8P8GKp7adEf1LdvZ9r2tPMkb7hNe652wgfcntJ4zXhKi1zdihxQLBHfnUBWN436HAaEurBMWBhH9me68o7LHMzbwd1PRgscVG6f3XLwd3kmN9DVFDjvKpAQhg766gdAy3L74kPwkAqZz7Ma9wpYPkoEMP44YaAirDU8XtahQNG6"
    updateAddress shouldEqual "9vQZrzvghytfwhiSPKe6cWfq7pjQCk537UA3BWRMUCnAYVgVE9p5beEiX4WLtyFRtWptCqmzA8Ga9GFhZpRVJGRD87fCwVWBM3xxK7VaZDV8oWWZhTCRgCURxNkYHS5nUcSoc2Y8Up8dABy8ncyYznkvurCJaNH89XcwsJTZLN5WHtPJrJe1ESAKvbzcLqUqgnuTa7Ru1VzJ5kbWtudwCrH2YfhoQViipJNQCPkTfzSFVUiX3dLfTxTn647v2Li5iEDrJcTyvZYaafH9WJvoCp24PniKZQ6J5bb9Sv4pDmwZEnk3rvUELQYfCiaf4Npeg2vY11TFqwbjjvNZfq7EKyL59JhkSVUYP3KMJWQpcCLmWNfXwt5GwFBmff2sWfPCahfHQYbWhzc8NGsy5Ruc6XEgFAG8NPDbYwWdxUGnjUwGNaQCULBT2jpReSrJ2aKSf8AZKgLiaidHb5bFQ5u7Yt9jzbQdZgqYYpddEFqeiAaV1LQnskXhRWvsSz4pBKm7TDa2sWzuPTq9u11huTjy96wtKpsBTwFjegV"
    Blake2b256(epochPrepErgoTree.bytes).encodeHex shouldEqual "07db45d97b14a33c583ec7900c60fb59dad90b93059ce7086ee1759d7e9394a6"
    Blake2b256(liveEpochErgoTree.bytes).encodeHex shouldEqual "3224d2ca054d9b80eb15c945072cd29fcac771b8e8d8fb2f73846845341c133e"
    minPoolBoxValue shouldEqual 14000000
  }
}
