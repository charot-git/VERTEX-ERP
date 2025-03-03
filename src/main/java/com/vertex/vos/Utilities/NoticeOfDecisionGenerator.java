package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.PhysicalInventory;
import com.vertex.vos.Objects.PhysicalInventoryDetails;
import org.apache.poi.xwpf.usermodel.*;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class NoticeOfDecisionGenerator {


    public static void generateNoticeOfDecision(
            PhysicalInventory inventory, String recipient, String auditor,
            List<PhysicalInventoryDetails> detailsList, String totalShortage,
            String finding, LocalDate auditStart, LocalDate auditEnd) throws IOException {

        XWPFDocument document = new XWPFDocument();

        // Title
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("NOTICE TO EXPLAIN");
        titleRun.setBold(true);
        titleRun.setFontSize(14);

        // Document Information
        document.createParagraph().createRun().setText("To: " + recipient);
        document.createParagraph().createRun().setText("Date: " + inventory.getDateEncoded().toLocalDateTime().toLocalDate());
        document.createParagraph().createRun().setText("From: " + auditor);
        document.createParagraph().createRun().setText("Docno: " + inventory.getPhNo());

        // Attention Section
        XWPFParagraph attention = document.createParagraph();
        XWPFRun attentionRun = attention.createRun();
        attentionRun.setText("\nAttention:");
        attentionRun.setBold(true);

        // Audit Findings
        document.createParagraph().createRun().setText("\nWe would like to inform you that the audit department has probed and confirmed that you have stocks "
                + finding.toLowerCase() + " from your inventory audited: " + auditStart + " to " + auditEnd
                + " amounting to PHP " + totalShortage + ".");

        // Note Section
        XWPFParagraph note = document.createParagraph();
        XWPFRun noteRun = note.createRun();
        noteRun.setText("Note: ");
        noteRun.setBold(true);
        note.createRun().setText("Details are attached in this memorandum.");

        // Request for Explanation
        document.createParagraph().createRun().setText("\nPlease provide a written explanation in this memorandum on the said offence "
                + "to be submitted within the day to the audit department for further delving.");

        // Written Explanation Section
        XWPFParagraph explanation = document.createParagraph();
        XWPFRun explanationRun = explanation.createRun();
        explanationRun.setText("\nWritten Explanation: ");
        explanationRun.setBold(true);

        // File Save Dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Notice of Decision");
        File selectedFile = new File("Notice_of_Decision_" + inventory.getPhNo() + ".docx");
        int fileNumber = 1;
        while (selectedFile.exists()) {
            selectedFile = new File("Notice_of_Decision_" + inventory.getPhNo() + "_" + fileNumber + ".docx");
            fileNumber++;
        }
        fileChooser.setSelectedFile(selectedFile);
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                document.write(out);
                System.out.println("Document saved as " + fileToSave.getAbsolutePath());
            }
        } else {
            System.out.println("Save operation was canceled.");
        }
        document.close();
    }


}