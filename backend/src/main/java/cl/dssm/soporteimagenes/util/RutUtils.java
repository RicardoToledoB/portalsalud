package cl.dssm.soporteimagenes.util;

public final class RutUtils {
    private RutUtils() {}

    public static String clean(String rut) {
        if (rut == null) return null;
        return rut.replace(".", "").replace("-", "").replace(" ", "").trim().toUpperCase();
    }

    public static String format(String rut) {
        String clean = clean(rut);
        if (clean == null || clean.length() < 2) return rut;
        String body = clean.substring(0, clean.length() - 1);
        String dv = clean.substring(clean.length() - 1);
        return body + "-" + dv;
    }

    public static boolean isValid(String rut) {
        String clean = clean(rut);
        if (clean == null || clean.length() < 2 || clean.length() > 9) return false;
        String body = clean.substring(0, clean.length() - 1);
        char dv = clean.charAt(clean.length() - 1);
        if (!body.matches("\\d+")) return false;
        int sum = 0;
        int multiplier = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }
        int rest = 11 - (sum % 11);
        char expected = rest == 11 ? '0' : rest == 10 ? 'K' : Character.forDigit(rest, 10);
        return expected == Character.toUpperCase(dv);
    }
}
