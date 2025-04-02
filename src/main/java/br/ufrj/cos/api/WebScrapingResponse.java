package br.ufrj.cos.api;

import lombok.Data;
import java.util.List;

@Data
public class WebScrapingResponse {
    private List<String> urls;
}