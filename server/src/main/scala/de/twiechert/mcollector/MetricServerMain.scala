package de.twiechert.mcollector

import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{HttpServer}
import de.twiechert.mcollector.controller.MetricController
import de.twiechert.mcollector.service.{MetricModule}

object MetricServerMain extends HttpServer {


  override val modules = Seq(MetricModule)

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add[MetricController]
  }



}
