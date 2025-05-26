package dk.patientassist.service.dto;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

/**
 * ExamTreatTypeDTO
 */
@EqualsAndHashCode
public class ExamTreatTypeDTO implements Comparable<ExamTreatTypeDTO> {

    public Integer id;
    public String name;
    @JsonProperty("url_safe_name")
    public String urlSafeName;
    public String description;
    @JsonProperty("exams_and_treats")
    public ExamTreatDTO[] examTreats;

    public ExamTreatTypeDTO() {
    }

    public ExamTreatTypeDTO(String name) {
        this.name = name;
        this.urlSafeName = URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    @Override
    public int compareTo(ExamTreatTypeDTO other) {
        return name.compareTo(other.name);
    }

    public void setUrlSafeName() {
        this.urlSafeName = URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}
