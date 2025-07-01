package miniprojectjo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

@Data
@ToString
@NoArgsConstructor
public class PublishingRequested extends AbstractEvent {

    private Long id;
    private String title;
    private Long authorId;
    private String status;
    private String content;  // 원고 내용
    private Date createdAt;
    private String eventType;

    // 기본 생성자 (JSON 생성 시 사용)
    @JsonCreator
    public PublishingRequested(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("authorId") Long authorId,
        @JsonProperty("status") String status,
        @JsonProperty("content") String content,
        @JsonProperty("createdAt") Date createdAt
    ) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.status = status;
        this.content = content;
        this.createdAt = createdAt;
        this.eventType = "PublishingRequested";
    }

    // aggregate에서 값 받아오기
    public PublishingRequested(Object aggregate) {
        super(aggregate);
        this.eventType = "PublishingRequested";

        if (aggregate instanceof AiBookGeneration) {
            AiBookGeneration agg = (AiBookGeneration) aggregate;
            this.id = agg.getId();
            this.title = "책 제목";  // 제목이 없다면 필요한 로직 추가
            this.authorId = null;  // 필요한 경우 authorId 추가
            this.status = agg.getStatus();
            this.content = agg.getManuscriptContent();  // 원고 내용
            this.createdAt = agg.getCreatedAt();
        }
    }
}
