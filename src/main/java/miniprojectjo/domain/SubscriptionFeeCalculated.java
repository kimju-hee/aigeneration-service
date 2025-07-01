package miniprojectjo.domain;

import lombok.*;
import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

//<<< DDD / Domain Event
@Data
@ToString
public class SubscriptionFeeCalculated extends AbstractEvent {

    private Long id; // ← 추가: 이벤트의 유일 식별자
    private Long manuscriptId;
    private Integer subscriptionFee;
    private String criteria;
    private Date calculatedAt;

    public SubscriptionFeeCalculated(AiBookGeneration aggregate) {
        super(aggregate);
        this.id = aggregate.getId(); // aggregate에서 ID 받아올 경우
        this.manuscriptId = aggregate.getManuscriptId();
    }

    public SubscriptionFeeCalculated() {
        super();
    }
}
//>>> DDD / Domain Event
