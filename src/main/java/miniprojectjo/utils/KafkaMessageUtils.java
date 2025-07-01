package miniprojectjo.utils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import miniprojectjo.domain.*;
import miniprojectjo.infra.AbstractEvent;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class KafkaMessageUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("miniprojectjo.domain")  // 도메인 이벤트 타입 허용
                .allowIfBaseType(java.util.Date.class)     // java.util.Date 허용 추가
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
    }

    /**
     * ✅ Kafka 메시지 문자열을 적절한 Event 클래스(BookSummaryGenerate 등)로 매핑
     */
    public static AbstractEvent decodeToAbstractEvent(String payload) throws Exception {
        try {
            log.info("📩 Kafka 메시지 수신: {}", payload);

            // 1) payload가 큰따옴표로 감싸져 있다면 제거
            if (payload != null && payload.length() > 1 &&
                payload.startsWith("\"") && payload.endsWith("\"")) {
                payload = payload.substring(1, payload.length() - 1);
                log.info("🧹 양쪽 큰따옴표 제거 후 payload: {}", payload);
            }

            // 2) 기존 Base64 여부 판단 및 디코딩 로직
            String json;
            if (isBase64Encoded(payload)) {
                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(payload);
                    json = new String(decodedBytes, StandardCharsets.UTF_8);
                    log.info("📦 Base64 디코딩된 JSON: {}", json);
                } catch (IllegalArgumentException e) {
                    // Base64 디코딩 실패 시, 원본 메시지를 사용
                    log.warn("⚠️ Base64 디코딩 실패, 원본 JSON 사용");
                    json = payload;
                }
            } else {
                log.warn("⚠️ Base64 인코딩 아님 → 원문 JSON 사용");
                json = payload;
            }

            // 3) JSON 파싱 및 eventType 추출, 역직렬화
            JsonNode rootNode = objectMapper.readTree(json);
            if (!rootNode.has("eventType")) {
                throw new IllegalArgumentException("❌ 'eventType' 필드가 없습니다.");
            }
            String eventType = rootNode.get("eventType").asText();
            log.info("🧭 eventType = {}", eventType);

            switch (eventType) {
                case "BookSummaryGenerate":
                    return objectMapper.readValue(json, BookSummaryGenerate.class);
                case "CoverImageGenerated":
                    return objectMapper.readValue(json, CoverImageGenerated.class);
                case "SubscriptionFeeCalculated":
                    return objectMapper.readValue(json, SubscriptionFeeCalculated.class);
                case "Registered":
                    return objectMapper.readValue(json, Registered.class);
                default:
                    throw new IllegalArgumentException("❌ 알 수 없는 eventType: " + eventType);
            }

        } catch (Exception e) {
            log.error("❌ Kafka 메시지 역직렬화 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * ✅ Base64 인코딩 여부를 정확하게 판별
     */
    private static boolean isBase64Encoded(String input) {
        if (input == null || input.isEmpty()) return false;

        // Base64 문자열은 길이가 4의 배수여야 함
        if (input.length() % 4 != 0) return false;

        // Base64 허용 문자 검사
        if (!input.matches("^[A-Za-z0-9+/=\\r\\n]+$")) return false;

        try {
            Base64.getDecoder().decode(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
