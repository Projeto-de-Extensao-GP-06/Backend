package br.com.sgpj.security;

import br.com.sgpj.exception.OneDriveErroException;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing filenames to prevent path injection attacks.
 * This class validates and sanitizes filenames to ensure they don't contain
 * path traversal sequences or other dangerous characters.
 */
public class FilenameSanitizer {

    private static final int MAX_FILENAME_LENGTH = 255;
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "\\.\\.[\\\\/]|" +          // ../ or ..\
        "[\\\\/]\\.\\.|" +          // /.. or \..
        "^\\.\\.$|" +               // Just ..
        "^\\.|" +                   // Starts with .
        "[\\\\/]|" +                // Path separators
        "\\x00|" +                  // Null bytes
        "[\\x00-\\x1F\\x7F]"        // Control characters
    );

    private static final Pattern DANGEROUS_CHARS_PATTERN = Pattern.compile(
        "[<>:\"|?*\\x00-\\x1F\\x7F]"  // Windows reserved characters and control characters
    );

    /**
     * Sanitizes a filename by removing dangerous characters and path traversal sequences.
     * 
     * @param filename The original filename to sanitize
     * @return A sanitized filename safe for use in file paths
     * @throws OneDriveErroException if the filename is null, empty, or contains path traversal attempts
     */
    public static String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new OneDriveErroException("O nome do arquivo não pode ser vazio.");
        }

        // Check for path traversal attempts first
        if (PATH_TRAVERSAL_PATTERN.matcher(filename).find()) {
            throw new OneDriveErroException("O nome do arquivo contém caracteres inválidos ou tentativa de path traversal.");
        }

        // Remove dangerous characters
        String sanitized = DANGEROUS_CHARS_PATTERN.matcher(filename).replaceAll("_");

        // Trim whitespace
        sanitized = sanitized.trim();

        // Remove leading/trailing dots and spaces (Windows compatibility)
        sanitized = sanitized.replaceAll("^[.\\s]+|[.\\s]+$", "");

        // Ensure the filename is not empty after sanitization
        if (sanitized.isEmpty()) {
            throw new OneDriveErroException("O nome do arquivo ficou vazio após a sanitização.");
        }

        // Enforce maximum length
        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
            // Remove trailing characters that might be incomplete
            sanitized = sanitized.replaceAll("[_\\s]+$", "");
        }

        // Ensure still not empty after length truncation
        if (sanitized.isEmpty()) {
            throw new OneDriveErroException("O nome do arquivo ficou vazio após a truncagem.");
        }

        return sanitized;
    }

    /**
     * Validates a filename without modifying it.
     * 
     * @param filename The filename to validate
     * @return true if the filename is safe, false otherwise
     */
    public static boolean isValid(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }

        if (PATH_TRAVERSAL_PATTERN.matcher(filename).find()) {
            return false;
        }

        if (filename.length() > MAX_FILENAME_LENGTH) {
            return false;
        }

        return true;
    }

    /**
     * Sanitizes a filename without throwing exceptions. Returns a safe default if sanitization fails.
     * This is intended for internal use where a valid filename must always be returned.
     * 
     * @param filename The original filename to sanitize
     * @return A sanitized filename, or a safe default if sanitization fails
     */
    public static String sanitizeSafe(String filename) {
        if (filename == null || filename.isBlank()) {
            return "arquivo_sem_nome";
        }

        try {
            return sanitize(filename);
        } catch (OneDriveErroException e) {
            // If sanitization fails, apply basic sanitization and return a safe default
            String sanitized = filename.replaceAll("[\\\\/<>:\"|?*\\x00-\\x1F\\x7F]", "_")
                    .replaceAll("^[.\\s]+|[.\\s]+$", "")
                    .trim();
            
            if (sanitized.isEmpty() || sanitized.length() > MAX_FILENAME_LENGTH || 
                PATH_TRAVERSAL_PATTERN.matcher(sanitized).find()) {
                return "arquivo_sanitizado";
            }
            
            return sanitized;
        }
    }
}