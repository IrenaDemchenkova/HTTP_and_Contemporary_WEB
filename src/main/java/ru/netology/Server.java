package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.svg", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    public final int PORT = 9999;
    public final int NUMBER_OF_THREADS = 64;

    private final Map<String, Handler> handlerMap = new ConcurrentHashMap<>();

    public void start() {
        final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.execute(() ->
                        executorService.submit(connectionProceeding(socket)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(AllowedMethod method, String path, Handler handler) {

        handlerMap.put(method + " " + path, handler);
    }

    public Runnable connectionProceeding(Socket socket) {
        return () -> {
            try {
                handleConnection(socket);
                socket.close();
            } catch (IOException e) {
                System.err.println("Exception: " + e.getMessage());
            }
        };
    }

    public void handleConnection(Socket socket) throws IOException {
        try (final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            Request request = Request.getRequest(socket.getInputStream());
            if (request == null) {
                Response.badRequest(out);
                return;
            }

            final Handler handler = handlerMap.get(request.getMethod() + " " + request.getPath());
            if (handler == null) {
                if (!VALID_PATHS.contains(request.getPath())) {
                    Response.notFoundResponse(out);
                } else {
                    Response.contentIncludedResponse(out, request.getPath());
                }
            } else {
                handler.handle(request, out);
            }
            out.flush();
        }
    }
}
