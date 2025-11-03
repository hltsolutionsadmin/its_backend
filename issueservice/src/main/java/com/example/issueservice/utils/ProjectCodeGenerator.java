package com.example.issueservice.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ProjectCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int RANDOM_SUFFIX_LENGTH = 4;
    private static final String DEFAULT_PREFIX = "PRJ";


    public String generateCode(String projectName) {
        String abbreviation = extractAbbreviation(projectName);
        String datePart = LocalDate.now().format(DATE_FORMAT);
        String randomPart = randomHex(RANDOM_SUFFIX_LENGTH);

        return String.format("%s-%s-%s-%s", DEFAULT_PREFIX, abbreviation, datePart, randomPart);
    }

    private String extractAbbreviation(String name) {
        if (name == null || name.isEmpty()) return "GEN";
        String[] words = name.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (sb.length() >= 4) break;
            }
        }
        return sb.toString();
    }

    /**
     * Generates a random hexadecimal string (A–F, 0–9) of given length.
     */
    private String randomHex(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(Integer.toHexString(RANDOM.nextInt(16)).toUpperCase());
        }
        return sb.toString();
    }
}
