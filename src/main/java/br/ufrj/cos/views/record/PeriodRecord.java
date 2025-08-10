package br.ufrj.cos.views.record;

public record PeriodRecord(String label, Long milliseconds) {

    @Override
    public String toString() {
        return label;
    }

}
