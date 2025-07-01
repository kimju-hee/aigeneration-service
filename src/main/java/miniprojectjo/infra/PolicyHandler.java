package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PolicyHandler {

    @Autowired
    AiBookGenerationRepository aiBookGenerationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private <T> T decodeEvent(Message<byte[]> message, Class<T> clazz) {
        try {
            String base64 = new String(message.getPayload(), StandardCharsets.UTF_8);
            String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("❌ [{}] 역직렬화 실패: {}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PublishingRequested'")
    public void onPublishingRequested(Message<byte[]> message) {
        PublishingRequested event = decodeEvent(message, PublishingRequested.class);
        if (event != null && event.validate()) {
            log.info("📗 PublishingRequested 수신: {}", event);
            AiBookGeneration.generateBookSummary(event);
            AiBookGeneration.generateCoverImage(event);
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerated(Message<byte[]> message) {
        BookSummaryGenerate event = decodeEvent(message, BookSummaryGenerate.class);
        if (event != null && event.validate()) {
            log.info("📝 BookSummaryGenerate 수신: {}", event);
            AiBookGeneration.registerProcessedBook(event);
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='CoverImageGenerated'")
    public void onCoverImageGenerated(Message<byte[]> message) {
        CoverImageGenerated event = decodeEvent(message, CoverImageGenerated.class);
        if (event != null && event.validate()) {
            log.info("🎨 CoverImageGenerated 수신: {}", event);
            AiBookGeneration.registerProcessedBook(event);
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Registered'")
    public void onRegistered(Message<byte[]> message) {
        Registered event = decodeEvent(message, Registered.class);
        if (event != null && event.validate()) {
            log.info("💰 Registered 수신: {}", event);
            AiBookGeneration.subscriptionFeePolicy(event);
        }
    }
}
