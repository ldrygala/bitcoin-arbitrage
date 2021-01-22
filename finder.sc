import $file.model, model.Exchange
import NegativeCycleFinder.Edge
import scala.annotation.tailrec

class NegativeCycleFinder[A] private (edgeWeights: Map[Edge[A], Double]) {
  case class State(
      bestPaths: Map[A, Double],
      predecessors: Map[A, Option[A]],
      relaxed: Option[A]
  )

  val vertices = edgeWeights.keySet.flatMap { edge =>
    Set(edge.from, edge.to)
  }

  def findNegativeCycle(): Option[List[A]] = {
    val State(_, predecessors, lastRelaxedOpt) = getRelaxedState()

    for {
      lastRelaxed   <- lastRelaxedOpt
      startVertex   <- getVertexInNegativeCycle(lastRelaxed, predecessors)
      negativeCycle <- buildNegativeCycle(startVertex, predecessors)
    } yield negativeCycle
  }

  private def getRelaxedState() = {
    val numberOfIterations = vertices.size

    val initState = State(
      vertices.map(v => v -> 0d).toMap,
      vertices.map(v => v -> None).toMap,
      None
    )

    @tailrec
    def go(iteration: Int, currentState: State): State =
      // iterante n times to over relax graph to see if something will change in Nth iteration.
      // if yes then we have negative cycle
      if (iteration < numberOfIterations) {
        val relaxedState = relax(currentState.copy(relaxed = None))
        if (currentState.bestPaths == relaxedState.bestPaths) {
          currentState
        } else {
          go(iteration + 1, relaxedState)
        }
      } else {
        currentState
      }

    go(0, initState)
  }

  private def relax(state: State): State =
    edgeWeights.foldLeft(state) { case (currentState, (edge, weight)) =>
      val from                = edge.from
      val to                  = edge.to
      val currentBestPaths    = currentState.bestPaths
      val currentPredecessors = currentState.predecessors
      val currentRelaxed      = currentState.relaxed

      (currentBestPaths.get(from), currentBestPaths.get(to)) match {
        case (Some(fromWeight), Some(toWeight)) =>
          if (toWeight > (fromWeight + weight))
            State(
              currentBestPaths.updated(to, (fromWeight + weight)),
              currentPredecessors.updated(to, Some(from)),
              Some(to)
            )
          else
            State(
              currentBestPaths,
              currentPredecessors,
              currentRelaxed
            )
        case (Some(fromWeight), None) =>
          State(
            currentBestPaths.updated(to, (fromWeight + weight)),
            currentPredecessors.updated(to, Some(from)),
            Some(to)
          )
        case (_, _) =>
          State(
            currentBestPaths,
            currentPredecessors,
            currentRelaxed
          )
      }
    }

  private def getVertexInNegativeCycle(
      lastRelaxed: A,
      predecessors: Map[A, Option[A]]
  ): Option[A] = {

    @tailrec
    def getPredecessor(last: A, iteration: Int): Option[A] =
      // find n-1th predecessor to be sure that this vertex is in the negative cycle
      // needed because last relaxed vertex could be reachable from negative cycle but not belongs to it
      if (iteration < vertices.size - 1) {
        predecessors.get(last).flatten match {
          case Some(predecessor) => getPredecessor(predecessor, iteration + 1)
          case None              => None
        }
      } else {
        Some(last)
      }

    getPredecessor(lastRelaxed, 0)
  }

  private def buildNegativeCycle(startVertex: A, predecessors: Map[A, Option[A]]): Option[List[A]] = {
    def getPredecessor(currentVertex: A): Option[A] =
      predecessors.get(currentVertex).flatten

    @tailrec
    def build(
        currentVertex: A,
        currentCycle: List[A]
    ): Option[List[A]] =
      getPredecessor(currentVertex) match {
        case Some(predecessor) =>
          if (predecessor == startVertex) {
            Some(predecessor :: currentCycle)
          } else {
            build(predecessor, predecessor :: currentCycle)
          }
        case None => None
      }

    build(startVertex, List(startVertex))
  }
}

object NegativeCycleFinder {
  private case class Edge[A](from: A, to: A)

  def forExchangeRates(exchangeRates: Map[Exchange, Double]): NegativeCycleFinder[String] = {
    val graph = exchangeRates.map { case (exchange, rate) =>
      Edge(exchange.from, exchange.to) -> -Math.log(rate)
    }
    new NegativeCycleFinder(graph)
  }
}
