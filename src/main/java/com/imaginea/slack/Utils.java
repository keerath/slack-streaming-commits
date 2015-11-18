package com.imaginea.slack;

import com.imaginea.spark.job.MonitorCommits;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class Utils {

    private static final Utils INSTANCE = new Utils();

    private Utils() {

    }

    public static Utils getInstance() {
        return INSTANCE;
    }

    public void writeToFile(String buffCommitters) {
        try {
            AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Paths.get(MonitorCommits.COMMITTERS()), StandardOpenOption.WRITE);
            asyncChannel.write(ByteBuffer.wrap(buffCommitters.getBytes()), asyncChannel.size(), asyncChannel, new CompletionHandler<Integer, AsynchronousFileChannel>() {

                @Override
                public void completed(Integer result, AsynchronousFileChannel attachment) {
                    try {
                        attachment.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, AsynchronousFileChannel attachment) {
                    exc.printStackTrace();
                    try {
                        attachment.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
