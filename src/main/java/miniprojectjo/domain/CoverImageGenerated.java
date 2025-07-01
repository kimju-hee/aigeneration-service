package miniprojectjo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

@Getter
@Setter
@JsonSerialize
@JsonDeserialize
public class CoverImageGenerated extends AbstractEvent {

    private Long id;
    private Long manuscriptId;
    private String coverImageUrl;
    private Date createdAt;

    @JsonCreator
    public CoverImageGenerated(
        @JsonProperty("id") Long id,
        @JsonProperty("manuscriptId") Long manuscriptId,
        @JsonProperty("coverImageUrl") String coverImageUrl,
        @JsonProperty("createdAt") Date createdAt
    ) {
        this.id = id;
        this.manuscriptId = manuscriptId;
        this.coverImageUrl = coverImageUrl;
        this.createdAt = createdAt;
        this.setEventType("CoverImageGenerated");
    }

    // ✅ 추가된 생성자
    public CoverImageGenerated(AiBookGeneration aggregate) {
        super(aggregate);
        this.id = aggregate.getId();
        this.manuscriptId = aggregate.getManuscriptId();
        this.coverImageUrl = aggregate.getCoverImageUrl();
        this.createdAt = new Date();
        this.setEventType("CoverImageGenerated");
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
