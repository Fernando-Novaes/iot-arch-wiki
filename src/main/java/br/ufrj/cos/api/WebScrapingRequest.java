package br.ufrj.cos.api;

import lombok.Data;

import java.util.List;

@Data
public class WebScrapingRequest {
    private List<String> urls;
}
