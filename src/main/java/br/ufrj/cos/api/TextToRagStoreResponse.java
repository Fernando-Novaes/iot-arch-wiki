package br.ufrj.cos.api;

import lombok.Data;

@Data
public class TextToRagStoreResponse {
    private String text;
    private String chunk_separator;
}
