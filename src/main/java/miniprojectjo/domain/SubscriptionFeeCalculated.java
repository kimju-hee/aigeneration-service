package miniprojectjo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

//<<< DDD / Domain Event
@Data
@Getter
@Setter
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
public class SubscriptionFeeCalculated extends AbstractEvent {

    private Long id;
    private Long manuscriptId;
    private Integer subscriptionFee;
    private String criteria;
    private Date calculatedAt;

    // ✅ 기본 생성자
    public SubscriptionFeeCalculated() {
        super();
        this.setEventType("SubscriptionFeeCalculated");
    }

    // ✅ aggregate 생성자
    public SubscriptionFeeCalculated(AiBookGeneration aggregate) {
        super(aggregate);
        this.id = aggregate.getId();
        this.manuscriptId = aggregate.getManuscriptId();
        this.setEventType("SubscriptionFeeCalculated");
    }

    // ✅ Json 역직렬화를 위한 생성자
    @JsonCreator
    public SubscriptionFeeCalculated(
        @JsonProperty("id") Long id,
        @JsonProperty("manuscriptId") Long manuscriptId,
        @JsonProperty("subscriptionFee") Integer subscriptionFee,
        @JsonProperty("criteria") String criteria,
        @JsonProperty("calculatedAt") Date calculatedAt
    ) {
        this.id = id;
        this.manuscriptId = manuscriptId;
        this.subscriptionFee = subscriptionFee;
        this.criteria = criteria;
        this.calculatedAt = calculatedAt;
        this.setEventType("SubscriptionFeeCalculated");
    }
}
//>>> DDD / Domain Event
