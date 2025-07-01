package miniprojectjo.infra;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

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
            // JSON 직렬화
            String jsonEvent = toJson();

            // Base64 인코딩
            String encodedEvent = Base64.getEncoder().encodeToString(jsonEvent.getBytes(StandardCharsets.UTF_8));

            // Kafka로 전송
            outputChannel.send(
                MessageBuilder
                    .withPayload(encodedEvent)  // Base64 인코딩된 JSON 문자열
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)  // 중요: JSON 아님
                    .setHeader("type", getEventType())
                    .build()
            );

            System.out.println("📦 Base64 인코딩된 이벤트 발행 완료: " + encodedEvent);

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
            objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType("miniprojectjo.domain")
                    .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            );
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("❌ JSON 직렬화 실패", e);
        }
    }
}
