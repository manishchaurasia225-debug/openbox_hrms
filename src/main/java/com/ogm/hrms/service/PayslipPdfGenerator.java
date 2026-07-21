package com.ogm.hrms.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ogm.hrms.entity.Payslip;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

/** Renders a simple, self-contained PDF payslip (OpenPDF). Earnings only — no deductions. */
@Component
public class PayslipPdfGenerator {

    public byte[] generate(Payslip payslip, String employeeName, String employeeCode, String companyName) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph(companyName != null ? companyName : "Company", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Payslip for " + payslip.getPeriodMonth() + "/" + payslip.getPeriodYear());
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Employee: " + employeeName + " (" + employeeCode + ")"));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(70);
            addRow(table, "Component", "Amount", true);
            addRow(table, "Basic", format(payslip.getBasic()), false);
            addRow(table, "HRA", format(payslip.getHra()), false);
            addRow(table, "Special Allowance", format(payslip.getSpecialAllowance()), false);
            addRow(table, "Bonus", format(payslip.getBonus()), false);
            addRow(table, "Incentives", format(payslip.getIncentives()), false);
            addRow(table, "Other Allowances", format(payslip.getOtherAllowances()), false);
            addRow(table, "Gross Pay", format(payslip.getGrossPay()), true);
            addRow(table, "Net Pay", format(payslip.getNetPay()), true);
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("This is a system-generated payslip."));
            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to render payslip PDF", e);
        }
        return out.toByteArray();
    }

    private void addRow(PdfPTable table, String label, String value, boolean bold) {
        Font font = bold ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)
                : FontFactory.getFont(FontFactory.HELVETICA, 11);
        table.addCell(new PdfPCell(new Paragraph(label, font)));
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, font));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String format(BigDecimal amount) {
        return amount != null ? amount.toPlainString() : "0.00";
    }
}
