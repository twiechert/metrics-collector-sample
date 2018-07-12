package de.twiechert.mcollector.controller

import com.twitter.finatra.http.Controller
import de.twiechert.mcollector.service.MetricService
import javax.inject.Inject
import de.twiechert.mcollector.common.domain.MetricReport
import de.twiechert.mcollector.domain.UserMetricTimeQuery

class MetricController @Inject()(metricService: MetricService) extends Controller {

  val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")


  post("/api/v1/metrics") { metricReport: MetricReport =>
    metricService.writeMetrics(metricReport)
  }

  get("/api/v1/metrics/:metric/byUser/:userId") { query: UserMetricTimeQuery =>
    metricService.getUserMetrics(query)
  }

}
