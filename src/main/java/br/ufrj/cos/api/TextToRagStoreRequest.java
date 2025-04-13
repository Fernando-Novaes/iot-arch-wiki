package br.ufrj.cos.api;

import lombok.Data;

@Data
public class TextToRagStoreRequest {
    private String text;
    private String chunk_separator;
}
