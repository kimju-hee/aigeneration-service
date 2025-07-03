package miniprojectjo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

@Data
@NoArgsConstructor
public class PublishingRequested extends AbstractEvent {

    private Long id;
    private String title;
    private Long authorId;
    private String status;
    private String content; // 원고 내용
    private Date createdAt;

    @JsonCreator
    public PublishingRequested(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("authorId") Long authorId,
        @JsonProperty("status") String status,
        @JsonProperty("content") String content,
        @JsonProperty("createdAt") Date createdAt,
        @JsonProperty("eventType") String eventType
    ) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.status = status;
        this.content = content;
        this.createdAt = createdAt;
        this.setEventType(eventType != null ? eventType : "PublishingRequested");
    }

    public PublishingRequested(Object aggregate) {
        super(aggregate);
        this.setEventType("PublishingRequested");

        if (aggregate instanceof AiBookGeneration) {
            AiBookGeneration agg = (AiBookGeneration) aggregate;
            this.id = agg.getId();
            this.title = "책 제목"; // 필요시 다른 로직으로 대체
            this.authorId = null;  // 확장 필요 시 추가
            this.status = agg.getStatus();
            this.content = agg.getManuscriptContent();
            this.createdAt = agg.getCreatedAt();
        }
    }
}
