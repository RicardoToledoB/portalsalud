package cl.dssm.soporteimagenes.util;

public final class CsvUtils {
    private CsvUtils() {}

    public static String escape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
