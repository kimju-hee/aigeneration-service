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
public class PolicyHandler {

    @Autowired
    private AiBookGenerationRepository aiBookGenerationRepository;

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

                case "PublishingRequested":
                    handlePublishingRequested((PublishingRequested) event);
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
        aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId()).ifPresentOrElse(
            book -> {
                book.setSummary(event.getSummary());
                book.setStatus("SUMMARY_GENERATED");
                book.setUpdatedAt(new Date());
                aiBookGenerationRepository.save(book);
                log.info("✅ 요약 정보 저장 완료: {}", book.getId());

                // 요약 완료 후 → 커버 이미지 생성 요청
                CoverImageGenerated coverEvent = new CoverImageGenerated(book);
                coverEvent.publish();
                log.info("➡️ 커버 이미지 생성 이벤트 발행 완료");
            },
            () -> log.warn("❌ 해당 manuscriptId를 가진 AiBookGeneration이 없습니다: {}", event.getManuscriptId())
        );
    }

    private void handleCoverImageGenerated(CoverImageGenerated event) {
        aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId()).ifPresentOrElse(
            book -> {
                book.setCoverImageUrl(event.getCoverImageUrl());
                book.setStatus("COVER_GENERATED");
                book.setUpdatedAt(new Date());
                aiBookGenerationRepository.save(book);
                log.info("✅ 커버 이미지 저장 완료: {}", book.getId());

                // 요약도 완료되어 있다면 → 가격 책정 요청
                if (book.getSummary() != null && !book.getSummary().isEmpty()) {
                    SubscriptionFeeCalculated feeEvent = new SubscriptionFeeCalculated(book);
                    feeEvent.publish();
                    log.info("➡️ 구독료 계산 이벤트 발행 완료");
                }
            },
            () -> log.warn("❌ 해당 manuscriptId를 가진 AiBookGeneration이 없습니다: {}", event.getManuscriptId())
        );
    }

    private void handleSubscriptionFeeCalculated(SubscriptionFeeCalculated event) {
        aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId()).ifPresentOrElse(
            book -> {
                book.setSubscriptionFee(event.getSubscriptionFee());
                book.setStatus("SUBSCRIPTION_CALCULATED");
                book.setUpdatedAt(new Date());
                aiBookGenerationRepository.save(book);
                log.info("✅ 구독료 저장 완료: {}", book.getId());

                // 최종 등록 요건 모두 충족되었을 때 → Registered 이벤트 발행
                if (book.getSummary() != null && book.getCoverImageUrl() != null && book.getSubscriptionFee() != null) {
                    Registered registeredEvent = new Registered();
                    registeredEvent.setManuscriptId(book.getManuscriptId());
                    registeredEvent.setSummary(book.getSummary());
                    registeredEvent.setCoverImageUrl(book.getCoverImageUrl());
                    registeredEvent.setSubscriptionFee(book.getSubscriptionFee());
                    registeredEvent.setStatus("DONE");
                    registeredEvent.setCreatedAt(new Date().getTime());
                    registeredEvent.publish();
                    log.info("➡️ Registered 최종 등록 이벤트 발행 완료");
                }
            },
            () -> log.warn("❌ 해당 manuscriptId를 가진 AiBookGeneration이 없습니다: {}", event.getManuscriptId())
        );
    }

    private void handleRegistered(Registered event) {
        aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId()).ifPresentOrElse(
            book -> {
                book.setStatus(event.getStatus());
                book.setUpdatedAt(new Date());
                aiBookGenerationRepository.save(book);
                log.info("✅ Registered 상태 저장 완료: {}", book.getId());
            },
            () -> log.warn("❌ 해당 manuscriptId를 가진 AiBookGeneration이 없습니다: {}", event.getManuscriptId())
        );
    }

    private void handlePublishingRequested(PublishingRequested event) {
        AiBookGeneration.generateBookSummary(event);
        log.info("📬 [PublishingRequested] 이벤트로 자동 요약 시작");
    }
}
