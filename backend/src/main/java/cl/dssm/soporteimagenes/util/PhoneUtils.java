package cl.dssm.soporteimagenes.util;

public final class PhoneUtils {
    private PhoneUtils() {}

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String onlyDigits(String value) {
        if (value == null) return null;
        return value.replaceAll("\\D", "");
    }

    public static boolean isValidMobile(String value) {
        if (isBlank(value)) return true;
        String digits = onlyDigits(value);
        if (digits == null) return false;
        // Chile: acepta 9XXXXXXXX, 569XXXXXXXX o +56 9 XXXX XXXX.
        if (digits.length() == 9 && digits.startsWith("9")) return true;
        return digits.length() == 11 && digits.startsWith("569");
    }

    public static boolean isValidFixedPhone(String value) {
        if (isBlank(value)) return true;
        String digits = onlyDigits(value);
        if (digits == null) return false;
        // Flexible para teléfonos fijos regionales: acepta 7 a 12 dígitos, con o sin +56.
        return digits.length() >= 7 && digits.length() <= 12;
    }

    public static String normalizeMobile(String value) {
        if (isBlank(value)) return null;
        String digits = onlyDigits(value);
        if (digits == null || digits.isBlank()) return null;
        if (digits.length() == 9 && digits.startsWith("9")) return "+56 " + digits.charAt(0) + " " + digits.substring(1, 5) + " " + digits.substring(5);
        if (digits.length() == 11 && digits.startsWith("569")) return "+56 " + digits.charAt(2) + " " + digits.substring(3, 7) + " " + digits.substring(7);
        return value.trim();
    }

    public static String normalizeFixedPhone(String value) {
        if (isBlank(value)) return null;
        return value.trim().replaceAll("\\s+", " ");
    }
}
