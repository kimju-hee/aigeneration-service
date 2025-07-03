package miniprojectjo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

@Data
@Getter
@Setter
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
public class Registered extends AbstractEvent {

    private Long id;
    private Long manuscriptId;
    private String summary;
    private String coverImageUrl;
    private Integer subscriptionFee;
    private String status;

    // Kafka 메시지에서 넘어오는 createdAt 값을 밀리초(Long)로 받음
    private Long createdAt;

    public Registered() {
        super();
        this.setEventType("Registered");
    }

    public Registered(AiBookGeneration aggregate) {
        super(aggregate);
        this.id = aggregate.getId();
        this.manuscriptId = aggregate.getManuscriptId();
        this.summary = aggregate.getSummary();
        this.coverImageUrl = aggregate.getCoverImageUrl();
        this.subscriptionFee = aggregate.getSubscriptionFee();
        this.status = aggregate.getStatus();
        this.createdAt = System.currentTimeMillis();
        this.setEventType("Registered");
    }

    @JsonCreator
    public Registered(
        @JsonProperty("id") Long id,
        @JsonProperty("manuscriptId") Long manuscriptId,
        @JsonProperty("summary") String summary,
        @JsonProperty("coverImageUrl") String coverImageUrl,
        @JsonProperty("subscriptionFee") Integer subscriptionFee,
        @JsonProperty("status") String status,
        @JsonProperty("createdAt") Long createdAt
    ) {
        this.id = id;
        this.manuscriptId = manuscriptId;
        this.summary = summary;
        this.coverImageUrl = coverImageUrl;
        this.subscriptionFee = subscriptionFee;
        this.status = status;
        this.createdAt = createdAt;
        this.setEventType("Registered");
    }

    @JsonIgnore
    public Date getCreatedAtAsDate() {
        return createdAt != null ? new Date(createdAt) : null;
    }
}
