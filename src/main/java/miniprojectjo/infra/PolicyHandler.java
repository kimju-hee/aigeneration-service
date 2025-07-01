package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
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

    private <T> T decodePayload(String base64Encoded, Class<T> clazz) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("❌ JSON 디코딩 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void rawListener(org.springframework.messaging.Message<byte[]> message) {
        String json = new String(message.getPayload(), StandardCharsets.UTF_8);
        System.out.println("📥 Kafka CLI 메시지 수신:");
        System.out.println(" - Headers: " + message.getHeaders());
        System.out.println(" - Payload: " + json);
    }

    // === PublishingRequested 이벤트 수신: 요약 및 표지 생성 ===
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PublishingRequested'")
    public void onPublishingRequested(@Payload String encodedPayload) {
        PublishingRequested event = decodePayload(encodedPayload, PublishingRequested.class);
        if (event != null) {
            log.info("📗 PublishingRequested received: {}", event);
            AiBookGeneration.generateBookSummary(event);
            AiBookGeneration.generateCoverImage(event);
        }
    }

    // === BookSummaryGenerate 이벤트 수신: 처리 정보 등록 ===
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerated(@Payload String encodedPayload) {
        BookSummaryGenerate event = decodePayload(encodedPayload, BookSummaryGenerate.class);
        if (event != null && event.validate()) {
            log.info("📝 BookSummaryGenerate received: {}", event);
            AiBookGeneration.registerProcessedBook(event);
        }
    }

    // === CoverImageGenerated 이벤트 수신: 처리 정보 등록 ===
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='CoverImageGenerated'")
    public void onCoverImageGenerated(@Payload String encodedPayload) {
        CoverImageGenerated event = decodePayload(encodedPayload, CoverImageGenerated.class);
        if (event != null && event.validate()) {
            log.info("🖼️ CoverImageGenerated received: {}", event);
            AiBookGeneration.registerProcessedBook(event);
        }
    }

    // === Registered 이벤트 수신: 구독료 정책 적용 ===
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Registered'")
    public void onRegistered(@Payload String encodedPayload) {
        Registered event = decodePayload(encodedPayload, Registered.class);
        if (event != null && event.validate()) {
            log.info("💰 Registered received: {}", event);
            AiBookGeneration.subscriptionFeePolicy(event);
        }
    }
}
