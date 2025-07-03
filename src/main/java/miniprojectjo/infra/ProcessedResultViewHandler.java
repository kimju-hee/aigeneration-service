package miniprojectjo.infra;

import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import miniprojectjo.utils.KafkaMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class ProcessedResultViewHandler {

    @Autowired
    private ProcessedResultRepository processedResultRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void handleAllEvents(@Payload String message) {
        try {
            AbstractEvent event = KafkaMessageUtils.decodeToAbstractEvent(message);
            String eventType = event.getEventType();
            log.info("📨 Kafka 이벤트 수신됨: {}", eventType);

            switch (eventType) {
                case "BookSummaryGenerate":
                    handleBookSummaryGenerate((BookSummaryGenerate) event);
                    break;

                case "CoverImageGenerated":
                    handleCoverImageGenerated((CoverImageGenerated) event);
                    break;

                case "SubscriptionFeeCalculated":
                    handleSubscriptionFeeCalculated((SubscriptionFeeCalculated) event);
                    break;

                case "Registered":
                    handleRegistered((Registered) event);
                    break;

                default:
                    log.warn("⚠️ 알 수 없는 이벤트 타입 수신: {}", eventType);
            }

        } catch (Exception e) {
            log.error("❌ 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void handleBookSummaryGenerate(BookSummaryGenerate event) {
        if (!event.validate()) return;

        ProcessedResult result = processedResultRepository
            .findByManuscriptId(event.getManuscriptId())
            .orElseGet(() -> {
                log.info("🆕 [BookSummaryGenerate] 신규 엔티티 생성");
                ProcessedResult newResult = new ProcessedResult();
                newResult.setManuscriptId(event.getManuscriptId());
                return newResult;
            });

        result.setSummary(event.getSummary());
        result.setStatus("SUMMARY_GENERATED");
        result.setUpdatedAt(new Date());

        processedResultRepository.save(result);
        log.info("✅ [BookSummaryGenerate] 저장 완료: {}", result);
    }

    private void handleCoverImageGenerated(CoverImageGenerated event) {
        if (!event.validate()) return;

        ProcessedResult result = processedResultRepository
            .findByManuscriptId(event.getManuscriptId())
            .orElseGet(() -> {
                log.info("🆕 [CoverImageGenerated] 신규 엔티티 생성");
                ProcessedResult newResult = new ProcessedResult();
                newResult.setManuscriptId(event.getManuscriptId());
                return newResult;
            });

        result.setCoverImageUrl(event.getCoverImageUrl());
        result.setStatus("COVER_GENERATED");
        result.setUpdatedAt(new Date());

        processedResultRepository.save(result);
        log.info("✅ [CoverImageGenerated] 저장 완료: {}", result);
    }

    private void handleSubscriptionFeeCalculated(SubscriptionFeeCalculated event) {
        if (!event.validate()) return;

        ProcessedResult result = processedResultRepository
            .findByManuscriptId(event.getManuscriptId())
            .orElseGet(() -> {
                log.info("🆕 [SubscriptionFeeCalculated] 신규 엔티티 생성");
                ProcessedResult newResult = new ProcessedResult();
                newResult.setManuscriptId(event.getManuscriptId());
                return newResult;
            });

        result.setSubscriptionFee(event.getSubscriptionFee());
        result.setStatus("SUBSCRIPTION_CALCULATED");
        result.setUpdatedAt(new Date());

        processedResultRepository.save(result);
        log.info("✅ [SubscriptionFeeCalculated] 저장 완료: {}", result);
    }

    private void handleRegistered(Registered event) {
        if (!event.validate()) return;

        ProcessedResult result = processedResultRepository
            .findByManuscriptId(event.getManuscriptId())
            .orElseGet(() -> {
                log.info("🆕 [Registered] 신규 엔티티 생성");
                ProcessedResult newResult = new ProcessedResult();
                newResult.setManuscriptId(event.getManuscriptId());
                return newResult;
            });

        result.setSummary(event.getSummary());
        result.setCoverImageUrl(event.getCoverImageUrl());
        result.setSubscriptionFee(event.getSubscriptionFee());
        result.setStatus("DONE");
        result.setUpdatedAt(new Date());

        processedResultRepository.save(result);
        log.info("✅ [Registered] 최종 저장 완료: {}", result);
    }
}
