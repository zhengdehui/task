package cn.dehui.zbj1984105;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;

public class KeywordRollingFileAppender extends FileAppender {

    private int  index      = 0;

    private int  maxLine;

    LoggingEvent firstEvent = null;

    public KeywordRollingFileAppender() {
        super();
    }

    public KeywordRollingFileAppender(Layout layout, String filename, boolean append) throws IOException {
        super(layout, filename, append);
    }

    public KeywordRollingFileAppender(Layout layout, String filename) throws IOException {
        super(layout, filename);
    }

    public void rollOver() {
        try {
            // This will also close the file. This is OK since multiple close operations are safe.  
            fileName = getRollingFileName(fileName, ++index);
            setFile(fileName, false, bufferedIO, bufferSize);
        } catch (IOException e) {
            if (e instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LogLog.error("setFile(" + fileName + ", false) call failed.", e);
        }
    }

    private String getRollingFileName(String fileName, int index) {
        Pattern p = Pattern.compile("\\.\\d+\\.csv");
        Matcher m = p.matcher(fileName);

        return m.replaceFirst(String.format(".%d.csv", index));
    }

    @Override
    protected void subAppend(LoggingEvent event) {
        super.subAppend(event);
        if (firstEvent == null) {
            firstEvent = event;
        }
        if (fileName != null && qw != null) {
            long lineCount = ((LineCountingQuietWriterextends) qw).getLineCount();
            if (lineCount >= maxLine) {
                rollOver();
                super.subAppend(firstEvent);
            }
        }
    }

    @Override
    protected void setQWForFiles(Writer writer) {
        this.qw = new LineCountingQuietWriterextends(writer, errorHandler);
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine + 1;
    }

    static class LineCountingQuietWriterextends extends CountingQuietWriter {
        protected long lineCount;

        public LineCountingQuietWriterextends(Writer writer, ErrorHandler errorHandler) {
            super(writer, errorHandler);
        }

        @Override
        public void write(String string) {
            super.write(string);
            lineCount++;
        }

        public long getLineCount() {
            return lineCount;
        }
    }
}
