package de.twiechert.mcollector

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.filters.{CommonFilters, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{HttpServer}
import de.twiechert.mcollector.controller.MetricsController
import de.twiechert.mcollector.service.{MetricModule}

object MetricServerMain extends HttpServer {


  override val modules = Seq(MetricModule)

  override protected def configureHttp(router: HttpRouter): Unit = {

    router
    //  .filter[TraceIdMDCFilter[Request, Response]]
     // .filter[CommonFilters]
      .add[MetricsController]
  }



}
