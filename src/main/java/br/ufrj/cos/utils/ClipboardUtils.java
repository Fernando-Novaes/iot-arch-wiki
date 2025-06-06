package br.ufrj.cos.utils;

import com.vaadin.flow.component.UI;

import java.util.regex.Pattern;

public class ClipboardUtils {

    // Pattern to match HTML tags
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    // Pattern to match common HTML entities (add more as needed)
    private static final Pattern HTML_ENTITY_PATTERN_AMP = Pattern.compile("&", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_ENTITY_PATTERN_LT = Pattern.compile("<", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_ENTITY_PATTERN_GT = Pattern.compile(">", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_ENTITY_PATTERN_QUOT = Pattern.compile("\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_ENTITY_PATTERN_NBSP = Pattern.compile(" ", Pattern.CASE_INSENSITIVE);
    // Add more entities like ', ©, ®, etc. if needed

    /**
     * Basic HTML sanitizer that strips all HTML tags and decodes common HTML entities.
     * WARNING: This is a very basic sanitizer and should NOT be relied upon for
     * security purposes with untrusted HTML input. Use a dedicated library for that.
     *
     * @param htmlString The HTML string to sanitize.
     * @return A string with HTML tags removed and common entities decoded.
     *         Returns an empty string if the input is null.
     */
    public static String basicHtmlToCleanString(String htmlString) {
        if (htmlString == null) {
            return "";
        }

        // 1. Remove HTML tags
        String noHtml = HTML_TAG_PATTERN.matcher(htmlString).replaceAll("");

        // 2. Decode common HTML entities (order can be important for nested entities, though less common here)
        String decoded = HTML_ENTITY_PATTERN_AMP.matcher(noHtml).replaceAll("&");
        decoded = HTML_ENTITY_PATTERN_LT.matcher(decoded).replaceAll("<");
        decoded = HTML_ENTITY_PATTERN_GT.matcher(decoded).replaceAll(">");
        decoded = HTML_ENTITY_PATTERN_QUOT.matcher(decoded).replaceAll("\"");
        decoded = HTML_ENTITY_PATTERN_NBSP.matcher(decoded).replaceAll(" "); // Replace non-breaking space with a regular space

        // 3. Trim whitespace from the beginning and end
        return decoded.trim();
    }

    public static void copyToClipboard(String text, String successMessage) {
        if (text == null || text.isEmpty()) {
            NotificationUtils.showErrorNotification("Nothing to copy.");
            return;
        }

        // Escape backticks, backslashes, and dollar signs for JavaScript template literals
        String escapedText = text.replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("${", "\\${");

        UI.getCurrent().getPage().executeJs(
                "navigator.clipboard.writeText(`" + escapedText + "`).then(function() {" +
                        "    return true;" +
                        "}).catch(function(err) {" +
                        "    console.error('Failed to copy text: ', err);" +
                        "    return false;" +
                        "});"
        );
//        .then(Boolean.class, success -> {
//            if (Boolean.TRUE.equals(success)) {
//                NotificationUtils.showSuccessNotification(successMessage);
//            } else {
//                // Fallback or more prominent error if needed, though console error is primary
//                NotificationUtils.showErrorNotification("Failed to copy. See browser console.");
//            }
//        });
    }



    public static void copyToClipboard(String text) {
        copyToClipboard(text, "Copied to clipboard!");
    }

}
