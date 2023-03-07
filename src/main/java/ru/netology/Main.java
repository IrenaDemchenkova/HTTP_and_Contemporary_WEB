package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        final Server server = new Server();
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            final String text = "<h1>GET /messages</h1>\n" + "Headers: " + request.getHeaders();
            generateAnyData(text, responseStream);
        });
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            final String text = "<h1>POST /messages</h1>\n" + "Headers: " + request.getHeaders() + "\n" + "Body: " + request.getBody();
            generateAnyData(text, responseStream);
        });
        server.start();
    }

    private static void generateAnyData(String content, BufferedOutputStream out) throws IOException {
        final String responseBuilder = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + content.length() + "\r\n" + "Connection: close\r\n" + "\r\n";

        out.write(responseBuilder.getBytes());
        out.write(content.getBytes(StandardCharsets.UTF_8));
        System.out.println(responseBuilder);
        System.out.println(content);
    }
}
