package br.ufrj.cos.views.record;

public record TechnologyRecord(String description) {
    public String toString() {
        return description;
    }
}
