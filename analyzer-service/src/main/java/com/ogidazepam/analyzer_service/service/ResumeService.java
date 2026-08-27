package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.exception.ResumeProcessingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ResumeService {

    public String extractTextFromPdf(byte[] cv){

        if (cv == null || cv.length == 0){
            throw new ResumeProcessingException("CV file is empty");
        }

        try(PDDocument document = Loader.loadPDF(cv)){
            if (document.isEncrypted()){
                throw new ResumeProcessingException("Password-protected PDFs cannot be analyzed.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.isBlank()){
                throw new ResumeProcessingException("Could not extract any text from the PDF. Scanned images without OCR are not supported.");
            }

            return text;
        } catch (IOException e) {
            throw new ResumeProcessingException("Corrupted or invalid PDF document", e);
        }
    }
}
