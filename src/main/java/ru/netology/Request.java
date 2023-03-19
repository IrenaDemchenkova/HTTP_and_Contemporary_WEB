package ru.netology;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Request {
    static final int LIMIT = 4096;
    static final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};

    static final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
    private final String method;
    private final String path;
    private final HashMap<String, String> headers;
    private final String body;
    public Map<String, String> paramsQuery;
    public HashMap<String, String> postParams;

    public Request(String method, String path, Map<String, String> paramsQuery, HashMap<String, String> headers, String body, HashMap<String, String> postParams) {
        this.method = method;
        this.path = path;
        this.paramsQuery = paramsQuery;
        this.headers = headers;
        this.body = body;
        this.postParams = postParams;
    }

    public HashMap<String, String> getPostParams() {
        return this.postParams;
    }

    public String getPostParam(String name) {
        if (this.postParams == null) {
            return null;
        }

        return this.postParams.get(name);
    }

    static Request getRequest(InputStream in) throws IOException {
        in.mark(LIMIT);
        final byte[] buffer = new byte[LIMIT];
        final int read = in.read(buffer);

        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }

        final String[] requestLine = new String(Arrays.copyOfRange(buffer, 0, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        // Method and Path
        final String method = requestLine[0];
        if (!AllowedMethod.isValidMethod(method)) {
            return null;
        }

        final String path = requestLine[1];
        if (!path.startsWith("/")) {
            return null;
        }

        // Headers
        final int headersStart = requestLineEnd + requestLineDelimiter.length;
        final int headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);

        if (headersEnd == -1) {
            return null;
        }

        final String[] rawHeaders = new String(Arrays.copyOfRange(buffer, headersStart, headersEnd)).split("\r\n");
        HashMap<String, String> headers = new HashMap<>();
        for (String header : rawHeaders) {
            String[] headerKeyValue = header.split(": ");
            headers.put(headerKeyValue[0], headerKeyValue[1]);
        }

        // parsing body
        String body = null;
        if (!method.equals("GET")) {
            body = new String(Arrays.copyOfRange(buffer, headersEnd + headersDelimiter.length, read));
        }

        HashMap<String, String> postParams = null;
        String contentType = headers.get("Content-Type");
        if (contentType != null && contentType.equals("application/x-www-form-urlencoded")) {
            String[] params = body.split("&");
            postParams = new HashMap<>();

            for (String param : params) {
                String[] keyValueParam = param.split("=");
                postParams.put(keyValueParam[0], keyValueParam[1]);
            }
        }

        return new Request(method, path.split("\\?")[0], getQueryParams(path), headers, body, postParams);
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static String getQueryFromURL(String path) {
        if (path.contains("?")) {
            return path.substring(path.indexOf("?") + 1);
        }
        return null;
    }

    public static Map<String, String> getQueryParams(String url) {
        HashMap<String, String> queryParams = new HashMap<>();

        if (url.contains("?")) {
            String query = getQueryFromURL(url);
            URLEncodedUtils.parse(query, StandardCharsets.UTF_8)
                    .forEach(param -> queryParams.put(param.getName(), param.getValue()));
        }

        return queryParams;
    }

    public String getQueryParam(String paramName) {
        return this.paramsQuery.get(paramName);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQuery() {
        return this.paramsQuery;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}