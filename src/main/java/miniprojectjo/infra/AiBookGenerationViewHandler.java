package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.AiBookGeneration;
import miniprojectjo.infra.AiBookGenerationRepository;
import miniprojectjo.domain.BookSummaryGenerate;
import miniprojectjo.utils.KafkaMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class AiBookGenerationViewHandler {

    @Autowired
    AiBookGenerationRepository aiBookGenerationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookSummaryGenerated_then_UPDATE(@Payload String message) {
        try {
            String decodedJson = KafkaMessageUtils.decodeBase64Twice(message);
            BookSummaryGenerate event = objectMapper.readValue(decodedJson, BookSummaryGenerate.class);

            log.info("📨 수신 이벤트 타입: {}", event.getEventType());

            Optional<AiBookGeneration> optionalView = aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId());
            if (optionalView.isPresent()) {
                AiBookGeneration view = optionalView.get();
                view.setSummary(event.getSummary());
                view.setStatus("SUMMARY_CREATED");
                view.setUpdatedAt(new Date());

                aiBookGenerationRepository.save(view);
                log.info("✅ AiBookGeneration summary 업데이트 완료");
            } else {
                log.warn("⚠️ manuscriptId={} 에 해당하는 엔터티가 존재하지 않습니다", event.getManuscriptId());
            }

        } catch (Exception e) {
            log.error("❌ [BookSummaryGenerate] 역직렬화 실패: {}", e.getMessage(), e);
        }
    }
}
