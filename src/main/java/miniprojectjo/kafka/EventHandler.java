package miniprojectjo.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import miniprojectjo.domain.*;

@Service
public class EventHandler {

    @StreamListener("event-in")
    public void handleEvent(@Payload String rawPayload) {
        try {
            // Base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(rawPayload);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            String eventType = node.get("eventType").asText();

            System.out.println("🟡 Kafka 수신 이벤트 타입: " + eventType);

            switch (eventType) {
                case "BookSummaryGenerate":
                    BookSummaryGenerate summaryEvent = mapper.treeToValue(node, BookSummaryGenerate.class);
                    handleBookSummaryGenerate(summaryEvent);
                    break;

                case "Registered":
                    Registered registered = mapper.treeToValue(node, Registered.class);
                    handleRegistered(registered);
                    break;

                default:
                    System.out.println("⚠️ 알 수 없는 이벤트 타입: " + eventType);
            }

        } catch (Exception e) {
            System.out.println("❌ 이벤트 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleBookSummaryGenerate(BookSummaryGenerate event) {
        System.out.println("📚 BookSummaryGenerate 이벤트 수신!");
        System.out.println("ID: " + event.getId());
        System.out.println("요약: " + event.getSummary());
        System.out.println("생성일: " + event.getCreatedAt());
    }

    private void handleRegistered(Registered event) {
        System.out.println("✅ Registered 이벤트 수신!");
        System.out.println("책 상태: " + event.getStatus());
        System.out.println("구독료: " + event.getSubscriptionFee());
    }
}
