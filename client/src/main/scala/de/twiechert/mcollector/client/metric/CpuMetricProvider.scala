package de.twiechert.mcollector.client.metric

import de.twiechert.mcollector.common.domain.MetricProvider
import de.twiechert.mcollector.common.domain.CpuMetric
import oshi.SystemInfo
import oshi.hardware.CentralProcessor

class CpuMetricProvider extends MetricProvider[CpuMetric] {

  val sysInfo: SystemInfo = new SystemInfo
  val cpu: CentralProcessor = sysInfo.getHardware.getProcessor

  override def provideMetric(): CpuMetric = {
    CpuMetric(avgUtilization = this.getProcessCpuLoad, avgUtilizationByCore = this.getProcessCpuLoadPerCore)
  }

  override def aggregateMetrics(metrics: Seq[CpuMetric]): CpuMetric = {
    val mean = metrics.map(metric => metric.avgUtilization).sum / metrics.size
    val variance = metrics.map(metric => scala.math.pow(metric.avgUtilization - mean, 2)).sum / metrics.size
    val meanByCore = metrics.map(metric => metric.avgUtilizationByCore).foldLeft(Array.fill(this.getProcessorCoreCount)(0.0)) { (a, b) => a.zip(b).map { case (x, y) => x + y } }.map(_ / metrics.size)
    val varianceByCore = metrics.map(metric => metric.avgUtilizationByCore.zip(meanByCore).map { case (x, y) => scala.math.pow(x - y, 2) })
      .foldLeft(Array.fill(this.getProcessorCoreCount)(0.0)) { (a, b) => a.zip(b).map { case (x, y) => x + y } }.map(_ / metrics.size)
    CpuMetric(mean, variance, meanByCore, varianceByCore)


  }


  def getProcessCpuLoad: Double = {
    cpu.getSystemCpuLoad
  }

  def getProcessCpuLoadPerCore: Array[Double] = {
    cpu.getProcessorCpuLoadBetweenTicks
  }

  def getProcessorCoreCount: Int = {
    cpu.getLogicalProcessorCount
  }

}
