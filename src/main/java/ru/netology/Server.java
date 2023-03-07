package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.svg", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final int PORT = 9999;
    private final int NUMBER_OF_THREADS = 64;

    private final String PROTOCOL_VERSION = "HTTP/1.1 ";
    private final Map<String, Handler> handlerMap = new ConcurrentHashMap<>();

    public void start() {
        final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        try (final var serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.execute(() -> {
                    executorService.submit(connectionProceeding(socket));
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlerMap.put(method + " " + path, handler);
    }

    public Runnable connectionProceeding(Socket socket) {
        return () -> {
            try {
                handleConnection(socket);
                socket.close();
            } catch (IOException e) {
                System.out.println("Exception: " + e.getMessage());
            }
        };
    }

    public void handleConnection(Socket socket) throws IOException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = getRequest(in);
            if (request == null) {
                return;
            }


            final Handler handler = handlerMap.get(request.getMethod() + " " + request.getPath());
            if (handler == null) {

                if (!VALID_PATHS.contains(request.getPath())) {
                    notFoundResponse(out);
                } else {
                    contentIncludedResponse(out, request.getPath());
                }
            } else {
                handler.handle(request, out);
            }
            out.flush();
        }
    }

    private Request getRequest(BufferedReader in) throws IOException {
        final String requestText = in.readLine();
        final String[] parts = requestText.split(" ");
        if (parts.length != 3) {
            return null;
        }

        final StringBuilder header = new StringBuilder();
        final StringBuilder body = new StringBuilder();
        boolean hasBody = false;

        String inputText = in.readLine();
        while (inputText.length() > 0) {
            header.append(inputText);
            if (inputText.startsWith("Content-length: ")) {
                int index = inputText.indexOf(":") + 1;
                String str = inputText.substring(index).trim();
                if (Integer.parseInt(str) > 0) {
                    hasBody = true;
                }
            }
            inputText = in.readLine();
        }

        if (hasBody) {
            inputText = in.readLine();
            while (inputText != null && inputText.length() > 0) {
                body.append(inputText);
                inputText = in.readLine();
            }
        }
        return new Request(parts[0], parts[1], header.toString(), body.toString());
    }

    public void notFoundResponse(BufferedOutputStream out) throws IOException {
        Response response = new Response(StatusCode.NOT_FOUND, null, 0);
        generateHeaders(response, out);
    }

    public void contentIncludedResponse(BufferedOutputStream out, String path) throws IOException {
        Path filePath = Path.of(".", "public", path);
        String mimeType = Files.probeContentType(filePath);

        if (path.equals("/classic.html")) {
            final String template = Files.readString(filePath);
            final byte[] content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            Response response = new Response(StatusCode.OK, mimeType, content.length);
            generateHeaders(response, out);
            out.write(content);
            out.flush();
        } else {
            long length = Files.size(filePath);
            Response response = new Response(StatusCode.OK, mimeType, length);
            generateHeaders(response, out);
            Files.copy(filePath, out);
        }
    }

    private void generateHeaders(Response response, BufferedOutputStream out) throws IOException {
        StringBuilder responseBuild = new StringBuilder();
        responseBuild.append(PROTOCOL_VERSION).append(" ").append(response.getCode().code).append("\r\n");
        if (response.getContentType() != null) {
            responseBuild.append("Content-Type: ").append(response.getContentType()).append("\r\n");
        }
        responseBuild.append("Content-Length: ").append(response.getContentLength()).append("\r\n");
        responseBuild.append("Connection: close\r\n");
        responseBuild.append("\r\n");
        out.write(responseBuild.toString().getBytes());
    }

}
