package at.fhtw.httpserver.server;

import java.util.HashMap;
import java.util.Map;

public class HeaderMap {
    public static final String AUTHORIZATION_TOKEN_HEADER = "Authorization"; //für Auth
    public static final String CONTENT_TYPE_HEADER = "Content-Type"; //für Contenttypedings

    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String HEADER_NAME_VALUE_SEPARATOR = ":";
    private Map<String, String> headers = new HashMap<>();

    public void ingest(String headerLine) {
        final String[] split = headerLine.split(HEADER_NAME_VALUE_SEPARATOR, 2);
        headers.put(split[0], split[1].trim());
    }

    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    public int getContentLength() {
        final String header = headers.get(CONTENT_LENGTH_HEADER);
        if (header == null) {
            return 0;
        }
        return Integer.parseInt(header);
    }

    public void print() {
        System.out.println(headers);
    }

    public void setContentLength(String content) {
        headers.put(CONTENT_LENGTH_HEADER, content);
    }

    public String getAuthorizationTokenHeader() {
        final String header = headers.get(AUTHORIZATION_TOKEN_HEADER);
        return header;
    }

    public void setAuthorizationTokenHeader(String token) {
        headers.put(AUTHORIZATION_TOKEN_HEADER, token);
    }

    public String getContentTypeHeader(String content) {
        final String header = headers.get(CONTENT_TYPE_HEADER);
        return header;
    }

    public void setContentTypeHeader(String content) {
        headers.put(CONTENT_TYPE_HEADER, content);
    }

}
