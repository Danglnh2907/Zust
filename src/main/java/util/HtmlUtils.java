package util;

public class HtmlUtils {

    /**
     * Strip HTML tags from content
     */
    public static String stripHtmlTags(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // Remove HTML tags but keep content
        String stripped = html.replaceAll("<[^>]*>", " ");

        // Replace multiple whitespaces with single space
        stripped = stripped.replaceAll("\\s+", " ");

        // Decode common HTML entities
        stripped = stripped.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'");

        return stripped.trim();
    }

    /**
     * Extract preview text from HTML content
     */
    public static String getPreviewText(String htmlContent, int maxLength) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        String plainText = stripHtmlTags(htmlContent);

        if (plainText.length() <= maxLength) {
            return plainText;
        }

        // Find last space before maxLength to avoid cutting words
        int lastSpace = plainText.lastIndexOf(' ', maxLength);
        if (lastSpace > maxLength - 20 && lastSpace > 0) { // If space is close enough to maxLength
            return plainText.substring(0, lastSpace) + "...";
        } else {
            return plainText.substring(0, Math.min(maxLength, plainText.length())) + "...";
        }
    }

    /**
     * Clean HTML for safe display (basic sanitization)
     */
    public static String sanitizeHtml(String html) {
        if (html == null) return "";

        // Allow basic formatting tags, remove potentially dangerous ones
        html = html.replaceAll("(?i)<script[^>]*>.*?</script>", "")
                .replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "")
                .replaceAll("(?i)javascript:", "")
                .replaceAll("(?i)on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "") // Remove event handlers
                .replaceAll("(?i)on\\w+\\s*=\\s*[^\\s>]*", ""); // Remove event handlers without quotes

        return html;
    }

    /**
     * Check if string contains HTML tags
     */
    public static boolean isHtmlContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        return content.contains("<") && content.contains(">");
    }

    /**
     * Get a short preview with "..." if content is longer than specified length
     */
    public static String getShortPreview(String content, int maxLength) {
        return getPreviewText(content, maxLength);
    }
}
