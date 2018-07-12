package de.twiechert.mcollector.controller

import com.twitter.finatra.http.Controller
import de.twiechert.mcollector.service.MetricService
import javax.inject.Inject
import de.twiechert.mcollector.common.domain.MetricReport

class MetricController @Inject()(metricService: MetricService) extends Controller {

  val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")


  post("/api/v1/metrics") { metricReport: MetricReport =>
    metricService.writeMetrics(metricReport)
  }

}
