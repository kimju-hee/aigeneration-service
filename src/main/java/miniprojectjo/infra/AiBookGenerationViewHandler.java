package miniprojectjo.infra;

// import com.fasterxml.jackson.databind.ObjectMapper; // 이제 필요 없음
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
// import org.springframework.messaging.Message; // 이제 필요 없음
// import java.util.Base64; // 이제 필요 없음
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBookGenerationViewHandler {

    private final AiBookGenerationRepository aiBookGenerationRepository;

    // decodePayload 메서드를 완전히 제거합니다.
    // Spring Cloud Stream이 자동으로 JSON 역직렬화를 처리할 것입니다.

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerate(@Payload BookSummaryGenerate event) { // @Payload로 직접 객체 받기
        try {
            if (event != null && event.validate()) {
                log.info("✅ [BookSummaryGenerate 수신]: {}", event);
                AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
                if (book != null) {
                    book.generateBookSummary(event);
                    aiBookGenerationRepository.save(book);
                } else {
                    log.warn("✅ [BookSummaryGenerate 수신]: AiBookGeneration(ID: {})을 찾을 수 없습니다.", event.getId());
                }
            }
        } catch (Exception e) {
            // 이제 이 catch 블록은 Spring Cloud Stream의 MessageConversionException을 직접 받지 않고,
            // 여러분의 비즈니스 로직(event.validate() 등)에서 발생하는 예외를 처리하게 됩니다.
            log.error("❌ [BookSummaryGenerate 처리 중 오류]: {}", e.getMessage(), e);
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Registered'")
    public void onRegistered(@Payload Registered event) { // @Payload로 직접 객체 받기
        try {
            if (event != null && event.validate()) {
                log.info("📦 Registered received: {}", event);
                AiBookGeneration book = aiBookGenerationRepository.findById(event.getId()).orElse(null);
                if (book != null) {
                    book.registerProcessedBook(event); // Registered 이벤트 객체를 인자로 넘기는 것이 더 적절해 보입니다.
                    aiBookGenerationRepository.save(book);
                } else {
                    log.warn("📦 Registered 수신]: AiBookGeneration(ID: {})을 찾을 수 없습니다.", event.getId());
                }
            }
        } catch (Exception e) {
            log.error("❌ [Registered 처리 중 오류]: {}", e.getMessage(), e);
        }
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='CoverImageGenerated'")
    public void onCoverImageGenerated(@Payload CoverImageGenerated event) { // @Payload로 직접 객체 받기
        try {
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
        } catch (Exception e) {
            log.error("❌ [CoverImageGenerated 처리 중 오류]: {}", e.getMessage(), e);
        }
    }
}