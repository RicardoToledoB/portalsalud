package cl.dssm.soporteimagenes.dto;

import java.util.Map;

public record DashboardSummaryDto(
        long total,
        long pendientes,
        long enRevision,
        long contactados,
        long resueltos,
        long noCorresponde,
        Map<String, Long> porTipoDificultad,
        Map<String, Long> porPortal,
        long ultimos7Dias,
        long ultimos30Dias
) {}
