package miniprojectjo.infra;

import miniprojectjo.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/aiBookGenerations")
@Transactional
public class AiBookGenerationController {

    @Autowired
    AiBookGenerationRepository aiBookGenerationRepository;

    // 📘 요약 생성 요청
    @PostMapping("/{id}/generate-summary")
    public void generateSummary(@PathVariable Long id) {
        AiBookGeneration aggregate = aiBookGenerationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("해당 ID의 도서를 찾을 수 없습니다: " + id));

        System.out.println("🧠 요약 생성 API 호출됨");
        AiBookGeneration.generateBookSummary(new PublishingRequested(aggregate));
    }

    // 🖼️ 표지 이미지 생성 요청
    @PostMapping("/{id}/generate-cover")
    public void generateCover(@PathVariable Long id) {
        AiBookGeneration aggregate = aiBookGenerationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("해당 ID의 도서를 찾을 수 없습니다: " + id));

        System.out.println("🎨 표지 생성 API 호출됨");
        AiBookGeneration.generateCoverImage(new PublishingRequested(aggregate));
    }

    // 💰 구독료 책정 요청
    @PostMapping("/{id}/calculate-subscription-fee")
    public void calculateFee(@PathVariable Long id) {
        AiBookGeneration aggregate = aiBookGenerationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("해당 ID의 도서를 찾을 수 없습니다: " + id));

        System.out.println("💸 구독료 책정 API 호출됨");
        AiBookGeneration.subscriptionFeePolicy(new Registered(aggregate));
    }

    // 📚 최종 등록 요청 (선택)
    @PostMapping("/{id}/register")
    public void registerBook(@PathVariable Long id) {
        AiBookGeneration aggregate = aiBookGenerationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("해당 ID의 도서를 찾을 수 없습니다: " + id));

        System.out.println("📌 도서 등록 API 호출됨");
        AiBookGeneration.registerProcessedBook(new BookSummaryGenerate(aggregate));
    }

    // 📝 테스트용 Mok 데이터 생성 (원고 내용 포함)
    @PostMapping("/mock")
    public AiBookGeneration createMockData() {
        AiBookGeneration entity = new AiBookGeneration();
        entity.setManuscriptId(1L);
        entity.setStatus("REQUESTED");
        entity.setManuscriptContent("옛날 옛적에 백성공주가 살았는데, 그녀는 용감하고 지혜로웠습니다. 왕국에 큰 위기가 닥치자 그녀가 문제를 해결했습니다.");
        entity.setSummary(null);
        entity.setCoverImageUrl(null);
        entity.setSubscriptionFee(null);

        return aiBookGenerationRepository.save(entity);
    }

    // 🧪 전체 자동 흐름 테스트용 API (요약 → 이미지 → 가격 → 등록)
    @PostMapping("/{id}/mock-full-flow")
    public void mockFullFlow(@PathVariable Long id) {
        AiBookGeneration aggregate = aiBookGenerationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("해당 ID의 도서를 찾을 수 없습니다: " + id));

        PublishingRequested event = new PublishingRequested();
        event.setId(aggregate.getManuscriptId());
        event.setContent(aggregate.getManuscriptContent());

        // 자동 흐름 실행
        AiBookGeneration.generateBookSummary(event);
        AiBookGeneration.generateCoverImage(event);
        AiBookGeneration.registerProcessedBook(new BookSummaryGenerate(aggregate));
        AiBookGeneration.registerProcessedBook(new CoverImageGenerated(aggregate));
        AiBookGeneration.subscriptionFeePolicy(new Registered(aggregate));
    }
}
