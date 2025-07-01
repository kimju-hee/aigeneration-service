package miniprojectjo.infra;

import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PolicyHandler {

    @Autowired
    AiBookGenerationRepository aiBookGenerationRepository;

    // 테스트 리스너
    @StreamListener(KafkaProcessor.INPUT)
    public void rawListener(@Payload String rawJson) {
        log.info("🔍 Raw Kafka message 수신됨: {}", rawJson);
    }


    // // === 기본 fallback 리스너 ===
    // @StreamListener(KafkaProcessor.INPUT)
    // public void fallbackListener(@Payload String eventString) {
    //     log.debug("Unknown event received: {}", eventString);
    // }

    // === PublishingRequested 이벤트 수신 시, 요약 및 표지 생성 ===
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PublishingRequested'")
    public void onPublishingRequested(@Payload PublishingRequested event) {
        if (!event.validate()) return;

        log.info("PublishingRequested received: {}", event);

        // 요약 및 표지 이미지 생성 로직 호출
        AiBookGeneration.generateBookSummary(event);
        AiBookGeneration.generateCoverImage(event);
    }

    // === 요약 생성 이벤트 수신 시, 처리 정보 등록 ===
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerated(@Payload BookSummaryGenerate event) {
        if (!event.validate()) return;

        log.info("BookSummaryGenerate received: {}", event);
        AiBookGeneration.registerProcessedBook(event);
    }

    // === 표지 생성 이벤트 수신 시, 처리 정보 등록 ===
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='CoverImageGenerated'")
    public void onCoverImageGenerated(@Payload CoverImageGenerated event) {
        if (!event.validate()) return;

        log.info("CoverImageGenerated received: {}", event);
        AiBookGeneration.registerProcessedBook(event);
    }

    // === 등록 완료 시 구독료 자동 책정 ===
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Registered'")
    public void onRegistered(@Payload Registered event) {
        if (!event.validate()) return;

        log.info("Registered event received (for pricing): {}", event);
        AiBookGeneration.subscriptionFeePolicy(event);
    }
}
