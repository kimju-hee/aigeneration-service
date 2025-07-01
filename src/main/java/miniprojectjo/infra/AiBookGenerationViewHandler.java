package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.domain.*;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiBookGenerationViewHandler {

    private final ProcessedResultRepository processedResultRepository;
    private final ObjectMapper objectMapper;

    @StreamListener(value = "event-in", condition = "headers['type']=='BookSummaryGenerate'")
    public void onBookSummaryGenerate(@Payload String payload) {
        BookSummaryGenerate event = decodeEvent(payload);
        log.info("📚 BookSummaryGenerate 이벤트 수신: {}", event);

        ProcessedResult result = processedResultRepository.findById(event.getId())
                .orElseThrow(() -> new IllegalArgumentException("❌ 존재하지 않는 ID"));

        result.setSummary(event.getSummary());
        result.setStatus("SUMMARY_COMPLETED");

        processedResultRepository.save(result);
    }

    private BookSummaryGenerate decodeEvent(String payload) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(payload);
            String decodedJson = new String(decodedBytes, StandardCharsets.UTF_8);

            log.info("✅ 디코딩된 JSON: {}", decodedJson);

            return objectMapper.readValue(decodedJson, BookSummaryGenerate.class);
        } catch (Exception e) {
            log.error("❌ [BookSummaryGenerate] 역직렬화 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
