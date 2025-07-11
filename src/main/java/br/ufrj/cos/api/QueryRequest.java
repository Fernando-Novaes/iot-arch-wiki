package br.ufrj.cos.api;

import lombok.Data;

import java.util.UUID;

@Data
public class QueryRequest {
    private String query;
    private UUID conversation_id;
}
