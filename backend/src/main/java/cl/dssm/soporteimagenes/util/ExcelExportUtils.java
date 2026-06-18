package cl.dssm.soporteimagenes.util;

import cl.dssm.soporteimagenes.dto.PortalImageRequestResponseDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class ExcelExportUtils {
    private ExcelExportUtils() {}

    public static byte[] requestsToXlsx(List<PortalImageRequestResponseDto> requests) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Solicitudes");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {
                    "Folio", "Portal", "Temática", "Fecha ingreso", "Estado", "Nombre", "RUT", "Correo", "Celular",
                    "Teléfono fijo", "Tipo dificultad", "Detalle otro", "Observación usuario", "Observación interna",
                    "Respuesta al solicitante", "Correo recepción enviado", "Correo respuesta enviado", "Error último correo",
                    "Funcionario asignado", "Fecha resolución", "Cantidad adjuntos"
            };
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (PortalImageRequestResponseDto r : requests) {
                Row row = sheet.createRow(rowNum++);
                int c = 0;
                row.createCell(c++).setCellValue(value(r.folio()));
                row.createCell(c++).setCellValue(value(r.portalName()));
                row.createCell(c++).setCellValue(value(r.topicName()));
                row.createCell(c++).setCellValue(value(r.createdAt()));
                row.createCell(c++).setCellValue(value(r.status()));
                row.createCell(c++).setCellValue(value(r.fullName()));
                row.createCell(c++).setCellValue(value(r.rut()));
                row.createCell(c++).setCellValue(value(r.email()));
                row.createCell(c++).setCellValue(value(r.phone()));
                row.createCell(c++).setCellValue(value(r.fixedPhone()));
                row.createCell(c++).setCellValue(value(r.difficultyType()));
                row.createCell(c++).setCellValue(value(r.otherDetail()));
                row.createCell(c++).setCellValue(value(r.userObservation()));
                row.createCell(c++).setCellValue(value(r.internalObservation()));
                row.createCell(c++).setCellValue(value(r.publicResponse()));
                row.createCell(c++).setCellValue(value(r.acknowledgementSentAt()));
                row.createCell(c++).setCellValue(value(r.responseSentAt()));
                row.createCell(c++).setCellValue(value(r.lastNotificationError()));
                row.createCell(c++).setCellValue(value(r.assignedUserName()));
                row.createCell(c++).setCellValue(value(r.resolvedAt()));
                row.createCell(c++).setCellValue(r.attachmentCount());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible generar archivo Excel", e);
        }
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
