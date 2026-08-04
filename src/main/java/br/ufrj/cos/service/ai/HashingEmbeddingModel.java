package br.ufrj.cos.service.ai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * In-process embedding model using the hashing trick + TF-style weighting.
 * Produces a deterministic, normalized 512-dimensional dense vector per text.
 *
 * No external API calls, no downloads, no ONNX runtime.
 * Effective for technical RAG content (IoT architecture documents) where
 * keyword-based cosine similarity works well.
 */
public class HashingEmbeddingModel implements EmbeddingModel {

    private static final Logger logger = LoggerFactory.getLogger(HashingEmbeddingModel.class);
    private static final int DIMENSION = 512;

    public HashingEmbeddingModel() {
        logger.info("HashingEmbeddingModel initialized (dim={}, no external dependencies).", DIMENSION);
    }

    @Override
    public Response<Embedding> embed(String text) {
        float[] vector = computeVector(text);
        return Response.from(Embedding.from(vector));
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

    private float[] computeVector(String text) {
        float[] vector = new float[DIMENSION];

        if (text == null || text.isBlank()) {
            return vector;
        }

        // Tokenize: split on non-alphanumeric, lowercase
        String[] tokens = text.toLowerCase().split("[^a-z0-9áéíóúâêîôûãõàèìòù]+");

        int validTokens = 0;
        for (String token : tokens) {
            if (token.length() < 2) continue;
            validTokens++;

            // Unigram hash
            addToken(vector, token, 1.0f);

            // Domain-specific semantic expansions for IoT architecture concepts
            expandSemanticTerms(vector, token);

            // Bigrams of characters for subword granularity
            for (int i = 0; i < token.length() - 1; i++) {
                String bigram = token.substring(i, i + 2);
                addToken(vector, bigram, 0.5f);
            }
        }

        // L2 normalize
        normalize(vector);
        return vector;
    }

    private void expandSemanticTerms(float[] vector, String token) {
        switch (token) {
            case "segurança", "seguranca", "security" -> {
                addToken(vector, "criptografia", 0.6f);
                addToken(vector, "autenticacao", 0.6f);
                addToken(vector, "tls", 0.6f);
                addToken(vector, "tpm", 0.6f);
            }
            case "desempenho", "performance", "latencia", "latency" -> {
                addToken(vector, "fog", 0.6f);
                addToken(vector, "throughput", 0.6f);
                addToken(vector, "eficiencia", 0.6f);
            }
            case "disponibilidade", "availability", "tolerancia", "fault" -> {
                addToken(vector, "redundancia", 0.6f);
                addToken(vector, "replica", 0.6f);
                addToken(vector, "clustering", 0.6f);
            }
            case "escalabilidade", "scalability" -> {
                addToken(vector, "nuvem", 0.6f);
                addToken(vector, "cloud", 0.6f);
                addToken(vector, "microservicos", 0.6f);
            }
        }
    }

    private void addToken(float[] vector, String token, float weight) {
        // Two independent hash functions to reduce collision impact
        int h1 = Math.abs(token.hashCode()) % DIMENSION;
        int h2 = Math.abs(murmurMix(token)) % DIMENSION;

        vector[h1] += weight;
        vector[h2] += weight * 0.7f;
    }

    private int murmurMix(String s) {
        int h = 0x9747b28c;
        for (char c : s.toCharArray()) {
            h ^= c;
            h *= 0x5bd1e995;
            h ^= h >>> 15;
        }
        return h;
    }

    private void normalize(float[] vector) {
        float norm = 0f;
        for (float v : vector) norm += v * v;
        norm = (float) Math.sqrt(norm);
        if (norm > 1e-8f) {
            for (int i = 0; i < DIMENSION; i++) vector[i] /= norm;
        }
    }
}
