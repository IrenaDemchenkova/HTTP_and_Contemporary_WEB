package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Response {
    private static final String PROTOCOL_VERSION = "HTTP/1.1 ";
    private final StatusCode code;
    private final String contentType;
    private final long contentLength;

    public Response(StatusCode code, String contentType, long contentLength) {
        this.code = code;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    public static void notFoundResponse(BufferedOutputStream out) throws IOException {
        Response response = new Response(StatusCode.NOT_FOUND, null, 0);
        generateHeaders(response, out);
    }

    public static void badRequest(BufferedOutputStream out) throws IOException {
        Response response = new Response(StatusCode.BAD_REQUEST, null, 0);
        generateHeaders(response, out);
    }

    public static void contentIncludedResponse(BufferedOutputStream out, String path) throws IOException {
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

    private static void generateHeaders(Response response, BufferedOutputStream out) throws IOException {
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

    public StatusCode getCode() {
        return code;
    }

    public String getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

}
