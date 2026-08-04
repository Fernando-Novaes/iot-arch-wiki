package br.ufrj.cos.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SuggestedArchitectureStack implements Serializable {
    private String domain;
    private String pattern;
    private List<String> edgeTechs;
    private List<String> fogTechs;
    private List<String> cloudTechs;
    private List<String> qualityReqs;

    public SuggestedArchitectureStack() {}

    public SuggestedArchitectureStack(String domain, String pattern, List<String> edgeTechs, List<String> fogTechs, List<String> cloudTechs, List<String> qualityReqs) {
        this.domain = domain;
        this.pattern = pattern;
        this.edgeTechs = edgeTechs;
        this.fogTechs = fogTechs;
        this.cloudTechs = cloudTechs;
        this.qualityReqs = qualityReqs;
    }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public List<String> getEdgeTechs() { return edgeTechs; }
    public void setEdgeTechs(List<String> edgeTechs) { this.edgeTechs = edgeTechs; }

    public List<String> getFogTechs() { return fogTechs; }
    public void setFogTechs(List<String> fogTechs) { this.fogTechs = fogTechs; }

    public List<String> getCloudTechs() { return cloudTechs; }
    public void setCloudTechs(List<String> cloudTechs) { this.cloudTechs = cloudTechs; }

    public List<String> getQualityReqs() { return qualityReqs; }
    public void setQualityReqs(List<String> qualityReqs) { this.qualityReqs = qualityReqs; }
}
