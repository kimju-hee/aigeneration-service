package miniprojectjo.utils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.domain.*;
import miniprojectjo.infra.AbstractEvent;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class KafkaMessageUtils {

    private static final ObjectMapper objectMapper;

    // 이벤트 타입 ↔ 클래스 맵핑
    private static final Map<String, Class<? extends AbstractEvent>> EVENT_TYPE_MAP = new HashMap<>();

    static {
        objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("miniprojectjo.domain")
                .allowIfBaseType(java.util.Date.class)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        // ✨ 이벤트 매핑 등록
        EVENT_TYPE_MAP.put("BookSummaryGenerate", BookSummaryGenerate.class);
        EVENT_TYPE_MAP.put("CoverImageGenerated", CoverImageGenerated.class);
        EVENT_TYPE_MAP.put("SubscriptionFeeCalculated", SubscriptionFeeCalculated.class);
        EVENT_TYPE_MAP.put("Registered", Registered.class);
    }

    public static AbstractEvent decodeToAbstractEvent(String payload) throws Exception {
        try {
            log.info("📩 Kafka 메시지 수신: {}", payload);

            String json = decodeBase64Twice(payload);
            String eventType = extractEventType(json);

            Class<? extends AbstractEvent> eventClass = EVENT_TYPE_MAP.get(eventType);
            if (eventClass == null) {
                throw new IllegalArgumentException("❌ 알 수 없는 eventType: " + eventType);
            }

            return objectMapper.readValue(json, eventClass);

        } catch (Exception e) {
            log.error("❌ Kafka 메시지 역직렬화 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 🔄 공통 Base64 디코딩 메서드 (중첩 2회)
    public static String decodeBase64Twice(String payload) {
        if (payload != null && payload.length() > 1 &&
            payload.startsWith("\"") && payload.endsWith("\"")) {
            payload = payload.substring(1, payload.length() - 1);
            log.info("🧹 양쪽 큰따옴표 제거 후 payload: {}", payload);
        }

        try {
            String onceDecoded = new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);
            log.info("📦 1차 디코딩: {}", onceDecoded);
            String twiceDecoded = new String(Base64.getDecoder().decode(onceDecoded), StandardCharsets.UTF_8);
            log.info("📦 2차 디코딩 → 최종 JSON: {}", twiceDecoded);
            return twiceDecoded;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Base64 디코딩 실패: " + e.getMessage(), e);
        }
    }

    // 🧭 JSON 내 eventType 추출
    private static String extractEventType(String json) throws Exception {
        JsonNode rootNode = objectMapper.readTree(json);
        if (!rootNode.has("eventType")) {
            throw new IllegalArgumentException("❌ 'eventType' 필드가 없습니다.");
        }
        String eventType = rootNode.get("eventType").asText();
        log.info("🧭 eventType = {}", eventType);
        return eventType;
    }
}
