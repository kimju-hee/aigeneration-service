package miniprojectjo.infra;

import lombok.RequiredArgsConstructor;
import miniprojectjo.domain.PublishingRequested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/kafka-test")
@RequiredArgsConstructor
public class KafkaTestController {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/publish")
    public String publish(@RequestBody PublishingRequested event) {
        event.setEventType("PublishingRequested");  // ✅ Kafka 수신을 위한 헤더
        event.setCreatedAt(new Date());

        // ✅ [1] 요청 도달 확인 로그
        System.out.println("✅ KafkaTestController 진입 성공");
        System.out.println("📦 발행할 이벤트: " + event);

        // ✅ [2] Kafka로 이벤트 발행
        kafkaTemplate.send("miniprojectjo", event);

        return "Event published: " + event.getTitle();
    }
}
