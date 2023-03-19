package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        final Server server = new Server();
        server.addHandler(AllowedMethod.GET, "/messages", (request, responseStream) -> {
            final String text = "<h1>GET /messages</h1>\n" +
                    "<div>Path: " + request.getPath() + "</div>" +
                    "<div>Params: " + request.getQuery() + "</div>" +
                    "<div>Param key1: " + request.getQueryParam("key1") + "</div>" +
                    "<div>Headers: " + request.getHeaders() + "</div>";
            write(text, responseStream);
        });
        server.addHandler(AllowedMethod.POST, "/messages", (request, responseStream) -> {
            final String text = "<h1>POST /messages</h1>\n" +
                    "Headers: " + request.getHeaders() + "\n" +
                    "Body: " + request.getBody();
            write(text, responseStream);
        });
        server.start();
    }

    private static void write(String content, BufferedOutputStream out) throws IOException {
        final String responseBuilder = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + content.length() + "\r\n" +
                "Connection: close\r\n" + "\r\n";

        out.write(responseBuilder.getBytes());
        out.write(content.getBytes(StandardCharsets.UTF_8));
        System.out.println(responseBuilder);
        System.out.println(content);
    }
}
