package de.twiechert.mcollector.controller

import com.twitter.finatra.http.Controller
import de.twiechert.mcollector.service.MetricService
import javax.inject.Inject
import de.twiechert.mcollector.common.domain.{CpuMetric, MetricReport}

class MetricsController @Inject()(metricService: MetricService) extends Controller {


  case class StatsRequest(id: Long, name: String)

  val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")


  post("/hi") { metricReport: MetricReport =>
    "Hello " + metricReport.userId + " with id "
  }

  post("/api/v1/metrics") { metricReport: MetricReport =>
    metricService.writeMetrics(metricReport)
    "Hello " + metricReport.userId + " with id "
  }

}
