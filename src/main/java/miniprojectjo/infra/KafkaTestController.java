package miniprojectjo.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import miniprojectjo.domain.PublishingRequested;

@RestController
@RequestMapping("/kafka-test")
public class KafkaTestController {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @PostMapping("/publish")
    public String publishTestEvent(@RequestBody PublishingRequested event) {
        event.setEventType("PublishingRequested"); // 중요: 헤더 대신 객체 속성으로 처리
        kafkaTemplate.send("miniprojectjo", event);
        return "Sent PublishingRequested event: " + event.getId();
    }
}