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
    private volatile BlockingQueue<Message> outMessageQueue;
    private ObjectMapper json;

    public InputStreamHandler(InputStream in, Terminal terminal, BlockingQueue<Message> outMessageQueue, String clientId) {
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
                terminalWriter.println("INCOMING JSON: " + inpJson);
                terminalWriter.println("\n==========================\n");
                Message msg = json.readValue(inpJson, Message.class);
                if(msg.type == Message.MessageType.CHAT) {
                    String msgText = msg.getData();

                    if (msgText.equals("/end")) break;
                    // TODO Add ".formattedChat" for messages of type `CHAT`

                    String stampedMsg = "[" + msg.getTimestamp() + "] " + clientId + ": " + msgText;

                    outMessageQueue.add(msg);
                    terminalWriter.println(stampedMsg);
                    terminalWriter.flush();
                }
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
