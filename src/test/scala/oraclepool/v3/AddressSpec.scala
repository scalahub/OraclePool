package oraclepool.v3

import kiosk.ergo._
import oraclepool.v3.OraclePool
import org.scalatest.{Matchers, PropSpec}
import scorex.crypto.hash.Blake2b256

class AddressSpec extends PropSpec with Matchers {
  lazy val minBoxValue = 2000000 // min value to remain AFTER paying rewards. For min value to keep BEFORE rewards, see minPoolBoxValue
  val pool = new OraclePool {}
  import pool._

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
    println("ballotAddress: " + ballotAddress)
    println("ballotErgoTree: " + ballotErgoTree.bytes.encodeHex)
    println("EpochPrepScriptHash: (for R6 of LiveEpochBox) " + liveEpochScriptHash)
    println("LiveEpochScriptHash: (for hard-coding in Datapoint script) " + liveEpochScriptHash)
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
    //    liveEpochAddress shouldEqual "NTkuk55NdwCVifH8NTWdK7o6Z4S6RcZPmprheumFHoc537SDnupXc6VNPGTYm1p93TRoSA1pqMkwuobnJERKJeQ42nbfQzsMgqzCLrme22aaBWSvRHJ9271WQ1ppgwUtHr2nHW7CHx4sTofjkQbYhW5z2u21fkuzLxhFj8AnHrDQJHn4gunx15jUBidNrADSGsinhnDc2GZWCtV3Zak6fdjr9PnCNyE5ZNNfyWRLxNc5KQaSQFfnBiFK7ThG2SnWWBkPhzAASaWa5LuD2KopZg2GPUyS48GRVJiBVo3DgkVGDeeqSoCRZ9266gE1bEZAydx2F4tjMEGyaETCy7qdR4Nm6TjAsxCYMxemH4MKAvfb73VCuuvfJ9k3ENcnXW4FkrVdjaZXrXHUHwPiXAJPLZYXBAuiTZpRuUrJNvY13f3iYSEwhAX5JucB1Kq3aJ2A8dSeJnatNYb7WfpqqLuKhygeweiexxt4ua8pxitynvDGx6yzNZ6EjEaxSMpP9ZpERhir2bKnrk2ym74VFLvb5XqhpRFRThqfEok1DVMEZD2Pz6v9mJGHCRTDfo"
    //    epochPrepAddress shouldEqual "2Qgfhy6m7Exe9deGQMEBJqaeVKgSJJyvNU7kBAwyfjvqv2PMqg4SLh1aMDgobaqpqV6Bb8MfzyBVAMVHRw1aQGfyKXNwafriXH1GUmvKnR3uweks3F1u3rp9bPLfxykGKz81bniWeu5nYfjStHcwCJ2iGVqaKusTxbTPAJ38o2zpqKUgTyfovCbAYEtc2zX3A2efvSZdn2W5W1gKVACDTXC4oWzMAa9iqmCTSofwKxycJAV8sTMP94q3QeUpytckTdqsYatV5PhJxk8NeDaaPmsXbf4J6YNMqdyFwAHynggACv6gAL9xMqijxPt3KWiKV6XNwSDsViJRFkHeiLmX9LbYGiyQ1TYy2f6V7GfLjB5deg9gBbCcNNcRwUkhdH1WdRKnzkzmLUTiPQcFGu4JbHrFezgK9vaVd7uCxZJrTcgA871Aia6GrcFCcSSiXB5tDYXsTjE5BCULf2vbHJUgVWocWV5SV5qL6k368J"

