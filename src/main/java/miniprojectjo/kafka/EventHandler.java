package miniprojectjo.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.domain.*;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @StreamListener("event-in")
    public void handleEvent(@Payload String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String eventType = node.get("eventType").asText();

            log.info("📥 Kafka 이벤트 수신: {}", eventType);

            switch (eventType) {
                case "BookSummaryGenerate":
                    BookSummaryGenerate summaryEvent = objectMapper.treeToValue(node, BookSummaryGenerate.class);
                    log.info("📘 요약 생성됨: {}", summaryEvent.getSummary());
                    break;

                case "CoverImageGenerated":
                    CoverImageGenerated coverEvent = objectMapper.treeToValue(node, CoverImageGenerated.class);
                    log.info("🖼️ 표지 생성됨: {}", coverEvent.getCoverImageUrl());
                    break;

                case "Registered":
                    Registered regEvent = objectMapper.treeToValue(node, Registered.class);
                    log.info("📚 등록 완료 상태: {}", regEvent.getStatus());
                    break;

                case "SubscriptionFeeCalculated":
                    SubscriptionFeeCalculated feeEvent = objectMapper.treeToValue(node, SubscriptionFeeCalculated.class);
                    log.info("💰 구독료 책정 완료: {}원", feeEvent.getSubscriptionFee());
                    break;

                default:
                    log.warn("⚠️ 알 수 없는 이벤트 타입: {}", eventType);
            }

        } catch (Exception e) {
            log.error("❌ 이벤트 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}
