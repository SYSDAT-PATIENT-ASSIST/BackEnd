package dk.patientassist.service.dto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;

/**
 * ExamTreatCategoryDTO
 */
@EqualsAndHashCode
public class ExamTreatCategoryDTO {

    public Integer id;
    public String name;
    @JsonProperty("url_safe_name")
    public String urlSafeName;
    public String description;
    @JsonProperty("exam_treat_types")
    public ExamTreatTypeDTO[] examTreatTypes;

    public ExamTreatCategoryDTO() {
    }

    public ExamTreatCategoryDTO(String name) {
        this.name = name;
        this.urlSafeName = URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    public void setUrlSafeName() {
        this.urlSafeName = URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}
