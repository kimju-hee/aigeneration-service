package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.domain.*;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProcessedResultViewHandler {

    private final ProcessedResultRepository processedResultRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private <T> T decodeEvent(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz); // ✅ Base64 decode 제거
        } catch (Exception e) {
            log.error("❌ [{}] 역직렬화 실패: {}", clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @StreamListener(value = "event-in", condition = "headers['type']=='BookSummaryGenerate'")
    public void whenBookSummaryGenerate_then_UPDATE_1(@Payload String payload) {
        BookSummaryGenerate event = decodeEvent(payload, BookSummaryGenerate.class);
        processedResultRepository.findByManuscriptId(event.getManuscriptId()).ifPresent(view -> {
            view.setSummary(event.getSummary());
            processedResultRepository.save(view);
            log.info("✅ 요약 저장 완료 (manuscriptId: {})", event.getManuscriptId());
        });
    }

    @StreamListener(value = "event-in", condition = "headers['type']=='BookSummaryGenerate'")
    public void whenBookSummaryGenerate_then_UPDATE_3(@Payload String payload) {
        BookSummaryGenerate event = decodeEvent(payload, BookSummaryGenerate.class);
        processedResultRepository.findByManuscriptId(event.getManuscriptId()).ifPresent(view -> {
            view.setStatus("SUMMARY_CREATED");
            processedResultRepository.save(view);
            log.info("📌 상태 변경: SUMMARY_CREATED (manuscriptId: {})", event.getManuscriptId());
        });
    }

    @StreamListener(value = "event-in", condition = "headers['type']=='CoverImageGenerated'")
    public void whenCoverImageGenerated_then_UPDATE_2(@Payload String payload) {
        CoverImageGenerated event = decodeEvent(payload, CoverImageGenerated.class);
        processedResultRepository.findByManuscriptId(event.getManuscriptId()).ifPresent(view -> {
            view.setCoverImageUrl(event.getCoverImageUrl());
            processedResultRepository.save(view);
            log.info("✅ 표지 저장 완료 (manuscriptId: {})", event.getManuscriptId());
        });
    }

    @StreamListener(value = "event-in", condition = "headers['type']=='CoverImageGenerated'")
    public void whenCoverImageGenerated_then_UPDATE_1(@Payload String payload) {
        CoverImageGenerated event = decodeEvent(payload, CoverImageGenerated.class);
        processedResultRepository.findByManuscriptId(event.getManuscriptId()).ifPresent(view -> {
            view.setStatus("COVER_CREATED");
            processedResultRepository.save(view);
            log.info("📌 상태 변경: COVER_CREATED (manuscriptId: {})", event.getManuscriptId());
        });
    }

    @StreamListener(value = "event-in", condition = "headers['type']=='Registered'")
    public void whenRegistered_then_UPDATE_4(@Payload String payload) {
        Registered event = decodeEvent(payload, Registered.class);
        processedResultRepository.findByManuscriptId(event.getManuscriptId()).ifPresent(view -> {
            view.setStatus("REGISTERED");
            processedResultRepository.save(view);
            log.info("📌 상태 변경: REGISTERED (manuscriptId: {})", event.getManuscriptId());
        });
    }

    @StreamListener(value = "event-in", condition = "headers['type']=='SubscriptionFeeCalculated'")
    public void whenSubscriptionFeeCalculated_then_UPDATE_5(@Payload String payload) {
        SubscriptionFeeCalculated event = decodeEvent(payload, SubscriptionFeeCalculated.class);
        processedResultRepository.findByManuscriptId(event.getManuscriptId()).ifPresent(view -> {
            view.setSubscriptionFee(event.getSubscriptionFee());
            view.setStatus("PRICED");
            processedResultRepository.save(view);
            log.info("💰 구독료 저장 완료 (manuscriptId: {})", event.getManuscriptId());
        });
    }
}
