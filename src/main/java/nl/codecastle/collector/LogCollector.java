package nl.codecastle.collector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class LogCollector {
    private ByteArrayOutputStream arrayOutputStream;
    private PrintStream sysOut;
    private boolean capturing = false;

    public void start() {
        if (capturing) {
            return;
        }

        capturing = true;
        sysOut = System.out;
        arrayOutputStream = new ByteArrayOutputStream();

        OutputStream outputStreamCombiner =
                new OutputStreamCombiner(Arrays.asList(sysOut, arrayOutputStream));
        PrintStream newSysOut = new PrintStream(outputStreamCombiner);

        System.setOut(newSysOut);
    }

    public String stop() {
        if (!capturing) {
            return "";
        }

        System.setOut(sysOut);

        String capturedValue = arrayOutputStream.toString();
        try {
            arrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        arrayOutputStream = null;
        sysOut = null;
        capturing = false;

        return capturedValue;
    }

    private static class OutputStreamCombiner extends OutputStream {
        private List<OutputStream> outputStreams;

        public OutputStreamCombiner(List<OutputStream> outputStreams) {
            this.outputStreams = outputStreams;
        }

        public void write(int b) throws IOException {
            for (OutputStream os : outputStreams) {
                os.write(b);
            }
        }

        public void flush() throws IOException {
            for (OutputStream os : outputStreams) {
                os.flush();
            }
        }

        public void close() throws IOException {
            for (OutputStream os : outputStreams) {
                os.close();
            }
        }
    }
}
