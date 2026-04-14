package org.example.cycle.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.cycle.model.Cycle;
import org.example.cycle.model.Symptome;

import java.io.File;
import java.io.FileOutputStream;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PdfExportService {

    public static void exportCyclesToPdf(File destFile, List<Cycle> cycles, SymptomeService symptomeService) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(destFile));

        document.open();

        // Title Font
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

        // Add Title
        Paragraph title = new Paragraph("Rapport Menstruel et Symptômes", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(30);
        document.add(title);

        for (Cycle cycle : cycles) {
            long days = ChronoUnit.DAYS.between(cycle.getDate_debut_m().toLocalDate(), cycle.getDate_fin_m().toLocalDate());

            // Cycle Header
            Paragraph cycleHeader = new Paragraph("Cycle: " + cycle.getDate_debut_m() + " -> " + cycle.getDate_fin_m() + " (" + days + " jours)", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(220, 53, 69)));
            cycleHeader.setSpacingBefore(15);
            cycleHeader.setSpacingAfter(10);
            document.add(cycleHeader);

            // Fetch symptoms for this cycle
            List<Symptome> symptoms = symptomeService.getSymptomesByCycleId(cycle.getCycle_id());

            if (symptoms.isEmpty()) {
                Paragraph noSymp = new Paragraph("Aucun symptôme enregistré pour ce cycle.", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY));
                document.add(noSymp);
            } else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5);

                // Table Header
                PdfPCell cell1 = new PdfPCell(new Phrase("Date", headerFont));
                cell1.setBackgroundColor(new BaseColor(59, 130, 246));
                cell1.setPadding(8);
                
                PdfPCell cell2 = new PdfPCell(new Phrase("Type", headerFont));
                cell2.setBackgroundColor(new BaseColor(59, 130, 246));
                cell2.setPadding(8);
                
                PdfPCell cell3 = new PdfPCell(new Phrase("Intensité", headerFont));
                cell3.setBackgroundColor(new BaseColor(59, 130, 246));
                cell3.setPadding(8);

                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);

                // Table rows
                for (Symptome s : symptoms) {
                    PdfPCell dCell = new PdfPCell(new Phrase(s.getDateObservation().toString(), cellFont));
                    dCell.setPadding(6);
                    PdfPCell tCell = new PdfPCell(new Phrase(s.getType().name(), cellFont));
                    tCell.setPadding(6);
                    PdfPCell iCell = new PdfPCell(new Phrase(s.getIntensite().name(), cellFont));
                    iCell.setPadding(6);

                    table.addCell(dCell);
                    table.addCell(tCell);
                    table.addCell(iCell);
                }

                document.add(table);
            }
        }

        document.close();
    }
}
