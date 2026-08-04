package br.ufrj.cos.views.record;

public record CommunicationFlowRecord(
        String sourceLayer,
        String targetLayer,
        String protocol,
        String security,
        String pattern
) {
    public String getFormattedSummary() {
        return String.format("%s ➔ %s: %s (%s | %s)",
                sourceLayer, targetLayer,
                protocol != null ? protocol : "Not Specified",
                security != null ? security : "TLS 1.3",
                pattern != null ? pattern : "Real-time");
    }

    @Override
    public String toString() {
        return getFormattedSummary();
    }
}
