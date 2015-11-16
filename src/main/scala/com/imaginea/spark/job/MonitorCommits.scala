package com.imaginea.spark.job

import java.io._
import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.util.Date

import com.imaginea.slack.{SlackAuthen, Utils}
import com.imaginea.spark.listener.BatchListener
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.WebSocketInputDStream._
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

import scala.collection.mutable
import scala.io.Source

object MonitorCommits {
  val token = Source.fromFile("src/main/resources/Config").getLines().toStream.head
  val mutableSet = new mutable.HashSet[String]()
  val checkPointDir = System.getProperty("user.home") + File.separator + "checkpoint-data"
  val nameVsAlias = mutable.HashMap[String, String]()
  val buffer = mutable.Buffer[String]()
  val dateFormat = new SimpleDateFormat("dd/MM/yyyy")
  val COMMITTERS = "committers"

  def main(args: Array[String]): Unit = {
    val webSoketURL = new SlackAuthen().tokenAuthen(token).getURL
    if (!Files.isDirectory(Paths.get(checkPointDir))) {
      Files.createDirectory(Paths.get(checkPointDir))
    }
    val ssc = StreamingContext.getOrCreate(checkPointDir, () => streamingJob(webSoketURL))
    ssc.addStreamingListener(new BatchListener)
    ssc.start()
    ssc.awaitTermination()
  }

  def streamingJob(webSocketURL: String) = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("Monitor Commits")
    val ssc = new StreamingContext(conf, Seconds(1))
    val sqlContext = new SQLContext(ssc.sparkContext)
    val commitMessages = ssc.webSocketStream(webSocketURL, getCommitMessages, StorageLevel.MEMORY_AND_DISK_2)
    val committers = commitMessages.transform({ rdd => {
      if (!rdd.isEmpty()) {
        val commitInfo = sqlContext.read.json(rdd).select("attachments.text")
        commitInfo.flatMap(_.getSeq[String](0).map(extractCommitters))
      } else {
        rdd.map(x => new mutable.HashSet[String]())
      }
    }
    }).reduceByWindow(_ ++ _, Minutes(60 * 24), Minutes(60 * 24))

    val nonCommitters = committers.map(mutableSet.diff)
    nonCommitters.map(_.mkString("\n")).saveAsTextFiles("Non-committers " + dateFormat.format(new Date()), "info")

    committers.foreachRDD({ rdd => rdd.foreach(record => record.foreach({ x =>
      mutableSet.add(x) match {
        case true => buffer += x
        case false => buffer
      }
    }))
      writeBufferToFile()
    })
    ssc.checkpoint(checkPointDir)
    ssc
  }

  def writeBufferToFile(): Unit = {
    if (buffer.nonEmpty) {
      new File(COMMITTERS).createNewFile
      Utils.getInstance.writeToFile(buffer.distinct.mkString("\n"))
      buffer.clear()
    }
  }

  def getCommitMessages(slackMessage: String) = {
    if (slackMessage.contains("new commit") &&
      slackMessage.contains("attachments") &&
      slackMessage.contains("github")) {
      Some(slackMessage)
    } else {
      None
    }
  }

  def extractCommitters(text: String): mutable.HashSet[String] = {
    val committers = new mutable.HashSet[String]()
    val lines = text.split("\\r?\\n")
    for (line <- lines; if !(line.contains("Merge") || line.contains("merge"))) {
      val indexOf: Int = line.lastIndexOf("-")
      if (indexOf != -1) {
        committers += replaceAlias(line.substring(indexOf + 1).trim, nameVsAlias)
      }
    }
    committers
  }

  def reloadMapOfNameVsAlias(file: File): Unit = {
    val lines = Source.fromFile(file).getLines()
    nameVsAlias.clear()
    nameVsAlias ++= lines.foldLeft(Map[String, String]())((z, y) => {
      val arr = y.split("->")
      z ++ Map(arr(0).trim -> arr(1).trim)
    })
  }

  def replaceAlias(name: String, nameVsAlias: mutable.HashMap[String, String]): String = {
    nameVsAlias.get(name) match {
      case Some(x) => x
      case None => name
    }
  }
}

