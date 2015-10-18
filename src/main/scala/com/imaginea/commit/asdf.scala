package com.imaginea.commit

import java.io.{FileWriter, BufferedWriter, File}

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.WebSocketInputDStream._

object asdf {
  def main(args: Array[String]) = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("Monitor Commits")
    val ssc = new StreamingContext(conf, Seconds(1))
    val file = new File("myFile")
    file.createNewFile()
    val buff = new BufferedWriter(new FileWriter(file.getAbsoluteFile))

    val lines = ssc.webSocketTextStream("wss://ms369.slack-msgs.com/websocket/_0DnyPbzvUKjnXSTYfuXt5qyRQFsZod-V5fhmL2uZ2hroh-d2Twvr_Ry4ibZj4JIVfJ8WjTQ1pELqd2uvhKzL-NcvRNPcgOSsFl52pXjY6tNMUkO-j6KMHTFTleCX0WfKsrA87pqqE9Ntk2BMMADdg==", StorageLevel.MEMORY_ONLY_SER)
    lines.filter(x => x.contains("hello")).reduceByWindow(_ + _, Seconds(30), Seconds(30)).saveAsTextFiles("hello","info")
    lines.foreachRDD(rdd => rdd)
    ssc.start()
    ssc.awaitTermination()
  }
}