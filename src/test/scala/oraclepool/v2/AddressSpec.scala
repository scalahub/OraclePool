package oraclepool.v2

import kiosk.ergo._
import oraclepool.v2.OraclePool
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
    println("EpochPrepScriptHash: (for R6 of LiveEpochBox) " + epochPrepScriptHash)
    println("LiveEpochScriptHash: (for hard-coding in Datapoint script) " + liveEpochScriptHash)
    println("Min box value: " + minPoolBoxValue)

    liveEpochAddress shouldEqual "NTkuk55NdwCXkF1e2nCABxq7bHjtinX3wH13zYPZ6qYT71dCoZBe1gZkh9FAr7GeHo2EpFoibzpNQmoi89atUjKRrhZEYrTapdtXrWU4kq319oY7BEWmtmRU9cMohX69XMuxJjJP5hRM8WQLfFnffbjshhEP3ck9CKVEkFRw1JDYkqVke2JVqoMED5yxLVkScbBUiJJLWq9BSbE1JJmmreNVskmWNxWE6V7ksKPxFMoqh1SVePh3UWAaBgGQRZ7TWf4dTBF5KMVHmRXzmQqEu2Fz2yeSLy23sM3pfqa78VuvoFHnTFXYFFxn3DNttxwq3EU3Zv25SmgrWjLKiZjFcEcqGgH6DJ9FZ1DfucVtTXwyDJutY3ksUBaEStRxoUQyRu4EhDobixL3PUWRcxaRJ8JKA9b64ALErGepRHkAoVmS8DaE6VbroskyMuhkTo7LbrzhTyJbqKurEzoEfhYxus7bMpLTePgKcktgRRyB7MjVxjSpxWzZedvzbjzZaHLZLkWZESk1WtdM25My33wtVLNXiTvficEUbjA23sNd24pv1YQ72nY1aqUHa2"
    epochPrepAddress shouldEqual "EfS5abyDe4vKFrJ48K5HnwTqa1ksn238bWFPe84bzVvCGvK1h2B7sgWLETtQuWwzVdBaoRZ1HcyzddrxLcsoM5YEy4UnqcLqMU1MDca1kLw9xbazAM6Awo9y6UVWTkQcS97mYkhkmx2Tewg3JntMgzfLWz5mACiEJEv7potayvk6awmLWS36sJMfXWgnEfNiqTyXNiPzt466cgot3GLcEsYXxKzLXyJ9EfvXpjzC2abTMzVSf1e17BHre4zZvDoAeTqr4igV3ubv2PtJjntvF2ibrDLmwwAyANEhw1yt8C8fCidkf3MAoPE6T53hX3Eb2mp3Xofmtrn4qVgmhNonnV8ekWZWvBTxYiNP8Vu5nc6RMDBv7P1c5rRc3tnDMRh2dUcDD7USyoB9YcvioMfAZGMNfLjWqgYu9Ygw2FokGBPThyWrKQ5nkLJvief1eQJg4wZXKdXWAR7VxwNftdZjPCHcmwn6ByRHZo9kb4Emv3rjfZE"
    dataPointAddress shouldEqual "AucEQEJ3Y5Uhmu4o8dsrHy28nRTgX5sVtXvjpMTqdMQzBR3uRVcvCFbv7SeGuPhQ16AXBP7XWdMShDdhRy4cayZgxHSkdAVuTiZRvj6WCfmhXJ4LY2E46CytRAnkiYubCdEroUUX2niMLhjNmDUn4KmXWSrKngrfGwHSaD8RJUMEp5AGADaChRU6kAnh9nstkDN3"
    poolDepositAddress shouldEqual "4L1NEtpkMq6NeZhy2pk6omYvewovcHTm7CbxKm9djsbobAHSdDe6TVfmnW5THVpSHrG6rWovqv7838reswYE3UYkykWaNnhoyBGHFCdZvWqa2TVRtHiWcVaner6giUp55ZpELLuj9TtKePv6zrtMV5YE1o2kjrQEgGDoGHBGNuyx6ymXkSimcAQo1oD4f4tTcuBfWfp"
    updateAddress shouldEqual "3EF22tGF7kuzBR7cUu8Yv6k6Z6pNu9aBtcK9oVV7tmVYLbDuZjgkPR1P1cqP9LQx15kDZwq5mQknqiYR8yfB6do1rbTEvto7pxRniq675p9K9VJGJ5oYQKw7K2qeng8nJSDELyWWibc7sCcxnp7SuHwizNDCDFXu4mDS4KhWuNmu6LznXggJ6jZdkSwfTK8g2fgKcWLChqKb3MGKdPAhtaYpijqvPm9Wj3dezTNGmYpn61i932K33ECF1U9Q7rtZcwsS1hJR2LxZsjsZm2AVGU2ZF4L2b9xULu7Covr7np4KFcVHJE4UiYea43d79B3dLEdjNuREMtgtKaAVJz8nNFxENKSWzc3Ucfk7snzATbUdywkyJgTUaGMrQ2"
    ballotAddress shouldEqual "A3z9mP2XvBTb4TvT9msUNfR3fUEbxMipCWQ6GTDC9BXGWoV8ymiGP7byhAe5LGbumgEnKw1Ae97vofk9NyLJNNeW8mx98Ja65AWJN236Yu1bpr2Hctj61PkWLQc5WEicmugqmiSAGxpAvzg8sRrrzAEUBsgwcA2YAUeQFgbR13WKupmBhxW4S"
    ballotErgoTree.bytes.encodeHex shouldEqual "1003040004000e20720978c041239e7d6eb249d801f380557126f6324e12c5ba9172d820be2e1dded802d601b2a57ee4e300030400d602e4c6a70407ea02d1ededed93e4c672010407720293c27201c2a793db63087201db6308a793c17201c1a7eb02cd7202d1ed938cb2db6308b2a4730000730100017302efe6c672010504"
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
