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
    private String content;
    private Date createdAt;
    private String eventType;

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

    public PublishingRequested(Object aggregate) {
        super(aggregate);
        this.eventType = "PublishingRequested";
    }
}
