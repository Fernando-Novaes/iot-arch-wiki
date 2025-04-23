package br.ufrj.cos.views.record;

public record EndpointRecord(String endpoint, String description) {
    @Override
    public String toString(){
        return String.format("%s - %s",endpoint, description);
    }
}
