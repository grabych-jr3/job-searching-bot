package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.exception.ResumeProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ResumeService {

    public String extractTextFromPdf(byte[] cv){
        if (cv == null || cv.length == 0){
            log.warn("PDF extraction failed: provided CV byte array is null or empty");
            throw new ResumeProcessingException("CV file is empty");
        }

        try(PDDocument document = Loader.loadPDF(cv)){
            if (document.isEncrypted()){
                log.warn("PDF extraction rejected: PDF document is password-protected/encrypted");
                throw new ResumeProcessingException("Password-protected PDFs cannot be analyzed.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.isBlank()){
                log.warn("PDF extraction failed: document contains no extractable text (likely image-only/scanned)");
                throw new ResumeProcessingException("Could not extract any text from the PDF. Scanned images without OCR are not supported.");
            }

            log.info("Successfully extracted {} characters of text from PDF resume (pages: {})", text.length(), document.getNumberOfPages());
            return text;
        } catch (IOException e) {
            log.error("PDF extraction failed due to I/O error or corrupted document: {}", e.getMessage(), e);
            throw new ResumeProcessingException("Corrupted or invalid PDF document", e);
        }
    }
}
