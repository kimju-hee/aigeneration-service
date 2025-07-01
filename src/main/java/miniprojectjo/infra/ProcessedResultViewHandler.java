package miniprojectjo.infra;

import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import miniprojectjo.utils.KafkaMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessedResultViewHandler {

    @Autowired
    private ProcessedResultRepository processedResultRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void handleAllEvents(@Payload String message) {
        try {
            AbstractEvent event = KafkaMessageUtils.decodeToAbstractEvent(message);

            if (event instanceof BookSummaryGenerate) {
                handleBookSummaryGenerate((BookSummaryGenerate) event);
            } else if (event instanceof CoverImageGenerated) {
                handleCoverImageGenerated((CoverImageGenerated) event);
            } else if (event instanceof SubscriptionFeeCalculated) {
                handleSubscriptionFeeCalculated((SubscriptionFeeCalculated) event);
            } else {
                log.warn("⚠️ 알 수 없는 이벤트 타입 수신: {}", event.getEventType());
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
                ProcessedResult newResult = new ProcessedResult();
                newResult.setManuscriptId(event.getManuscriptId());
                return newResult;
            });

        result.setSummary(event.getSummary());
        result.setStatus("SUMMARY_GENERATED");
        processedResultRepository.save(result);
    }

    private void handleCoverImageGenerated(CoverImageGenerated event) {
        if (!event.validate()) return;

        ProcessedResult result = processedResultRepository
            .findByManuscriptId(event.getManuscriptId())
            .orElseGet(() -> {
                ProcessedResult newResult = new ProcessedResult();
                newResult.setManuscriptId(event.getManuscriptId());
                return newResult;
            });

        result.setCoverImageUrl(event.getCoverImageUrl());
        result.setStatus("COVER_GENERATED");
        processedResultRepository.save(result);
    }

    private void handleSubscriptionFeeCalculated(SubscriptionFeeCalculated event) {
        if (!event.validate()) return;

        ProcessedResult result = processedResultRepository
            .findByManuscriptId(event.getManuscriptId())
            .orElseGet(() -> {
                ProcessedResult newResult = new ProcessedResult();
                newResult.setManuscriptId(event.getManuscriptId());
                return newResult;
            });

        result.setSubscriptionFee(event.getSubscriptionFee());
        result.setStatus("REGISTERED");
        processedResultRepository.save(result);
    }
}
