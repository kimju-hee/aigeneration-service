package miniprojectjo.infra;

import lombok.RequiredArgsConstructor;
import miniprojectjo.domain.PublishingRequested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.Base64;

@RestController
@RequestMapping("/kafka-test")
@RequiredArgsConstructor
public class KafkaTestController {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/publish")
    public String publish(@RequestBody PublishingRequested event) {
        event.setEventType("PublishingRequested");  // ✅ eventType을 명시적으로 설정
        event.setCreatedAt(new Date());

        // ✅ [1] 요청 도달 확인 로그
        System.out.println("✅ KafkaTestController 진입 성공");
        System.out.println("📦 발행할 이벤트: " + event);

        try {
            // ✅ [2] 이벤트 객체를 JSON 문자열로 직렬화
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonEvent = objectMapper.writeValueAsString(event);  // 객체를 JSON 문자열로 변환

            // ✅ [3] JSON 문자열을 Base64로 인코딩
            String encodedEvent = Base64.getEncoder().encodeToString(jsonEvent.getBytes());

            // ✅ [4] Kafka로 Base64로 인코딩된 이벤트 발행
            kafkaTemplate.send("miniprojectjo", encodedEvent);
            System.out.println("📦 Base64 인코딩된 이벤트 발행 완료: " + encodedEvent);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error publishing event";
        }

        return "Event published: " + event.getTitle();
    }

}
