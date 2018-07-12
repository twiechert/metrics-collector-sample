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

  val metricProviders = List(new CpuMetricProvider)
  val msBetweenMeasurements = 50
  val measurementsPerAggregate = 100
  val sysInfo: SystemInfo = new SystemInfo
  val osType = this.determineOsType()
  val osVersion = sysInfo.getOperatingSystem.getVersion.getVersion
  val metricServiceBackendUrl = "http://localhost:8888/api/v1/metrics"
  val mapper: ObjectMapper = new ObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.setPropertyNamingStrategy(
    PropertyNamingStrategy.SNAKE_CASE)

  def main(args: Array[String]): Unit = {
    println("This is the file sharing client app")
    while (true) {

      val startTimestamp = new Date().getTime
      for(i <- 1 to measurementsPerAggregate) {
        for(metricProvider <- this.metricProviders) {
          metricProvider.measure()
          Thread.sleep(msBetweenMeasurements)
        }
      }

      val report =  MetricReport(sysInfo.getHardware.getComputerSystem.getSerialNumber,
        startTimestamp,
        new Date().getTime,
        measurementsPerAggregate,
        this.osType,
        osVersion,
        metricProviders.map(provider => provider.getAggregatedMetric() ).toArray)

      this.sendReport(report)

    }


  }

  def sendReport(report:MetricReport): Unit = {
    // create our object as a json string
    val reportJson = mapper.writeValueAsString(report);
    val client = HttpClientBuilder.create().build()

    val post = new HttpPost(metricServiceBackendUrl)

    // add name value pairs
    val nameValuePairs = new util.ArrayList[NameValuePair]()
    nameValuePairs.add(new BasicNameValuePair("JSON", reportJson))
    val requestEntity = new StringEntity(
      reportJson,
      ContentType.APPLICATION_JSON);
    post.setEntity(requestEntity)

    // send the post request
    val response = client.execute(post)
    println("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))
  }

  def determineOsType()={
    System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH) match {
      case e if (e.indexOf("mac") >= 0) || (e.indexOf("darwin") >= 0) =>  "MacOS"
      case e if e.indexOf("win")  >= 0 =>  "Windows"
      case _ =>  "Linux"

    }
  }

}