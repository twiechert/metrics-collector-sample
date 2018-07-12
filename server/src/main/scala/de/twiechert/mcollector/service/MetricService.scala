package de.twiechert.mcollector.service

import com.google.inject.Provides
import com.paulgoldbaum.influxdbclient.{InfluxDB, Point}
import com.paulgoldbaum.influxdbclient.Parameter.Precision
import com.twitter.inject.TwitterModule
import de.twiechert.mcollector.config.Params
import javax.inject.Singleton
import de.twiechert.mcollector.common.domain.{CpuMetric, Metric, MetricReport, MetricTimeQuery}

import scala.concurrent.ExecutionContext.Implicits.global


object MetricModule extends TwitterModule {

  @Singleton
  @Provides
  def providesMetricService: MetricService = {
    new InfluxDbMetricService()
  }

}


/**
  * Trait that provides functionality around metric processing.
  */
trait MetricService {


  def writeMetrics(metricReport:MetricReport)

  def getUserMetrics(metricTimeQuery: MetricTimeQuery)

  def shutdown()

}

/**
  * Implementation of the metric service for InfluxDB
  */
class InfluxDbMetricService extends MetricService {

  private val influxdb = InfluxDB.connect(Params.METRICS_DB_HOST, Params.METRICS_DB_PORT)
  private val database = influxdb.selectDatabase(Params.METRICS_DB)


  override def getUserMetrics(metricTimeQuery: MetricTimeQuery): Unit = {
    database.query("SELECT * FROM cpu")
  }


  override def writeMetrics(metricReport: MetricReport): Unit = {
    val points = metricReport.metrics.map(metric => this.map(metricReport, metric))
    database.bulkWrite(points, Precision.MILLISECONDS)
  }


  private def map(metricReport:MetricReport, metric: Metric): Point = {
    val point = Point(Params.MEASUREMENTS_COLLECTION, metricReport.endTimestamp)
      .addTag("userId", metricReport.userId)
      .addTag("osVersion", metricReport.osVersion)
      .addTag("osType", metricReport.osType.toString)

    metric match {
      case cpuMetric: CpuMetric =>
        point.addField("cpuLoadAvg", cpuMetric.avgUtilization)
        point.addField("cpuLoadVar", cpuMetric.varUtilization)
      case _ =>
    }

    point
  }

  override def shutdown(): Unit = influxdb.close()

}
