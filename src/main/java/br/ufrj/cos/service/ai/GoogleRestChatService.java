package br.ufrj.cos.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calls Google's gemini-3.5-flash via direct REST API (generateContent & streamGenerateContent).
 * Uses Java's built-in HttpClient with SSE streaming support and automatic retry backoff.
 */
public class GoogleRestChatService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleRestChatService.class);
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=";
    private static final String STREAM_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:streamGenerateContent?alt=sse&key=";

    private final String apiKey;
    private final HttpClient httpClient;

    private static final int MAX_RETRIES = 3;
    private static final List<Long> BACKOFF_MS = List.of(2_000L, 4_000L, 8_000L);

    public GoogleRestChatService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        logger.info("GoogleRestChatService initialized (model=gemini-3.5-flash with streaming).");
    }

    public String generate(String prompt) {
        String escaped = escapeJson(prompt);
        String body = """
                {
                  "contents": [
                    {
                      "parts": [{"text": "%s"}]
                    }
                  ],
                  "generationConfig": {
                    "temperature": 0.2,
                    "maxOutputTokens": 8192
                  }
                }
                """.formatted(escaped);

        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL + apiKey))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(60))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                if (status == 200) {
                    return parseText(response.body());
                }

                if ((status == 503 || status == 429 || status == 500) && attempt < MAX_RETRIES) {
                    long waitMs = BACKOFF_MS.get(attempt - 1);
                    logger.warn("Chat API returned {} on attempt {}/{}. Retrying in {}ms...",
                            status, attempt, MAX_RETRIES, waitMs);
                    Thread.sleep(waitMs);
                    continue;
                }

                lastException = new RuntimeException("Chat API error " + status + ": " + response.body());

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request interrupted while calling Chat API", ie);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                lastException = new RuntimeException("Error calling Google Chat API: " + e.getMessage(), e);
                if (attempt < MAX_RETRIES) {
                    long waitMs = BACKOFF_MS.get(attempt - 1);
                    logger.warn("Network error on attempt {}/{}: {}. Retrying in {}ms...",
                            attempt, waitMs, e.getMessage());
                    try { Thread.sleep(waitMs); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw lastException != null ? lastException
                : new RuntimeException("Chat API failed after " + MAX_RETRIES + " attempts");
    }

    public Flux<String> generateStream(String prompt) {
        String escaped = escapeJson(prompt);
        String body = """
                {
                  "contents": [
                    {
                      "parts": [{"text": "%s"}]
                    }
                  ],
                  "generationConfig": {
                    "temperature": 0.2,
                    "maxOutputTokens": 8192
                  }
                }
                """.formatted(escaped);

        return Flux.create(sink -> executeStreamRequestWithRetry(body, 1, sink));
    }

    private void executeStreamRequestWithRetry(String body, int attempt, FluxSink<String> sink) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(STREAM_API_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(90))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        int status = response.statusCode();
                        if (status == 200) {
                            response.body().forEach(line -> {
                                if (line.startsWith("data: ")) {
                                    String json = line.substring(6).trim();
                                    if (!json.isEmpty() && !json.equals("[DONE]")) {
                                        String chunk = parseChunkText(json);
                                        if (chunk != null && !chunk.isEmpty()) {
                                            sink.next(chunk);
                                        }
                                    }
                                }
                            });
                            sink.complete();
                        } else if ((status == 429 || status == 503 || status == 500) && attempt < MAX_RETRIES) {
                            long waitMs = BACKOFF_MS.get(attempt - 1);
                            logger.warn("Streaming API returned {} on attempt {}/{}. Retrying in {}ms...", status, attempt, MAX_RETRIES, waitMs);
                            try { Thread.sleep(waitMs); } catch (InterruptedException ignored) {}
                            executeStreamRequestWithRetry(body, attempt + 1, sink);
                        } else {
                            sink.error(new RuntimeException("Streaming API error " + status));
                        }
                    })
                    .exceptionally(ex -> {
                        if (attempt < MAX_RETRIES) {
                            long waitMs = BACKOFF_MS.get(attempt - 1);
                            logger.warn("Streaming network error on attempt {}/{}. Retrying in {}ms...", attempt, MAX_RETRIES, waitMs);
                            try { Thread.sleep(waitMs); } catch (InterruptedException ignored) {}
                            executeStreamRequestWithRetry(body, attempt + 1, sink);
                        } else {
                            sink.error(ex);
                        }
                        return null;
                    });
        } catch (Exception e) {
            sink.error(e);
        }
    }

    private String parseText(String json) {
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new RuntimeException("Could not parse text from chat response: " + json.substring(0, Math.min(300, json.length())));
        }
        String raw = matcher.group(1);
        raw = raw
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");

        return decodeUnicodeEscapes(raw);
    }

    private String parseChunkText(String json) {
        try {
            Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                String raw = matcher.group(1);
                raw = raw
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
                return decodeUnicodeEscapes(raw);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String decodeUnicodeEscapes(String input) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (i + 5 < input.length()
                    && input.charAt(i) == '\\'
                    && input.charAt(i + 1) == 'u') {
                String hex = input.substring(i + 2, i + 6);
                try {
                    int codePoint = Integer.parseInt(hex, 16);
                    sb.append((char) codePoint);
                    i += 6;
                    continue;
                } catch (NumberFormatException ignored) {
                }
            }
            sb.append(input.charAt(i));
            i++;
        }
        return sb.toString();
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
