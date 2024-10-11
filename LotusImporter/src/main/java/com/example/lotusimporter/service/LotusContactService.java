package com.example.lotusimporter.service;

import com.example.lotusimporter.entity.LotusContactDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for importing emails from a CSV file.
 * This service is responsible for extracting email addresses and associated names,
 * as well as organizing the extracted data.
 */

@Service
public class LotusContactService {

    // Regular expression to extract email addresses
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    // Regular expression to extract names
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "\"([^\"]*)\"|\\b(?!(http|https)://)([a-zA-Z ,.'-]+(?:\\([a-zA-Z ,.'-]+\\))?)\\b"
    );


    private static final String ERROR_READING_FILE = "Error reading the CSV file";


    /**
     * Reads a CSV file and extracts email addresses along with associated names.
     *
     * @param file The CSV file to be processed.
     * @return A map associating each mailbox with a list of EmailInfo objects.
     */
    public Map<String, List<LotusContactDetails>> getEmailsFromCsv(MultipartFile file) {
        Map<String, List<LotusContactDetails>> mailboxEmailsMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, mailboxEmailsMap);
            }
        } catch (Exception e) {
            System.err.println(ERROR_READING_FILE);
            e.printStackTrace();
        }

        return mailboxEmailsMap;
    }

    /**
     * Processes a line from the CSV file to extract names and email addresses,
     * and adds them to the mailbox email map.
     *
     * @param line             The line to be processed.
     * @param mailboxEmailsMap The map of mailbox emails to be updated.
     */
    private void processLine(String line, Map<String, List<LotusContactDetails>> mailboxEmailsMap) {
        String[] columns = line.split(",", -1);
        if (columns.length > 0) {
            // Retrieve and clean the mailbox name
            String mailbox = columns[0].replace("\"", "").trim();
            // Extract only the part after the last \
            int lastBackslashIndex = mailbox.lastIndexOf("\\");
            if (lastBackslashIndex != -1) {
                mailbox = mailbox.substring(lastBackslashIndex + 1); // Take everything after the last \
            }

            List<String> names = extractNames(line);
            List<String> emails = extractEmails(line);

            int nameIndex = 0;
            for (String email : emails) {
                String name = names.size() > nameIndex ? names.get(nameIndex) : "";
                String[] nameParts = name.split("\\s+");
                String firstName = nameParts.length > 0 ? nameParts[0] : ""; // firstName
                String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : ""; //last NAme
                mailboxEmailsMap.computeIfAbsent(mailbox, k -> new ArrayList<>()).add(new LotusContactDetails(email, firstName, lastName));
                nameIndex++;
            }
        }
    }

    /**
     * Extracts names from a line using a regular expression.
     *
     * @param line The line containing the names.
     * @return A list of extracted names.
     */
    private List<String> extractNames(String line) {
        List<String> names = new ArrayList<>();
        Matcher nameMatcher = NAME_PATTERN.matcher(line);

        while (nameMatcher.find()) {
            String name = nameMatcher.group(1) != null ? nameMatcher.group(1) : nameMatcher.group(3);
            if (name != null && !name.contains("@") && !name.matches(".*\\\\.*")) {
                names.add(name.trim());
            }
        }

        return names;
    }

    /**
     * Extracts email addresses from a line using a regular expression.
     *
     * @param line The line containing the email addresses.
     * @return A list of extracted email addresses.
     */
    private List<String> extractEmails(String line) {
        List<String> emails = new ArrayList<>();
        Matcher emailMatcher = EMAIL_PATTERN.matcher(line);

        while (emailMatcher.find()) {
            emails.add(emailMatcher.group());
        }

        return emails;
    }
}
