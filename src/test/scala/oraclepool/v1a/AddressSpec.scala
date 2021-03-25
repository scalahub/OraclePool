package oraclepool.v1a

import kiosk.ergo._
import org.scalatest.{Matchers, PropSpec}

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
    // for MockUpdateSpec
    val newPool = new OraclePool {
      override def minOracleBoxes: Int = 5 // this is different
    }
    println("NewEpochPrepAddress: " + newPool.epochPrepAddress)

    liveEpochAddress shouldEqual "NTkuk55NdwCa9goicKq7MQ4rvdG95wEkw1oZMUnUG2M8CpF7jDGVXSFqHFjCj45PE3fbsR4BrDgMqYoqWvtV5R8nDUvbf7RckyEomX6Mm3nuCvQmp6dMXcL99WymtPmrmVgww8qKXVAo1MXFygJFR5Y41s9TQcktTFkZ5eRCeY3pwnA38JBDeMr3hewvkjMpk5DUuyEACzuawv16iDmQw9DEGuzfw91JwxN5SrdnwPBP4F3dLzub3s6NnMi774Lb7o1TnZUhX1xMDXkR4XM7whsUprZBXXaNFXYb8sVmPNCWcUGaVqrc1nLp8YrmShg4MvP9sg155ZBt2CfdbQG23kH3t15gNtj6ECrQFQ6ZDBmR2mspvRcXvVdaJoScoXjWBdotNrpnSEZBXrHX214LzZj3JB26Lam5EY8CymzJXqPwF26Zrx2TfGEqYuPwa7Ko6UpazJPPpPfqersBWTHmPuD18f58X72Y4vy9VSJh567b4yQSwEUxBmLDF5eek7AUx3zy9hBGyoD52u2yCznSdaKyVzfgkzfdsXu3SXtq6fLn8gwmDzMU5E8qJj"
    epochPrepAddress shouldEqual "EfS5abyDe4vKFrJ48K1o6f4psAJSEwhidbnxaSxyVQikRV45JRotq569Vb5mR79xoHYhH1m2MbD8JHPVuSYaA9r7ucXp9tT2aKuuq7qCCtBRaKbXaqNXMhBUDcHjnFbirbsf4xWnVDLQjVTCpsWcj15Faf5VBw73HkxGYxFe9ju44rfSFs4wx653LdN18ptw6sfH5SURR2SfYrRUeqxXpWF3QUsKYryryFobfvdr9k3tkeeMsUFqbGGf2skfWMrchyXxgkWBR6aCZrLpak9zeFwCQHxaeBC8Soj2h53amNgDsQLpLxtjGvc2m3pMbWohZ7QTj2yFsxxUoQBTvbtYNxrn8xo7VyXUury9CZ3CBDPRtgfsDm5d8QA3RmDSZboXd1CAPRSnUJp4R9z5ojzoTUoB7711CyPKgrQCN9KJRbxWB2J8RemUTLmqu3d96epaFzcuafmpvH93BWZTn71FXXB3SwzS51n9bgGTzqhoYLNVviz"
    dataPointAddress shouldEqual "AucEQEJ3Y5Uhmu4o9DkTkDqWaEiKjozV5AGCdnXwEsFqgNsW8W8ACME4MpMFZD16qaWHcwMbPJ3ULtTn9xX5A6ooWEtAA4sgMYSG47rZPKi3LYkrLx93uUZpkFi7FAURPJJ1of67DimC3FNiUpgfrWB5zsvFrWibSA2uh8FFmjHanASMJHXGejGiSfJD1XCGhGBJ"
    poolDepositAddress shouldEqual "4L1NEtpkMq6Pw1oduba32faqKdukqN3kjfKu7C4JA5cGkm1XMbvmuSisbsFjH1gede1HC3BU4KxXwx4888i5tV2gdch8fHjTeUSLXtcX9VNsLTWb7wMVz6YGHrZZiFGHfbx2dfGDjLrM4Bpb85TvycryviFyzFsgcQCqwiXuEvDh5K2sBvZ2vo3uLqPuiqRvjAN9zLT"
    updateAddress shouldEqual "Su1rWdkh3KvNSBUiKjq2LvsWKJVbFcLQBdHM8WC1KoDL3ZHrY2zGox7DUfnasnmjk8y5LG8nFm3U7NG7fctBuo6WXveKFvgucUYKif2nnW4FZkt9Eudr2YWNN5h8k3MGUouZcPJTKGCJ1gW7UGiYWELC5XmfM5JbF84S4yn55TjjsdNXwatEgHjPgXzSfGo39z1JrTiiNTqRoew26ckbfpsS8D8XZQtg4irLukFUAen95WSzNEx7awcCRWXzn8Y7n9hYE83iKfB7KSBwdM616TpB2Sv3jAdx562pvBWgcHRDDgvL3nKof1w2STQxuWNSv2y2E1M6cy4kcVL8DPu8QAL6v51W7Eq9b938btUJGkDoN4BqjywDLiowVtNvKtFaFbC35M8uEbjLd5zuqTpU3z9ejxDCBv7Er"
    ballotAddress shouldEqual "7gak11YHej2wnu7YFDxdNtoXZrvc2iysCubCkeAT1rTnuNxKSh35tZCynt1DMjBydS7eS1SJwEM6pruA9fk6MjZFMGpNmUFW61CgEFve5dYcDXBQxf98oh5rnL3LRz6V1QfubNzGt8WDbcijvtCHNSWxmbMmV8Dit4rzHjGjuVp6j2ZjdijmqaDwf7T1qfrW"
    ballotErgoTree.bytes.encodeHex shouldEqual "100504000580dac409040004000e20dd26438230986cfe7305ad958451b69e55ad5ac37c8a355bfb08d810edd7a20fd802d601b2a5dc0c1aa402a7730000d602e4c6a70407ea02d1ededed93e4c672010407720293c27201c2a793db63087201db6308a792c172017301eb02cd7202d1ed938cb2db6308b2a473020073030001730492c17201c1a7"
    epochPrepScriptHash shouldEqual "138439d3faad36b53970feba95f0d9c0f74eb5b7ba8bfdb99614c8f22727ed26"
    liveEpochScriptHash shouldEqual "2e25aec775cb0acbc29751ae3e3272e9a9ce781955216c04be4624c3d8744ee9"
    minPoolBoxValue shouldEqual 5000000
    newPool.epochPrepAddress shouldEqual "EfS5abyDe4vKFrJ48K2hzLcDkBM7bR8ARsZ9imh8wsQ1QurtT84gb3iqbpHdg83aytMcaU14JudxiD1EzNtiS7eCMY2WgwP1Aq5NGVHjJB8WVmMqCiJkk3KUXHM1QGVjsQWz9NPYXgtFx8M4rW7dSqyJzFLv1wX4gCXNtZrg4No5ZQRKNPD4kuGmfiLgGBJfq5WegqLsmC8zvd9Cnb99ub8Gfs5B8kbTb5U22VUFFECkXn17L5CZeFeZLrQEdCC4GhompZRrxjuvpbsjAoHTeaSp2cViL2r66gftmyLtMxoEwLTFVo8PeA53rzhv5xS6BrAqMM7fHvvzpkbkhUVSRPKy6yBesDnrygBxB72iPqq7nDikFtA4cLchFypJytXbgzppuyyBC5SZ93xyBeLEiSRMoM3VPDUMwUS6FwtYMXPDh3WC2LZxJWgf9KtraTYXAUTD7nPct85ZL8mFsLa7itK5iPv2BAwd8AqADqZNRus2PrL"

  }
}
