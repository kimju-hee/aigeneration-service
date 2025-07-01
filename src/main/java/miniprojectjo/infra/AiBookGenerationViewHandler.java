package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBookGenerationViewHandler {

    private final AiBookGenerationRepository aiBookGenerationRepository;

    // ✅ Base64 문자열 디코딩 후 JSON 역직렬화 메서드
    private <T> T decodePayload(String encodedPayload, Class<T> clazz) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedPayload);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(decodedBytes, clazz);
        } catch (Exception e) {
            log.error("❌ Failed to decode payload into {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerate(@Payload BookSummaryGenerate event) {
        try {
            if (event != null && event.validate()) {
                log.info("✅ [BookSummaryGenerate 수신]: {}", event);
                AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
                if (book != null) {
                    book.generateBookSummary(event);
                    aiBookGenerationRepository.save(book);
                } else {
                    log.warn("✅ [BookSummaryGenerate] ID {}의 엔티티 없음", event.getId());
                }
            }
        } catch (Exception e) {
            log.error("❌ [BookSummaryGenerate 처리 중 오류]: {}", e.getMessage(), e);
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Registered'")
    public void onRegistered(Message<byte[]> message) {
        try {
            String encodedPayload = new String(message.getPayload(), StandardCharsets.UTF_8);
            Registered event = decodePayload(encodedPayload, Registered.class);

            if (event != null && event.validate()) {
                log.info("📦 Registered received: {}", event);
                AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
                if (book != null) {
                    book.registerProcessedBook(event);
                    aiBookGenerationRepository.save(book);
                } else {
                    log.warn("📦 [Registered] ID {}의 책이 존재하지 않습니다.", event.getId());
                }
            }
        } catch (Exception e) {
            log.error("❌ [Registered 처리 중 오류]: {}", e.getMessage(), e);
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='CoverImageGenerated'")
    public void onCoverImageGenerated(@Payload CoverImageGenerated event) {
        try {
            if (event != null && event.validate()) {
                log.info("🖼️ CoverImageGenerated received: {}", event);
                AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
                if (book != null) {
                    book.registerProcessedBook(event);
                    aiBookGenerationRepository.save(book);
                } else {
                    log.warn("🖼️ [CoverImageGenerated] ID {}의 책이 존재하지 않습니다.", event.getId());
                }
            }
        } catch (Exception e) {
            log.error("❌ [CoverImageGenerated 처리 중 오류]: {}", e.getMessage(), e);
        }
    }
}
