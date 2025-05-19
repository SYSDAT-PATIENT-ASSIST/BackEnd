package dk.patientassist.service.dto;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

/**
 * ExamTreatDTO
 */
@EqualsAndHashCode
public class ExamTreatDTO {

    public Integer id;
    public String name;
    @JsonProperty("url_safe_name")
    public String urlSafeName;
    public String description;
    @JsonProperty("src_url")
    public String srcUrl;
    public String article;

    public ExamTreatDTO() {
    }

    public ExamTreatDTO(String name) {
        this.name = name;
        this.urlSafeName = URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    public ExamTreatDTO(String name, String srcUrl) {
        this.name = name;
        this.srcUrl = srcUrl;
        this.urlSafeName = URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    public void setUrlSafeName() {
        this.urlSafeName = URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}
