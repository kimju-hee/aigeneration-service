package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.AiBookGeneration;
import miniprojectjo.domain.BookSummaryGenerate;
import miniprojectjo.domain.CoverImageGenerated;
import miniprojectjo.domain.SubscriptionFeeCalculated;
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

    @StreamListener(KafkaProcessor.INPUT)
    public void onBookSummaryGenerated(@Payload String message) {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
            BookSummaryGenerate event = objectMapper.readValue(decodedJson, BookSummaryGenerate.class);

            Optional<AiBookGeneration> optional = aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId());
            if (optional.isPresent()) {
                AiBookGeneration book = optional.get();
                book.setSummary(event.getSummary());
                book.setStatus("SUMMARY_GENERATED");
                book.setUpdatedAt(new java.util.Date());
                aiBookGenerationRepository.save(book);
                System.out.println("✅ 요약 정보 저장 완료: " + book.getId());
            } else {
                System.out.println("❌ 해당 manuscriptId를 가진 AiBookGeneration이 없습니다: " + event.getManuscriptId());
            }

        } catch (Exception e) {
            System.out.println("❌ [BookSummaryGenerate] 역직렬화 실패: " + e.getMessage());
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void onCoverImageGenerated(@Payload String message) {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
            CoverImageGenerated event = objectMapper.readValue(decodedJson, CoverImageGenerated.class);

            Optional<AiBookGeneration> optional = aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId());
            if (optional.isPresent()) {
                AiBookGeneration book = optional.get();
                book.setCoverImageUrl(event.getCoverImageUrl());
                book.setStatus("COVER_GENERATED");
                book.setUpdatedAt(new java.util.Date());
                aiBookGenerationRepository.save(book);
                System.out.println("✅ 커버 이미지 저장 완료: " + book.getId());
            } else {
                System.out.println("❌ 해당 manuscriptId를 가진 AiBookGeneration이 없습니다: " + event.getManuscriptId());
            }

        } catch (Exception e) {
            System.out.println("❌ [CoverImageGenerated] 역직렬화 실패: " + e.getMessage());
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void onSubscriptionFeeCalculated(@Payload String message) {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
            SubscriptionFeeCalculated event = objectMapper.readValue(decodedJson, SubscriptionFeeCalculated.class);

            Optional<AiBookGeneration> optional = aiBookGenerationRepository.findByManuscriptId(event.getManuscriptId());
            if (optional.isPresent()) {
                AiBookGeneration book = optional.get();
                book.setSubscriptionFee(event.getSubscriptionFee());
                book.setStatus("SUBSCRIPTION_CALCULATED");
                book.setUpdatedAt(new java.util.Date());
                aiBookGenerationRepository.save(book);
                System.out.println("✅ 구독료 저장 완료: " + book.getId());
            } else {
                System.out.println("❌ 해당 manuscriptId를 가진 AiBookGeneration이 없습니다: " + event.getManuscriptId());
            }

        } catch (Exception e) {
            System.out.println("❌ [SubscriptionFeeCalculated] 역직렬화 실패: " + e.getMessage());
        }
    }
}
