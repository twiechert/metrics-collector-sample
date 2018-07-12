package de.twiechert.mcollector.common.domain
import scala.collection.mutable

trait MetricProvider[E <: Metric] {

  var aggregatedMetricQueue = new mutable.Queue[E]

  def provideMetric(): E

  def aggregateMetrics(metrics: Seq[E]): E

  def getAggregatedMetric(): E = {
    aggregateMetrics(aggregatedMetricQueue.dequeueAll(e => true))

  }

  def measure() = {
    aggregatedMetricQueue+= this.provideMetric()
  }

}
