package miniprojectjo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

//<<< DDD / Domain Event
@Data
@ToString
@NoArgsConstructor
public class Registered extends AbstractEvent {

    private Long id;
    private Long manuscriptId;
    private String summary;
    private String coverImageUrl;
    private Integer subscriptionFee;
    private String status;
    private Date createdAt;

    // Jackson에서 사용할 생성자 추가
    @JsonCreator
    public Registered(
        @JsonProperty("id") Long id,
        @JsonProperty("manuscriptId") Long manuscriptId,
        @JsonProperty("summary") String summary,
        @JsonProperty("coverImageUrl") String coverImageUrl,
        @JsonProperty("subscriptionFee") Integer subscriptionFee,
        @JsonProperty("status") String status,
        @JsonProperty("createdAt") Date createdAt
    ) {
        this.id = id;
        this.manuscriptId = manuscriptId;
        this.summary = summary;
        this.coverImageUrl = coverImageUrl;
        this.subscriptionFee = subscriptionFee;
        this.status = status;
        this.createdAt = createdAt;
    }

    // 기존 도메인 생성자
    public Registered(AiBookGeneration aggregate) {
        super(aggregate);
        this.id = aggregate.getId();
        this.manuscriptId = aggregate.getManuscriptId();
        this.summary = aggregate.getSummary();
        this.coverImageUrl = aggregate.getCoverImageUrl();
        this.subscriptionFee = aggregate.getSubscriptionFee();
        this.status = aggregate.getStatus();
        this.createdAt = new Date();
        this.setEventType("Registered");
    }
}
//>>> DDD / Domain Event
