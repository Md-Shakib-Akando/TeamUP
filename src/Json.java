import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser / writer (zero dependencies, Java 8).
 * Objects parse to LinkedHashMap, arrays to ArrayList, numbers to Double.
 */
public final class Json {

    private final String s;
    private int i;

    private Json(String s) {
        this.s = s;
    }

    public static Object parse(String text) {
        if (text == null) return null;
        Json p = new Json(text);
        p.ws();
        if (p.i >= p.s.length()) return null;
        Object v = p.value();
        p.ws();
        if (p.i != p.s.length()) throw new IllegalArgumentException("Trailing characters at " + p.i);
        return v;
    }

    // ------------------------------------------------------------------ parsing

    private void ws() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private Object value() {
        ws();
        if (i >= s.length()) throw new IllegalArgumentException("Unexpected end of JSON");
        char c = s.charAt(i);
        switch (c) {
            case '{': return object();
            case '[': return array();
            case '"': return string();
            case 't': expect("true"); return Boolean.TRUE;
            case 'f': expect("false"); return Boolean.FALSE;
            case 'n': expect("null"); return null;
            default: return number();
        }
    }

    private void expect(String word) {
        if (!s.startsWith(word, i)) throw new IllegalArgumentException("Expected " + word + " at " + i);
        i += word.length();
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        i++; // {
        ws();
        if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
        while (true) {
            ws();
            if (i >= s.length() || s.charAt(i) != '"') throw new IllegalArgumentException("Expected key at " + i);
            String key = string();
            ws();
            if (i >= s.length() || s.charAt(i) != ':') throw new IllegalArgumentException("Expected ':' at " + i);
            i++;
            m.put(key, value());
            ws();
            if (i >= s.length()) throw new IllegalArgumentException("Unterminated object");
            char c = s.charAt(i++);
            if (c == '}') return m;
            if (c != ',') throw new IllegalArgumentException("Expected ',' or '}' at " + (i - 1));
        }
    }

    private List<Object> array() {
        List<Object> list = new ArrayList<Object>();
        i++; // [
        ws();
        if (i < s.length() && s.charAt(i) == ']') { i++; return list; }
        while (true) {
            list.add(value());
            ws();
            if (i >= s.length()) throw new IllegalArgumentException("Unterminated array");
            char c = s.charAt(i++);
            if (c == ']') return list;
            if (c != ',') throw new IllegalArgumentException("Expected ',' or ']' at " + (i - 1));
        }
    }

    private String string() {
        StringBuilder sb = new StringBuilder();
        i++; // opening quote
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            if (i >= s.length()) break;
            char e = s.charAt(i++);
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (i + 4 > s.length()) throw new IllegalArgumentException("Bad unicode escape");
                    sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                    break;
                default: sb.append(e);
            }
        }
        throw new IllegalArgumentException("Unterminated string");
    }

    private Double number() {
        int start = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (start == i) throw new IllegalArgumentException("Unexpected character '" + s.charAt(i) + "' at " + i);
        return Double.valueOf(s.substring(start, i));
    }

    // ------------------------------------------------------------------ writing

    public static String stringify(Object v) {
        StringBuilder sb = new StringBuilder();
        write(sb, v);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            sb.append('"').append(AuthStore.escapeJson((String) v)).append('"');
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v.toString());
        } else if (v instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) v).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                write(sb, e.getKey());
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object o : (Iterable<Object>) v) {
                if (!first) sb.append(',');
                first = false;
                write(sb, o);
            }
            sb.append(']');
        } else if (v instanceof Object[]) {
            sb.append('[');
            Object[] arr = (Object[]) v;
            for (int k = 0; k < arr.length; k++) {
                if (k > 0) sb.append(',');
                write(sb, arr[k]);
            }
            sb.append(']');
        } else {
            write(sb, v.toString());
        }
    }

    /** Small builder: Json.obj("title", t, "skills", list) */
    public static Map<String, Object> obj(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        for (int k = 0; k + 1 < kv.length; k += 2) m.put(String.valueOf(kv[k]), kv[k + 1]);
        return m;
    }

    // ------------------------------------------------------------------ typed access

    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(Object parent, String key) {
        Object v = (parent instanceof Map) ? ((Map<String, Object>) parent).get(key) : null;
        return (v instanceof Map) ? (Map<String, Object>) v : null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> list(Object parent, String key) {
        Object v = (parent instanceof Map) ? ((Map<String, Object>) parent).get(key) : null;
        return (v instanceof List) ? (List<Object>) v : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> maps(Object parent, String key) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Object o : list(parent, key)) {
            if (o instanceof Map) out.add((Map<String, Object>) o);
        }
        return out;
    }

    public static String[] strings(Object parent, String key) {
        List<Object> l = list(parent, key);
        List<String> out = new ArrayList<String>();
        for (Object o : l) if (o != null) out.add(String.valueOf(o));
        return out.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    public static String str(Object parent, String key, String def) {
        Object v = (parent instanceof Map) ? ((Map<String, Object>) parent).get(key) : null;
        if (v == null) return def;
        if (v instanceof Double && ((Double) v) == Math.rint((Double) v)) return String.valueOf(((Double) v).longValue());
        return String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    public static double num(Object parent, String key, double def) {
        Object v = (parent instanceof Map) ? ((Map<String, Object>) parent).get(key) : null;
        return (v instanceof Number) ? ((Number) v).doubleValue() : def;
    }

    public static int integer(Object parent, String key, int def) {
        return (int) Math.round(num(parent, key, def));
    }

    @SuppressWarnings("unchecked")
    public static boolean bool(Object parent, String key, boolean def) {
        Object v = (parent instanceof Map) ? ((Map<String, Object>) parent).get(key) : null;
        return (v instanceof Boolean) ? (Boolean) v : def;
    }
}
