package miniprojectjo.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.*;
import miniprojectjo.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
@NoArgsConstructor
@JsonSerialize
@JsonDeserialize
public class CoverImageGenerated extends AbstractEvent {

    private Long id;
    private Long manuscriptId;
    private String coverImageUrl;
    private Date createdAt;

    // ✅ JSON 역직렬화용 생성자 추가
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
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(this);
            System.out.println("📤 직렬화 결과 JSON = " + json);
        } catch (Exception e) {
            System.out.println("❌ 직렬화 실패: " + e.getMessage());
        }
    }
}
//>>> DDD / Domain Event
