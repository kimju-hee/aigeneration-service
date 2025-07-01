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
    @PostMapping("/mock")
    public AiBookGeneration createMockData() {
        AiBookGeneration entity = new AiBookGeneration();
        entity.setManuscriptId(123L);
        entity.setStatus("REQUESTED");
        entity.setSummary("이건 요약 테스트용입니다.");
        entity.setCoverImageUrl(null);
        entity.setSubscriptionFee(null);

        return aiBookGenerationRepository.save(entity);
    }
    
}
