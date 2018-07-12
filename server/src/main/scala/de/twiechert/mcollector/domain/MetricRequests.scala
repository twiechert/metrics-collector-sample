package de.twiechert.mcollector.domain

import com.twitter.finatra.request.RouteParam

/**
  * Case class describes the temporal query for metrics
  *
  * @param start the start timestamp to consider (left side interval)
  * @param end the end timestamp to consider (right side interval)
  * @param resampleRate the rate used for resampling
  */
case class MetricTimeQuery(start:String, end:String, resampleRate:String="15m")


case class UserMetricTimeQuery(@RouteParam("userId") userId:String, @RouteParam("metric") metric:String, start:String, end:String, resampleRate:String="15m")
