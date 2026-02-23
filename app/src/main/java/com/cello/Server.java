package com.cello;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server {
    private static final int PORT = 80; // Port number for the server
    private static Map<String, Socket> clients; // Map to store connected clients
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);

            Thread acceptNewClient = new Thread(new AcceptThread(serverSocket));
            acceptNewClient.start();
        } catch(IOException e){
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public static class AcceptThread implements Runnable {
        private Socket remoteClient;
        private ServerSocket serverSocket;

        public AcceptThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
            this.remoteClient = null;
        }

        @Override
        public void run() {
            try {
                remoteClient = serverSocket.accept();
                String clientHost = remoteClient.getInetAddress().getHostAddress();
                int clientPort = remoteClient.getPort();
                String clientId = clientHost + "/" + clientPort;
                System.out.println("Client requesting connection: " + clientId);

                InputStream in = remoteClient.getInputStream();
                OutputStream out = remoteClient.getOutputStream();
                try (var clientInp = new BufferedReader(new InputStreamReader(in));
                     var clientOut = new BufferedWriter(new OutputStreamWriter(out))
                ){
                    Thread clientRecieving = new Thread(new InputStreamHandler(clientInp, clientId));
                    clientRecieving.start();
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    remoteClient.close();
                } catch (IOException ignored){};
            };
        }
    }

    public static class InputStreamHandler implements Runnable {
        private final BufferedReader in;
        public final String clientId;

        public InputStreamHandler(BufferedReader in, String clientId) {
            this.in = in;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                System.out.print("(" + clientId + ") connected");
                for(String inpLine; (inpLine = in.readLine()) != null;){
                    System.out.println("(" + clientId + "): " + inpLine);
                }
            } catch(IOException e) {
                System.err.println("Incoming stream had a fault.");
                throw new RuntimeException(e);
            } finally {
                try {
                    in.close();
                } catch (IOException ignored){};
            }
        }
    }

    private static void handleHandShake(BufferedReader in, BufferedWriter out) throws IOException {
        try {
            System.out.println("initialising connection...");
            String request = in.readLine();
            String key = extractClientKey(request);
            String response = createHandshakeResponse(key);
            out.write(response);
        } catch(NoSuchFieldException | NoSuchAlgorithmException e) {
            System.err.println("Handshake Failed due to a" + e.getClass().getSimpleName());
        }
    }

    private static String extractClientKey(String handshakeRequest) throws NoSuchFieldException {
        Pattern pattern = Pattern.compile("Sec-WebSocket-Key: (.+)");
        Matcher matcher = pattern.matcher(handshakeRequest);
        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            System.err.println("No client key found in request");
            throw new NoSuchFieldException("Bad request (400): Sec-WebSocket-Key not found");
        }
    }

    private static String createHandshakeResponse(String clientKey) throws NoSuchAlgorithmException {
        try {
            return ("HTTP/1.1 101 Switching Protocols\r\n"
                + "Connection: Upgrade\r\n"
                + "Upgrade: websocket\r\n"
                + "Sec-WebSocket-Accept: "
                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1")
                    .digest((clientKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                + "\r\n\r\n");
        } catch(NoSuchAlgorithmException e) {
            System.err.println("Could not create handshake response: " + e.getClass().getSimpleName());
            throw e;
        }
    }
}