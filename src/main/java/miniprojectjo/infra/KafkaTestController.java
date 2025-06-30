
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