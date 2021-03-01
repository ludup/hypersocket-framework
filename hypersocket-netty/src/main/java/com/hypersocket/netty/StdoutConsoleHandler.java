package com.hypersocket.netty;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class StdoutConsoleHandler extends StreamHandler {

    public StdoutConsoleHandler() {
        setOutputStream(System.out);
        setLevel(Level.ALL);
        setFormatter(new SimpleFormatter());
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    @Override
    public void close() {
        flush();
    }
}