package com.ogidazepam.analyzer_service.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ResumeService {

    public String extractTextFromPdf(){
        ClassPathResource resource = new ClassPathResource("cv/Vladyslav Hrabovskyi EN.pdf");

        try(InputStream inputStream = resource.getInputStream();
            PDDocument document = Loader.loadPDF(inputStream.readAllBytes())){

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
