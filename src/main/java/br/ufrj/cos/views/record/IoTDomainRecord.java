package br.ufrj.cos.views.record;

public record IoTDomainRecord(String name) {
    public String toString() {
        return name;
    }
}
