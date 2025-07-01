package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBookGenerationViewHandler {

    private final AiBookGenerationRepository aiBookGenerationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private <T> T decodeEvent(Message<byte[]> message, Class<T> clazz) {
        try {
            String base64 = new String(message.getPayload(), StandardCharsets.UTF_8);
            String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("❌ [{}] 역직렬화 실패: {}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerate(Message<byte[]> message) {
        BookSummaryGenerate event = decodeEvent(message, BookSummaryGenerate.class);
        if (event != null && event.validate()) {
            log.info("✅ BookSummaryGenerate 수신: {}", event);
            AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
            if (book != null) {
                book.generateBookSummary(event);
                aiBookGenerationRepository.save(book);
            }
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='CoverImageGenerated'")
    public void onCoverImageGenerated(Message<byte[]> message) {
        CoverImageGenerated event = decodeEvent(message, CoverImageGenerated.class);
        if (event != null && event.validate()) {
            log.info("🖼️ CoverImageGenerated 수신: {}", event);
            AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
            if (book != null) {
                book.registerProcessedBook(event);
                aiBookGenerationRepository.save(book);
            }
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Registered'")
    public void onRegistered(Message<byte[]> message) {
        Registered event = decodeEvent(message, Registered.class);
        if (event != null && event.validate()) {
            log.info("📦 Registered 수신: {}", event);
            AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
            if (book != null) {
                book.registerProcessedBook(event);
                aiBookGenerationRepository.save(book);
            }
        }
    }
}
