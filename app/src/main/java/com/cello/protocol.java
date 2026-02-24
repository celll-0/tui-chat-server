package com.cello;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class protocol {

    private static void handleHandShake(BufferedReader in, BufferedWriter out) throws IOException {
        try {
            System.out.println("initialising connection...");
            String request = in.readLine();
            String key = extractClientKey(request);
            String response = createHandshakeResponse(key);
            out.write(response);
            out.newLine();
            out.flush();
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
