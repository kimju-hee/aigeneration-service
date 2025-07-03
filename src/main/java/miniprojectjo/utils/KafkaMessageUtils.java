package miniprojectjo.utils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
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

    private static final Map<String, Class<? extends AbstractEvent>> EVENT_TYPE_MAP = new HashMap<>();

    static {
        objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("miniprojectjo.domain")
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        EVENT_TYPE_MAP.put("BookSummaryGenerate", BookSummaryGenerate.class);
        EVENT_TYPE_MAP.put("CoverImageGenerated", CoverImageGenerated.class);
        EVENT_TYPE_MAP.put("SubscriptionFeeCalculated", SubscriptionFeeCalculated.class);
        EVENT_TYPE_MAP.put("Registered", Registered.class);
    }

    public static AbstractEvent decodeToAbstractEvent(String payload) throws Exception {
        try {
            log.info("📩 Kafka 메시지 수신 (원본): {}", payload);

            // 큰따옴표 제거
            String raw = stripQuotesIfExist(payload);

            // Base64 디코딩 시도
            String json = tryDecodeBase64(raw);
            log.info("📦 디코딩된 JSON: {}", json);

            // eventType 추출
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

    private static String stripQuotesIfExist(String input) {
        if (input == null || input.isBlank()) return input;
        if (input.startsWith("\"") && input.endsWith("\"")) {
            return input.substring(1, input.length() - 1);
        }
        return input;
    }

    private static String tryDecodeBase64(String input) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            // JSON 여부 간단 체크
            if (decoded.trim().startsWith("{") || decoded.trim().startsWith("[")) {
                return decoded;
            }
        } catch (IllegalArgumentException ignore) {
            // 디코딩 실패 → 그대로 반환
        }
        return input;
    }

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
