package miniprojectjo.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import miniprojectjo.utils.KafkaMessageUtils;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBookGenerationViewHandler {

    private final AiBookGenerationRepository aiBookGenerationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void handleKafkaMessage(@Payload byte[] messageBytes) {
        String message = new String(messageBytes, StandardCharsets.UTF_8);
        try {
            // 공통 메시지 역직렬화 (Base64 + Jackson + eventType 기반)
            AbstractEvent event = KafkaMessageUtils.decodeToAbstractEvent(message);
            log.info("📨 수신 이벤트 타입: {}", event.getEventType());

            switch (event.getEventType()) {
                case "BookSummaryGenerate":
                    handleBookSummaryGenerate((BookSummaryGenerate) event);
                    break;

                case "CoverImageGenerated":
                    handleCoverImageGenerated((CoverImageGenerated) event);
                    break;

                case "SubscriptionFeeCalculated":
                    handleSubscriptionFeeCalculated((SubscriptionFeeCalculated) event);
                    break;

                default:
                    log.warn("⚠️ 처리되지 않은 이벤트 타입: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("❌ Kafka 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void handleBookSummaryGenerate(BookSummaryGenerate event) {
        if (!event.validate()) return;

        AiBookGeneration aiBookGeneration = new AiBookGeneration();
        aiBookGeneration.setId(event.getId());
        aiBookGeneration.setManuscriptId(event.getManuscriptId());
        aiBookGeneration.setSummary(event.getSummary());
        aiBookGeneration.setStatus("SUMMARY_CREATED");
        aiBookGeneration.setCreatedAt(event.getCreatedAt());
        aiBookGeneration.setUpdatedAt(new Date());

        aiBookGenerationRepository.save(aiBookGeneration);
    }

    private void handleCoverImageGenerated(CoverImageGenerated event) {
        if (!event.validate()) return;

        aiBookGenerationRepository.findById(event.getId()).ifPresent(aiBookGeneration -> {
            aiBookGeneration.setCoverImageUrl(event.getCoverImageUrl());
            aiBookGeneration.setStatus("COVER_CREATED");
            aiBookGeneration.setUpdatedAt(new Date());
            aiBookGenerationRepository.save(aiBookGeneration);
        });
    }

    private void handleSubscriptionFeeCalculated(SubscriptionFeeCalculated event) {
        if (!event.validate()) return;

        aiBookGenerationRepository.findById(event.getId()).ifPresent(aiBookGeneration -> {
            aiBookGeneration.setSubscriptionFee(event.getSubscriptionFee());
            aiBookGeneration.setStatus("FEE_CALCULATED");
            aiBookGeneration.setUpdatedAt(new Date());
            aiBookGenerationRepository.save(aiBookGeneration);
        });
    }
}
