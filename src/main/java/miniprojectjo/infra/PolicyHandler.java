package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.AiBookGeneration;
import miniprojectjo.infra.AiBookGenerationRepository;
import miniprojectjo.domain.BookSummaryGenerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
public class PolicyHandler {

    @Autowired
    AiBookGenerationRepository aiBookGenerationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private BookSummaryGenerate decodeEvent(String base64Encoded) {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(base64Encoded), StandardCharsets.UTF_8);
            return objectMapper.readValue(decodedJson, BookSummaryGenerate.class);
        } catch (Exception e) {
            throw new RuntimeException("❌ 역직렬화 실패: " + e.getMessage(), e);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void onBookSummaryGenerated(@Payload String message) {
        BookSummaryGenerate event = decodeEvent(message);

        Optional<AiBookGeneration> optional = aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId());
        if (optional.isPresent()) {
            AiBookGeneration book = optional.get();
            book.setSummary(event.getSummary());
            book.setStatus("SUMMARY_GENERATED");
            aiBookGenerationRepository.save(book);
            System.out.println("✅ 요약 정보 저장 완료: " + book.getId());
        } else {
            System.out.println("❌ 해당 manuscriptId를 가진 AiBookGeneration이 없습니다: " + event.getManuscriptId());
        }
    }
}