    liveEpochAddress shouldEqual "NTkuk55NdwCXkF1e2nCABxq7bHjtinX3wH13zYPZ6qYT71dCoZBe1gZkh9FAr7GeHo2EpFoibzpNQmoi89atUjKRrhZEYrTapdtXrWU4kq319oY7BEWmtmRU9cMohX69XMuxJjJP5hRM8WQLfFnffbjshhEP3ck9CKVEkFRw1JDYkqVke2JVqoMED5yxLVkScbBUiJJLWq9BSbE1JJmmreNVskmWNxWE6V7ksKPxFMoqh1SVePh3UWAaBgGQRZ7TWf4dTBF5KMVHmRXzmQqEu2Fz2yeSLy23sM3pfqa78VuvoFHnTFXYFFxn3DNttxwq3EU3Zv25SmgrWjLKiZjFcEcqGgH6DJ9FZ1DfucVtTXwyDJutY3ksUBaEStRxoUQyRu4EhDobixL3PUWRcxaRJ8JKA9b64ALErGepRHkAoVmS8DaE6VbroskyMuhkTo7LbrzhTyJbqKurEzoEfhYxus7bMpLTePgKcktgRRyB7MjVxjSpxWzZedvzbjzZaHLZLkWZESk1WtdM25My33wtVLNXiTvficEUbjA23sNd24pv1YQ72nY1aqUHa2"
    epochPrepAddress shouldEqual "EfS5abyDe4vKFrJ48K5HnwTqa1ksn238bWFPe84bzVvCGvK1h2B7sgWLETtQuWwzVdBaoRZ1HcyzddrxLcsoM5YEy4UnqcLqMU1MDca1kLw9xbazAM6Awo9y6UVWTkQcS97mYkhkmx2Tewg3JntMgzfLWz5mACiEJEv7potayvk6awmLWS36sJMfXWgnEfNiqTyXNiPzt466cgot3GLcEsYXxKzLXyJ9EfvXpjzC2abTMzVSf1e17BHre4zZvDoAeTqr4igV3ubv2PtJjntvF2ibrDLmwwAyANEhw1yt8C8fCidkf3MAoPE6T53hX3Eb2mp3Xofmtrn4qVgmhNonnV8ekWZWvBTxYiNP8Vu5nc6RMDBv7P1c5rRc3tnDMRh2dUcDD7USyoB9YcvioMfAZGMNfLjWqgYu9Ygw2FokGBPThyWrKQ5nkLJvief1eQJg4wZXKdXWAR7VxwNftdZjPCHcmwn6ByRHZo9kb4Emv3rjfZE"
    dataPointAddress shouldEqual "AucEQEJ3Y5Uhmu4o8dsrHy28nRTgX5sVtXvjpMTqdMQzBR3uRVcvCFbv7SeGuPhQ16AXBP7XWdMShDdhRy4cayZgxHSkdAVuTiZRvj6WCfmhXJ4LY2E46CytRAnkiYubCdEroUUX2niMLhjNmDUn4KmXWSrKngrfGwHSaD8RJUMEp5AGADaChRU6kAnh9nstkDN3"
    poolDepositAddress shouldEqual "4L1NEtpkMq6NeZhy2pk6omYvewovcHTm7CbxKm9djsbobAHSdDe6TVfmnW5THVpSHrG6rWovqv7838reswYE3UYkykWaNnhoyBGHFCdZvWqa2TVRtHiWcVaner6giUp55ZpELLuj9TtKePv6zrtMV5YE1o2kjrQEgGDoGHBGNuyx6ymXkSimcAQo1oD4f4tTcuBfWfp"
    updateAddress shouldEqual "Su1rWdkh3KvNSBUiJDHjtzgeDL5cPyZYbvkpXGSUwehqLwUK6N7Vu4FUUzwajHikSpYNhTFRgabQN9CTNZJHZ53mn9ZY9SNTWVED65gxqGjoJKhQEFnwwNe6MSYxhMqVSy5Tf9qJstj7XwLfwe1skFWvCuLwCmxDDFekEXyqsscKaXuk7LDahpas336EEewxoqB4zsBiuCW7jzQ8uRHL7f7t2CrUVjiuTUe7DL7ihTs5rp2wK9zGUu8CGueQnvU5355wNu8sw42To1unxGJSV6VjrqLbdEcZtXDd2H1UwvVT5JDhDvF2GDfUqjQ7jREq4KaCs3wb6Yk3SCzzNWXsq77dbDGe2hj2svHk1iHUhbExu6vnxhguWrvaLA3HpftqNEBsEgQ59aPi4jjm3dMaqbib6nNh8e3Cs"
    ballotAddress shouldEqual "7gak11YHej2wnu7YFDxdNmQibKHrXjysJkcb2wRyq3Kqxcvo75HtchnCtUdL4YxY86NE6kV4m4PsWaPc3gTGLiBDSudMb2K8Ep48eVnJaUotoQmtH9A4mBkTyTaYD12Cogt5iLqPegFQYkLjdsBbPUzoGaNBk2KSZNHqNY23roZ78Ey13mRTSZatdb7jDVU4"
    ballotErgoTree.bytes.encodeHex shouldEqual "100504000580dac409040004000e20720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dded802d601b2a5dc0c1aa402a7730000d602e4c6a70407ea02d1ededed93e4c672010407720293c27201c2a793db63087201db6308a792c172017301eb02cd7202d1ed938cb2db6308b2a473020073030001730492c17201c1a7"
    epochPrepScriptHash shouldEqual "f7ef73c4a4ab91b84bb0a2905108d534114472ec057be3a57a9dfc9b1fbd85c1"
    liveEpochScriptHash shouldEqual "77dffd47b690caa52fe13345aaf64ecdf7d55f2e7e3496e8206311f491aa46cd"
    minPoolBoxValue shouldEqual 5000000

    // for MockUpdateSpec
    val newPool = new OraclePool {
      override def minOracleBoxes: Int = 5 // this is different
    }
    println("NewEpochPrepAddress: " + newPool.epochPrepAddress)
    newPool.epochPrepAddress shouldEqual "EfS5abyDe4vKFrJ48K7LESXx2QWyVx6Mh6aABHHuYmjm1eCQT1YcWaJUdurd3W9BSrQdjS8qXw7V3jcVtpuVNQQfxqQzpLJ3i5nQ2Fm1H11gGqc7fWM73HiWAa9gwa8Bw5wbUN6jocGVwyCfsSdAXAESKVJLpGCRce3aW6RqsCZevsg1coiNSTvZQjo8RdfaX411irMfBfYU4hU5UYvkrpJoPr6QgtmsuJym441trYrw5xHZgQFKG5FS8S226Fwx6GberrTeS5au1d7dh531imLWRShbRWMHAcWeYy1PeTpA8Nf7E8LCDwzY7wLJEdurYmZyYtyYM9itHHGf78R2rrW4JHvR71hnA28BxKmxixuzy28nssJT4RWGvEvqHX51t781cW431ZiFKLjkXzGd54odhDqKNMcUyaR9cxqoa7G91obCJS17DCLzajF1kuZYjjHEtHeteMpSWeENkBegmuEGwq4pKosTyMm4Q9DK93874no"
  }
}