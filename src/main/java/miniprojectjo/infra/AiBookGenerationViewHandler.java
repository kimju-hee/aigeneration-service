package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBookGenerationViewHandler {

    private final AiBookGenerationRepository aiBookGenerationRepository;

    private <T> T decodePayload(String encodedPayload, Class<T> clazz) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedPayload);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(decodedBytes, clazz);
        } catch (Exception e) {
            log.error("❌ Failed to decode payload into class {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerate(@Payload byte[] rawPayload) {
        try {
            String encodedPayload = new String(rawPayload); // byte[] → String
            BookSummaryGenerate event = decodePayload(encodedPayload, BookSummaryGenerate.class);
            if (event != null && event.validate()) {
                log.info("📘 BookSummaryGenerate received: {}", event);
                AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
                if (book != null) {
                    book.generateBookSummary(event);
                    aiBookGenerationRepository.save(book);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error decoding BookSummaryGenerate: {}", e.getMessage(), e);
        }
    }


    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='CoverImageGenerated'")
    public void onCoverImageGenerated(@Payload String encodedPayload) {
        CoverImageGenerated event = decodePayload(encodedPayload, CoverImageGenerated.class);
        if (event != null && event.validate()) {
            log.info("🖼️ CoverImageGenerated received: {}", event);
            AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
            if (book != null) {
                book.registerProcessedBook(event);
                aiBookGenerationRepository.save(book);
            } else {
                log.warn("🖼️ AiBookGeneration(ID: {}) not found", event.getId());
            }
        }
    }
}
