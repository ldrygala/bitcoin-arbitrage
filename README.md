# Arbitrage :bulb:
A solution for https://priceonomics.com/jobs/puzzle/. 
Written in Scala as [Ammonite Script](http://ammonite.io/#ScalaScripts).


```
rate(currency1, currency2) * rate(currency2, currency3) * ... * rate(currencyN, currency1) > 1
```

## Solution :hammer:
To find arbitrage opportunity, I'm using a modified version of [Bellman Ford algorithm](https://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm) which find cycle of negative sum. Where edge weight is `-log(rate)` because:

```
rate(currency1, currency2) * rate(currency2, currency3) * ... * rate(currencyN, currency1) > 1
```
is the same as
```
(-log(rate(currency1, currency2))) + (-log(rate(currency2, currency3))) + ... + (-log(rate(currencyN, currency1))) < 0
```

> Current version of algorithm find only single arbitrage possibility.

### Complexity :stopwatch:

- Time complexity `O(|V|*|E|)`
- Space complexity `O(|V|)`
  
Where `|V|` represents currencies and `|E|` represents exchanges between currencies.

### Run :fire:
You can run solution by calling:

`amm arbitrage.sc`

Output:
```
There is an arbitrage possibility: USD -> BTC -> USD
100.0 USD to 0.8518100000000001 BTC
0.8518100000000001 BTC to 116.67332671079501 USD
We can earn: 16.67332671079501%
```

if you don't have Ammonite you can install it using:

`sudo sh -c '(echo "#!/usr/bin/env sh" && curl -L https://github.com/lihaoyi/Ammonite/releases/download/2.3.8/2.13-2.3.8) > /usr/local/bin/amm && chmod +x /usr/local/bin/amm' && amm`


