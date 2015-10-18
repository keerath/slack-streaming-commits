package com.imaginea.slack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Utils {

    private static final Utils INSTANCE = new Utils();
    private static final String COMMITTERS = "committers";
    private Utils() {

    }

    public static Utils getInstance() {
        return INSTANCE;
    }

    public void writeToFile(String buffCommitters) {
        try (AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Paths.get(COMMITTERS), StandardOpenOption.WRITE)) {
            asyncChannel.write(ByteBuffer.wrap(buffCommitters.getBytes()), asyncChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
