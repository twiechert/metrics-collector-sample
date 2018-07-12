package de.twiechert.mcollector.common.domain

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}


/**
  * Represents the report of a client
  *
  * @param userId the userId that has issued the report
  * @param metrics teh sequence of metrics to be peristed
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

  def getName():String

}

case class MetricTimeQuery(start:Long,end:Long,resampleRateInMinutes:Int=15)


case class CpuMetric(avgUtilization:Double,
                     varUtilization:Double = 0,
                     avgUtilizationByCore:Array[Double],
                     varUtilizationByCore:Array[Double] = Array()) extends Metric {


  override def getName(): String = "CpuUtilization"
}


