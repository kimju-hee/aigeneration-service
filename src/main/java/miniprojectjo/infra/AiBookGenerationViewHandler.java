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
public class AiBookGenerationViewHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private <T> T decodeEvent(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("❌ [{}] 역직렬화 실패: {}", clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @StreamListener(value = "event-in", condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerate(@Payload String payload) {
        BookSummaryGenerate event = decodeEvent(payload, BookSummaryGenerate.class);
        AiBookGeneration.registerProcessedBook(event);
    }

    @StreamListener(value = "event-in", condition = "headers['type']=='Registered'")
    public void onRegistered(@Payload String payload) {
        Registered event = decodeEvent(payload, Registered.class);
        AiBookGeneration.subscriptionFeePolicy(event);
    }
}
