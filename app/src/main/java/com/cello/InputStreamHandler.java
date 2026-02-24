package com.cello;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

import java.io.*;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import static com.cello.CLILoggingRoutines.closedConnectionException_seeStacktrace;

public class InputStreamHandler implements Runnable {
    private final BufferedReader in;
    public final String clientId;
    private final PrintWriter terminalWriter;
    private final LineReader terminalReader;
    private volatile BlockingQueue<String> outMessageQueue;
    private ObjectMapper json;

    public InputStreamHandler(InputStream in, Terminal terminal, BlockingQueue<String> outMessageQueue, String clientId) {
        this.clientId = clientId;
        this.outMessageQueue = outMessageQueue;
        this.terminalWriter = terminal.writer();
        this.terminalReader = LineReaderBuilder.builder().terminal(terminal).build();
        this.json = new ObjectMapper();
        this.in = new BufferedReader(new InputStreamReader(in));
    }

    @Override
    public void run() {
        try (in){
            for(String inpJson; (inpJson = in.readLine()) != null;) {
                Message inp = json.readValue(inpJson, Message.class);
                String msg = inp.getData();

                if (msg.equals("/end")) break;
//                TODO move timestamp func into message. generate stamp on creation
//                TODO Add ".formattedChat" for messages of type `CHAT`

                String timestamp = new SimpleDateFormat("MM-dd-yyyy|HH:mm:ss").format(new Date());
                String stampedMsg = "[" + timestamp + "] " + clientId + ": " + msg;

                outMessageQueue.add(stampedMsg);
                terminalWriter.println(stampedMsg);
                terminalWriter.flush();
            }
            terminalWriter.println("\n[" + clientId + "] Connection closed\n");
        } catch (SocketException | NullPointerException | EndOfFileException e) {
            // Socket was closed — this is normal on disconnect, not an error#
            terminalWriter.println("\n[" + clientId + "] Connection closed\n");
            terminalWriter.close();
            closedConnectionException_seeStacktrace(terminalReader.getTerminal(), e);
        } catch(Exception e) {
            System.err.println("Incoming stream had a fault: " + e.getClass().getSimpleName());
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
            } catch (IOException ignored){}
        }
    }
}
