package miniprojectjo.infra;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import miniprojectjo.AigenerationApplication;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;

import org.springframework.beans.BeanUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MimeTypeUtils;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BookSummaryGenerate.class, name = "BookSummaryGenerate"),
    @JsonSubTypes.Type(value = CoverImageGenerated.class, name = "CoverImageGenerated"),
    @JsonSubTypes.Type(value = Registered.class, name = "Registered"),
    @JsonSubTypes.Type(value = SubscriptionFeeCalculated.class, name = "SubscriptionFeeCalculated")
})
public abstract class AbstractEvent {

    private String eventType;
    private Long timestamp;

    public AbstractEvent(Object aggregate) {
        this();
        BeanUtils.copyProperties(aggregate, this);
    }

    public AbstractEvent() {
        this.eventType = this.getClass().getSimpleName();
        this.timestamp = System.currentTimeMillis();
    }

    public void publish() {
        KafkaProcessor processor = AigenerationApplication.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();

        try {
            String jsonEvent = toJson();

            System.out.println("📤 JSON 이벤트 발행: " + jsonEvent);

            outputChannel.send(
                MessageBuilder
                    .withPayload(jsonEvent)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .setHeader("type", getEventType())
                    .build()
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ 메시지 발행 실패", e);
        }
    }

    public void publishAfterCommit() {
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    AbstractEvent.this.publish();
                }
            }
        );
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean validate() {
        return getEventType() != null && getEventType().equals(getClass().getSimpleName());
    }

    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("❌ JSON 직렬화 실패", e);
        }
    }
}
