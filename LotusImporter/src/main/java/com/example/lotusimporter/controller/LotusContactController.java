package com.example.lotusimporter.controller;

import com.example.lotusimporter.entity.LotusContactDetails;
import com.example.lotusimporter.service.LotusContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
/**
 * Controller for handling file uploads and email extraction.
 */
@RestController
@RequestMapping("/api")
public class LotusContactController {

    private final LotusContactService emailService;

    public LotusContactController(LotusContactService emailService) {
        this.emailService = emailService;
    }
    /**
     * Handles the upload of a CSV file containing email information.
     *
     * This method processes the uploaded CSV file to extract email addresses
     * and associated contact details. It returns a map where the keys are
     * mailboxes and the values are lists of LotusContactDetails objects
     * containing email and name information.
     *
     * @param file The CSV file to be uploaded. Must not be null.
     * @return A ResponseEntity containing a map of mailbox email details,
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, List<LotusContactDetails>>> uploadCsv(@RequestParam("file") MultipartFile file) {
        Map<String, List<LotusContactDetails>> result = emailService.getEmailsFromCsv(file);
        return ResponseEntity.ok(result);
    }
}
