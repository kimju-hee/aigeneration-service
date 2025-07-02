package miniprojectjo.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class AiImageService {

    @Value("${ai.openai.api-key}")
    private String openAiApiKey;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    // ✅ GPT 기반 요약 생성
    public String generateSummary(String manuscriptContent) {
        String prompt = "다음 내용을 3문장 이내로 간결하고 이해하기 쉽게 요약해줘:\n\n" + manuscriptContent;

        try {
            Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                    Map.of("role", "system", "content", "당신은 창의적이면서도 간결한 요약을 제공하는 요약 전문가입니다."),
                    Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
            );

            Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null || !response.containsKey("choices")) {
                return "⚠️ 응답 없음";
            }

            Map<String, Object> choice = (Map<String, Object>) ((List<?>) response.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            return message.get("content").toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ 요약 생성 실패";
        }
    }

    // ✅ DALL·E 직접 호출
    public String generateCoverImage(String prompt) {
        try {
            Map<String, Object> response = webClient.post()
                .uri("/images/generations")
                .bodyValue(Map.of(
                    "model", "dall-e-3",
                    "prompt", prompt,
                    "n", 1,
                    "size", "1024x1024"
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            return ((Map<String, String>) ((List<?>) response.get("data")).get(0)).get("url");

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ 이미지 생성 실패";
        }
    }

    // ✅ 요약 기반 표지 이미지 자동 생성
    public String generateCoverImageFromSummary(String summary) {
        String imagePrompt = String.format(
            "다음 이야기를 표현한 고품질 일러스트 북커버를 만들어줘. 분위기와 감정을 시각적으로 반영하되, 핵심 장면이나 상징을 포착해줘. 내용: \"%s\"",
            summary
        );
        return generateCoverImage(imagePrompt);
    }
}
