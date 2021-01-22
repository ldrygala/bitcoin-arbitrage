import $file.client, client.SttpExchangeRatesClient
import $file.finder, finder.NegativeCycleFinder
import $file.model, model.Exchange

private def displayArbitrage(exchangeRates: Map[Exchange, Double])(negativeCycle: List[String]): Unit = {
  println("There is an arbitrage possibility: " + negativeCycle.mkString(" -> "))
  val startValue = 100.0
  val endValue = negativeCycle.sliding(2).foldLeft(startValue) { case (value, List(fromCurrency, toCurrency)) =>
    val rate = exchangeRates.getOrElse(
      Exchange(fromCurrency, toCurrency),
      throw new IllegalArgumentException(s"There is no exchange rate from $fromCurrency to $toCurrency")
    )
    val exchangedValue = value * rate
    println(s"$value $fromCurrency to $exchangedValue $toCurrency")
    exchangedValue
  }
  val profit = ((endValue - startValue) / startValue) * 100.0
  println(s"We can earn: $profit%")
}

SttpExchangeRatesClient
  .fetchExchangeRates()
  .fold(
    errorReason => println(s"Couldn't fetch exchange rates because of: $errorReason"),
    exchangeRates => {
      val negativeCycleFinder =
        NegativeCycleFinder.forExchangeRates(exchangeRates)
      val negativeCycleOpt = negativeCycleFinder.findNegativeCycle()

      negativeCycleOpt
        .map(displayArbitrage(exchangeRates))
        .getOrElse(println("There is no arbitrage possibility."))
    }
  )
