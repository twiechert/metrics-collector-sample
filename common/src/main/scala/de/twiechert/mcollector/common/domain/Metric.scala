package de.twiechert.mcollector.common.domain

import java.util.Date

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

/**
  *
  * @param userId the user id
  * @param startTimestamp when collection for the contained aggregates has started
  * @param endTimestamp when collection for the contained aggregates has ended
  * @param observationsPerMetric the count of observations per metric used to calculate the aggregates
  * @param osType the os type
  * @param osVersion the os string
  * @param metrics the array of aggregates for the respective metric
  */
case class MetricReport(userId:String,
                        startTimestamp:Long,
                        endTimestamp:Long,
                        observationsPerMetric:Int,
                       // Scala enums cause problems with GSON
                        osType:String,
                        osVersion:String,
                        metrics: Array[Metric])

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type")
@JsonSubTypes(Array(
  new Type(value = classOf[CpuMetric], name = "CPU"),
))
trait Metric {

  def getName:String

}


/**
  * Metric used to describe the utilization/load of the CPU resource
  *
  * @param avgUtilization the average utilization of the complete CPU
  * @param varUtilization the variange of this utilization in case this value has been aggregated
  * @param avgUtilizationByCore same as avgUtilization but for each vcore
  * @param varUtilizationByCore same as varUtilization but for each vcore
  */
case class CpuMetric(avgUtilization:Double,
                     varUtilization:Double = 0,
                     avgUtilizationByCore:Array[Double],
                     varUtilizationByCore:Array[Double] = Array()) extends Metric {

  override def getName: String = "CpuUtilization"
}


case class UnivariateTimeseriesEntry(date:Date, value:Double)

case class UnivariateTimeseries(entries:Seq[UnivariateTimeseriesEntry])
