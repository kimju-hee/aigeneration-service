package miniprojectjo.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import miniprojectjo.config.kafka.KafkaProcessor;
import miniprojectjo.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ProcessedResultViewHandler {

    @Autowired
    private ProcessedResultRepository processedResultRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @StreamListener(KafkaProcessor.INPUT)
    public void onBookSummaryGenerated(@Payload String message) {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
            BookSummaryGenerate event = objectMapper.readValue(decodedJson, BookSummaryGenerate.class);

            if (!event.validate()) return;

            ProcessedResult result = processedResultRepository.findByManuscriptId(event.getManuscriptId()).orElse(null);
            if (result != null) {
                result.setSummary(event.getSummary());
                result.setStatus("SUMMARY_GENERATED");
                processedResultRepository.save(result);
            }

        } catch (Exception e) {
            System.out.println("\u274c [BookSummaryGenerate] 역직렬화 실패: " + e.getMessage());
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void onCoverImageGenerated(@Payload String message) {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
            CoverImageGenerated event = objectMapper.readValue(decodedJson, CoverImageGenerated.class);

            if (!event.validate()) return;

            ProcessedResult result = processedResultRepository.findByManuscriptId(event.getManuscriptId()).orElse(null);
            if (result != null) {
                result.setCoverImageUrl(event.getCoverImageUrl());
                result.setStatus("COVER_GENERATED");
                processedResultRepository.save(result);
            }

        } catch (Exception e) {
            System.out.println("\u274c [CoverImageGenerated] 역직렬화 실패: " + e.getMessage());
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void onSubscriptionFeeCalculated(@Payload String message) {
        try {
            String decodedJson = new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
            SubscriptionFeeCalculated event = objectMapper.readValue(decodedJson, SubscriptionFeeCalculated.class);

            if (!event.validate()) return;

            ProcessedResult result = processedResultRepository.findByManuscriptId(event.getManuscriptId()).orElse(null);
            if (result != null) {
                result.setSubscriptionFee(event.getSubscriptionFee());
                result.setStatus("REGISTERED");
                processedResultRepository.save(result);
            }

        } catch (Exception e) {
            System.out.println("\u274c [SubscriptionFeeCalculated] 역직렬화 실패: " + e.getMessage());
        }
    }
}
