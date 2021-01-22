import $ivy.`com.softwaremill.sttp.client3::core:3.0.0`
import $ivy.`com.softwaremill.sttp.client3::circe:3.0.0`
import $file.model, model.Exchange
import io.circe.KeyDecoder
import sttp.client3._
import sttp.client3.circe._

object SttpExchangeRatesClient {
  implicit val exchangeDecoder: KeyDecoder[Exchange] =
    KeyDecoder.decodeKeyString
      .map { currencyPair =>
        val Array(from, to) = currencyPair.split("_", 2)
        Exchange(from, to)
      }

  val backend = HttpURLConnectionBackend()

  def fetchExchangeRates(): Either[String, Map[Exchange, Double]] = 
    basicRequest
      .get(uri"http://fx.priceonomics.com/v1/rates/")
      .response(asJson[Map[Exchange, Double]])
      .send(backend)
      .body
      .left
      .map(_.getMessage)
}
