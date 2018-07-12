package de.twiechert.mcollector.client

import java.util
import java.util.{Date, Locale}

import de.twiechert.mcollector.client.metric.CpuMetricProvider
import de.twiechert.mcollector.common.domain.MetricReport
import org.apache.http.NameValuePair
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import oshi.SystemInfo
import com.fasterxml.jackson.databind.{ObjectMapper, PropertyNamingStrategy}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object ClientApplication {

  private val metricProviders = List(new CpuMetricProvider)
  private val msBetweenMeasurements = 50
  private val measurementsPerAggregate = 100
  private val sysInfo: SystemInfo = new SystemInfo
  private val osType = this.determineOsType()
  private val osVersion = sysInfo.getOperatingSystem.getVersion.getVersion
  private val metricServiceBackendUrl = "http://localhost:8888/api/v1/metrics"
  private val mapper: ObjectMapper = this.configureObjectMapper()
  private val httpClient = HttpClientBuilder.create().build()


  def main(args: Array[String]): Unit = {
    println("This is the file sharing client app")
    while (true) {

      val startTimestamp = new Date().getTime
      for (i <- 1 to measurementsPerAggregate) {
        for (metricProvider <- this.metricProviders) {
          metricProvider.measure()
          Thread.sleep(msBetweenMeasurements)
        }
      }

      /**
        * The report suammrized all metrics collected during the last interval
        */
      val report = MetricReport(sysInfo.getHardware.getComputerSystem.getSerialNumber,
        startTimestamp,
        new Date().getTime,
        measurementsPerAggregate,
        this.osType,
        osVersion,
        metricProviders.map(provider => provider.getAggregatedMetric).toArray)

      this.sendReport(report)

    }

  }

  /**
    * Sends the current metric report through http to the backend
    * @param report the report to send to the backend system
    */
  def sendReport(report: MetricReport): Unit = {

    // ignore errors during prototype and just try next time again
    try {

      // create our object as a json string
      val reportJson = mapper.writeValueAsString(report)
      println(s"Sending $reportJson")

      val post = new HttpPost(metricServiceBackendUrl)
      val requestEntity = new StringEntity(
        reportJson,
        ContentType.APPLICATION_JSON)
      post.setEntity(requestEntity)

      // send the post request
      httpClient.execute(post)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def determineOsType():String = {
    System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH) match {
      case e if (e.indexOf("mac") >= 0) || (e.indexOf("darwin") >= 0) => "MacOS"
      case e if e.indexOf("win") >= 0 => "Windows"
      case _ => "Linux"

    }
  }

  private def configureObjectMapper() = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    mapper
  }

}