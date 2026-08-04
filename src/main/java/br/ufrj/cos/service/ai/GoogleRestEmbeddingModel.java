package br.ufrj.cos.service.ai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EmbeddingModel using Google's gemini-embedding-001 via direct REST call (v1beta).
 * No extra Maven dependencies — uses Java's built-in HttpClient.
 */
public class GoogleRestEmbeddingModel implements EmbeddingModel {

    private static final Logger logger = LoggerFactory.getLogger(GoogleRestEmbeddingModel.class);
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key=";

    private final String apiKey;
    private final HttpClient httpClient;

    public GoogleRestEmbeddingModel(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        logger.info("GoogleRestEmbeddingModel initialized (model=gemini-embedding-001).");
    }

    @Override
    public Response<Embedding> embed(String text) {
        try {
            String escaped = escapeJson(text);

            String body = """
                    {
                      "model": "models/gemini-embedding-001",
                      "content": {
                        "parts": [{"text": "%s"}]
                      }
                    }
                    """.formatted(escaped);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Embedding API error " + response.statusCode() + ": " + response.body());
            }

            float[] vector = parseValues(response.body());
            return Response.from(Embedding.from(vector));

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error calling Google Embedding API: " + e.getMessage(), e);
        }
    }

    @Override
    public Response<Embedding> embed(TextSegment segment) {
        return embed(segment.text());
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
        List<Embedding> embeddings = new ArrayList<>(segments.size());
        for (TextSegment seg : segments) {
            embeddings.add(embed(seg.text()).content());
        }
        return Response.from(embeddings);
    }

    private float[] parseValues(String json) {
        Pattern pattern = Pattern.compile("\"values\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new RuntimeException("Could not parse embedding values: " + json.substring(0, Math.min(300, json.length())));
        }
        String[] parts = matcher.group(1).split(",");
        float[] values = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Float.parseFloat(parts[i].trim());
        }
        return values;
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
