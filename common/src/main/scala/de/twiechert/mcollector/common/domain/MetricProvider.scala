package de.twiechert.mcollector.common.domain
import scala.collection.mutable

/**
  * A metric provider serves metrics of the given Type
  * @tparam E the metric type
  */
trait MetricProvider[E <: Metric] {

  var aggregatedMetricQueue = new mutable.Queue[E]

  /**
    * Obtains/calculates the respective metric
    * @return the obtained metric
    */
  def provideMetric(): E

  /**
    * Method that aggregates the set of provided metrics
    * @param metrics the metric to aggregate
    * @return the aggregated metric
    */
  def aggregateMetrics(metrics: Seq[E]): E

  /**
    * Aggregates all metrics collected in the internal queue
    * @return the aggregates metric
    */
  def getAggregatedMetric: E = {
    aggregateMetrics(aggregatedMetricQueue.dequeueAll(e => true))
  }

  /**
    * Measures the given metric and stores it in the internal queue
    */
  def measure(): Unit = {
    aggregatedMetricQueue+= this.provideMetric()
  }

}
