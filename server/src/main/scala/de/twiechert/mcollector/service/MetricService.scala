package de.twiechert.mcollector.service

import java.text.SimpleDateFormat

import com.google.inject.Provides
import com.paulgoldbaum.influxdbclient.{InfluxDB, Point}
import com.paulgoldbaum.influxdbclient.Parameter.Precision
import com.twitter.inject.TwitterModule
import de.twiechert.mcollector.config.Params
import javax.inject.Singleton
import de.twiechert.mcollector.common.domain._
import de.twiechert.mcollector.domain.UserMetricTimeQuery

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


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


  /**
    * Writes the give metric object to the data store
    * @param metricReport
    */
  def writeMetrics(metricReport: MetricReport)

  def getUserMetrics(metricTimeQuery: UserMetricTimeQuery): Future[UnivariateTimeseries]

  /**
    * Closes the connection to the data store
    */
  def shutdown()

}

/**
  * Implementation of the metric service for InfluxDB
  */
class InfluxDbMetricService extends MetricService {

  private val influxdb = InfluxDB.connect(Params.METRICS_DB_HOST, Params.METRICS_DB_PORT)
  private val database = influxdb.selectDatabase(Params.METRICS_DB)
  private val influxDateFormat  = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss'Z'")

  // @TODO replace metric by enum
  override def getUserMetrics(metricTimeQuery: UserMetricTimeQuery): Future[UnivariateTimeseries] = {
    val response = database.query(s""" SELECT MEAN(${metricTimeQuery.metric}) FROM "${Params.MEASUREMENTS_COLLECTION}" WHERE time > '${metricTimeQuery.start}' and time < '${metricTimeQuery.end}' GROUP BY time(${metricTimeQuery.resampleRate}) """ )
    response transform {
      case Success(records) =>
        val timeseriesEntries = records.series.head.records
          .filter(record => record.allValues.size > 1 && record.allValues.head != null && record.allValues(1) != null )
          .map( record => UnivariateTimeseriesEntry(influxDateFormat.parse(record.allValues.head.asInstanceOf[String]), record.allValues(1).asInstanceOf[BigDecimal].toDouble ))
        Try (UnivariateTimeseries(timeseriesEntries))

       // in future return proper error object
      case Failure(t) =>
        println("An error has occurred: " + t.getMessage)
        Try(UnivariateTimeseries(Seq()))

    }
  }


  override def writeMetrics(metricReport: MetricReport): Unit = {
    val points = metricReport.metrics.map(metric => this.map(metricReport, metric))
    database.bulkWrite(points, Precision.MILLISECONDS)
  }


  private def map(metricReport: MetricReport, metric: Metric): Point = {
    var point = Point(Params.MEASUREMENTS_COLLECTION, metricReport.endTimestamp)
      .addTag("userId", metricReport.userId)
      .addTag("osVersion", metricReport.osVersion)
      .addTag("osType", metricReport.osType.toString)

    point = metric match {
      case cpuMetric: CpuMetric => {
        val coreAvgMax = cpuMetric.avgUtilizationByCore.toSeq.max
        val coreVarMax = cpuMetric.varUtilizationByCore.toSeq.max
        val coreAvgMin = cpuMetric.avgUtilizationByCore.toSeq.min
        val coreVarMin = cpuMetric.varUtilizationByCore.toSeq.min

        point.addField("cpuLoadAvg", cpuMetric.avgUtilization)
          .addField("cpuLoadVar", cpuMetric.varUtilization)
          .addField("cpuCoreLoadAvgMax", coreAvgMax)
          .addField("cpuCoreLoadAvgMin", coreAvgMin)
          .addField("cpuCoreLoadVarMax", coreVarMax)
          .addField("cpuCoreLoadMinMin", coreVarMin)
      }

      case _ => point
    }

    point
  }

  override def shutdown(): Unit = influxdb.close()

}
