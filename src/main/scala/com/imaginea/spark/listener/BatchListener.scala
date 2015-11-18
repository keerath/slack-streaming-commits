package com.imaginea.spark.listener

import java.io.File
import com.imaginea.spark.job.MonitorCommits
import scala.io.Source
import org.apache.spark.streaming.scheduler.{StreamingListenerReceiverStarted, StreamingListener, StreamingListenerBatchSubmitted}

class BatchListener extends StreamingListener {

  override def onBatchSubmitted(onBatchSubmitted: StreamingListenerBatchSubmitted) = {
    MonitorCommits.reloadMapOfNameVsAlias(new File(MonitorCommits.outputDir + File.separator + "Mappings"))
  }

  override def onReceiverStarted(receiverStarted: StreamingListenerReceiverStarted) = {
    val committerFile = new File(MonitorCommits.COMMITTERS)
    if (committerFile.exists) {
      val committers = Source.fromFile(committerFile).getLines()
      MonitorCommits.mutableSet.clear()
      if (committers.nonEmpty) {
        for (committer <- committers; if committer.trim().nonEmpty) {
          MonitorCommits.mutableSet += committer
        }
      }
    }
  }
}
