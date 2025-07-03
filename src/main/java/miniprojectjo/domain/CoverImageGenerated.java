package miniprojectjo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
public class CoverImageGenerated extends AbstractEvent {

    private Long id;
    private Long manuscriptId;
    private String coverImageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Date createdAt;

    @JsonCreator
    public CoverImageGenerated(
        @JsonProperty("id") Long id,
        @JsonProperty("manuscriptId") Long manuscriptId,
        @JsonProperty("coverImageUrl") String coverImageUrl,
        @JsonProperty("createdAt") Long createdAtMillis  // ← Long 타입
    ) {
        this.id = id;
        this.manuscriptId = manuscriptId;
        this.coverImageUrl = coverImageUrl;
        this.createdAt = new Date(createdAtMillis);      // ← 변환 처리
        this.setEventType("CoverImageGenerated");
    }


    public CoverImageGenerated(AiBookGeneration aggregate) {
        super(aggregate);
        this.id = aggregate.getId();
        this.manuscriptId = aggregate.getManuscriptId();
        this.coverImageUrl = aggregate.getCoverImageUrl();
        this.createdAt = new Date();
        this.setEventType("CoverImageGenerated");
    }

    @Override
    public boolean validate() {
        return this.manuscriptId != null && this.coverImageUrl != null;
    }

    public void logAsJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(this);
            System.out.println("📤 이벤트 직렬화: " + json);
        } catch (Exception e) {
            System.out.println("❌ JSON 직렬화 실패: " + e.getMessage());
        }
    }
}
