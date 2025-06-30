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
        event.setEventType("PublishingRequested");  // 중요: 이벤트 타입
        event.setCreatedAt(new Date());
        kafkaTemplate.send("miniprojectjo", event);
        return "Event published: " + event.getTitle();
    }
}
