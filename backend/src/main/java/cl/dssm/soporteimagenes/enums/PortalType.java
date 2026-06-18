package cl.dssm.soporteimagenes.enums;

public enum PortalType {
    PORTAL_IMAGENES("Portal de Imágenes");

    private final String displayName;

    PortalType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
