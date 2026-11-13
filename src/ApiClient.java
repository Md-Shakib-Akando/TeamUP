import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Authenticated JSON client for the TeamUp Node.js API.
 * Uses the JWT issued by AuthStore at login. (HttpURLConnection has no PATCH,
 * so the API only uses GET / POST / PUT.)
 */
public final class ApiClient {

    private ApiClient() {}

    public static final class Response {
        public final int status;
        public final String body;
        public final Map<String, Object> json;

        Response(int status, String body, Map<String, Object> json) {
            this.status = status;
            this.body = body;
            this.json = json;
        }

        public boolean ok() {
            return status >= 200 && status < 300 && Json.bool(json, "success", false);
        }

        public String message(String fallback) {
            return Json.str(json, "message", fallback);
        }
    }

    /** True when a user is logged in against the backend (not the offline CSV fallback). */
    public static boolean hasSession() {
        return AuthStore.getCurrentToken() != null && !AuthStore.isOfflineMode();
    }

    public static String seg(String raw) {
        try {
            return URLEncoder.encode(raw == null ? "" : raw, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return raw;
        }
    }

    public static Response get(String path) throws IOException {
        return send("GET", path, null);
    }

    public static Response post(String path, Object body) throws IOException {
        return send("POST", path, body == null ? Json.obj() : body);
    }

    public static Response put(String path, Object body) throws IOException {
        return send("PUT", path, body == null ? Json.obj() : body);
    }

    public static Response delete(String path) throws IOException {
        return send("DELETE", path, null);
    }

    @SuppressWarnings("unchecked")
    private static Response send(String method, String path, Object body) throws IOException {
        String base = AuthStore.getApiBaseUrl();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        URL url = URI.create(base + path).toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("Accept", "application/json");
        String token = AuthStore.getCurrentToken();
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);

        if (body != null) {
            byte[] bytes = Json.stringify(body).getBytes(StandardCharsets.UTF_8);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String text = "";
        if (is != null) {
            try (InputStream in = is; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) bos.write(buf, 0, r);
                text = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            }
        }

        Map<String, Object> json = null;
        try {
            Object parsed = Json.parse(text);
            if (parsed instanceof Map) json = (Map<String, Object>) parsed;
        } catch (IllegalArgumentException ignored) {
            // non-JSON body (e.g. proxy error page) — leave json null
        }
        return new Response(status, text, json);
    }
}
