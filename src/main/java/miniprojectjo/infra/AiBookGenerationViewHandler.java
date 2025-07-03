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
import java.util.Optional;

@Slf4j
@Service
public class AiBookGenerationViewHandler {

    @Autowired
    AiBookGenerationRepository aiBookGenerationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void handleAllViewEvents(@Payload String message) {
        try {
            AbstractEvent event = KafkaMessageUtils.decodeToAbstractEvent(message);

            log.info("📨 Kafka 이벤트 수신됨: {}", event.getEventType());

            if (event instanceof BookSummaryGenerate) {
                handleBookSummaryGenerated((BookSummaryGenerate) event);

            } else if (event instanceof Registered) {
                handleRegistered((Registered) event);

            } else {
                log.warn("📭 처리 대상 이벤트가 아님 → 무시됨");
            }

        } catch (Exception e) {
            log.error("❌ [ViewHandler] 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void handleBookSummaryGenerated(BookSummaryGenerate event) {
        Optional<AiBookGeneration> optionalView = aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId());
        if (optionalView.isPresent()) {
            AiBookGeneration view = optionalView.get();
            view.setSummary(event.getSummary());
            view.setStatus("SUMMARY_CREATED");
            view.setUpdatedAt(new Date());

            aiBookGenerationRepository.save(view);
            log.info("✅ [BookSummaryGenerate] AiBookGeneration summary 업데이트 완료");
        } else {
            log.warn("⚠️ manuscriptId={} 에 해당하는 AiBookGeneration이 존재하지 않습니다", event.getManuscriptId());
        }
    }

    private void handleRegistered(Registered event) {
        Optional<AiBookGeneration> optionalView = aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId());
        if (optionalView.isPresent()) {
            AiBookGeneration view = optionalView.get();
            view.setSummary(event.getSummary());
            view.setCoverImageUrl(event.getCoverImageUrl());
            view.setSubscriptionFee(event.getSubscriptionFee());
            view.setStatus("DONE");
            view.setUpdatedAt(new Date());

            aiBookGenerationRepository.save(view);
            log.info("✅ [Registered] 최종 상태 업데이트 완료");
        } else {
            log.warn("⚠️ manuscriptId={} 에 해당하는 AiBookGeneration이 존재하지 않습니다", event.getManuscriptId());
        }
    }
}
