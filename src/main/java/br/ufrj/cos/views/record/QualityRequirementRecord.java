package br.ufrj.cos.views.record;

public record QualityRequirementRecord(String name) {
    public String toString() {
        return name;
    }
}
