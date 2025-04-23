package br.ufrj.cos.api;

import lombok.Data;

@Data
public class Endpoint {

    String path;
    String endpoint;
    String methods;
    String description;

}
