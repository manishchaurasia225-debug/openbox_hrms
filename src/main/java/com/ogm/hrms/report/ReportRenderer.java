package com.ogm.hrms.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ogm.hrms.enums.ReportFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Renders {@link ReportData} into CSV, Excel (POI), or PDF (OpenPDF). */
@Component
public class ReportRenderer {

    public ReportFile render(ReportData data, ReportFormat format) {
        return switch (format) {
            case CSV -> new ReportFile(toCsv(data), filename(data, "csv"), "text/csv");
            case EXCEL -> new ReportFile(toExcel(data), filename(data, "xlsx"),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case PDF -> new ReportFile(toPdf(data), filename(data, "pdf"), MediaType.APPLICATION_PDF_VALUE);
        };
    }

    private byte[] toCsv(ReportData data) {
        StringBuilder sb = new StringBuilder();
        sb.append(csvRow(data.headers())).append("\r\n");
        for (List<String> row : data.rows()) {
            sb.append(csvRow(row)).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csvRow(List<String> cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(csvField(cells.get(i)));
        }
        return sb.toString();
    }

    private String csvField(String value) {
        String v = value != null ? value : "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }

    private byte[] toExcel(ReportData data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName(data.title()));
            Row header = sheet.createRow(0);
            for (int c = 0; c < data.headers().size(); c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(data.headers().get(c));
            }
            int r = 1;
            for (List<String> row : data.rows()) {
                Row sheetRow = sheet.createRow(r++);
                for (int c = 0; c < row.size(); c++) {
                    sheetRow.createCell(c).setCellValue(row.get(c) != null ? row.get(c) : "");
                }
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render Excel report", e);
        }
    }

    private byte[] toPdf(ReportData data) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            Paragraph title = new Paragraph(data.title(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(Math.max(1, data.headers().size()));
            table.setWidthPercentage(100);
            for (String header : data.headers()) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
                table.addCell(cell);
            }
            for (List<String> row : data.rows()) {
                for (String value : row) {
                    table.addCell(new Paragraph(value != null ? value : "", FontFactory.getFont(FontFactory.HELVETICA, 9)));
                }
            }
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to render PDF report", e);
        }
        return out.toByteArray();
    }

    private String filename(ReportData data, String extension) {
        return data.title().replaceAll("[^a-zA-Z0-9]+", "_") + "." + extension;
    }

    private String sheetName(String title) {
        String name = title.replaceAll("[\\\\/*?\\[\\]:]", " ");
        return name.length() > 31 ? name.substring(0, 31) : name;
    }
}
