package miniprojectjo.domain;

import java.util.Date;
import javax.persistence.*;

import lombok.Data;
import miniprojectjo.AigenerationApplication;
import miniprojectjo.infra.AiBookGenerationRepository;

@Entity
@Table(name = "AiBookGeneration_table")
@Data
public class AiBookGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long manuscriptId;

    @Lob
    private String summary;

    @Lob
    private String manuscriptContent;

    private String coverImageUrl;

    private Integer subscriptionFee;

    private String status;

    private Date createdAt;

    private Date updatedAt;

    // Repository 빈 주입
    public static AiBookGenerationRepository repository() {
        return AigenerationApplication.applicationContext.getBean(AiBookGenerationRepository.class);
    }

    private static AiImageService aiImageService() {
        return AigenerationApplication.applicationContext.getBean(AiImageService.class);
    }

    /**
     * 1. 도서 요약 생성 요청 처리 (기존 entity 수정!)
     */
    public static void generateBookSummary(PublishingRequested event) {
        repository().findByManuscriptId(event.getId()).ifPresent(entity -> {
            String summary = aiImageService().generateSummary(event.getContent());

            entity.setSummary(summary);
            entity.setStatus("SUMMARY_CREATED");
            entity.setUpdatedAt(new Date());
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(new Date());
            }

            repository().save(entity);

            BookSummaryGenerate published = new BookSummaryGenerate(entity);
            published.setCreatedAt(new Date());
            published.publishAfterCommit();
        });
    }

    /**
     * 2. 표지 이미지 생성 요청 처리
     */
    public static void generateCoverImage(PublishingRequested event) {
        repository().findByManuscriptId(event.getId()).ifPresent(entity -> {
            String imageUrl = aiImageService().generateCoverImage("책 제목: " + event.getTitle());

            if (imageUrl == null || imageUrl.isBlank()) {
                System.out.println("⚠️ 이미지 생성 실패 - 이미지 URL이 null입니다.");
                return;
            }

            entity.setCoverImageUrl(imageUrl);
            entity.setStatus("COVER_GENERATED");
            entity.setUpdatedAt(new Date());
            repository().save(entity);

            CoverImageGenerated published = new CoverImageGenerated(entity);
            published.publishAfterCommit();
        });
    }

    /**
     * 3. 요약 완료 후 등록 처리
     */
    public static void registerProcessedBook(BookSummaryGenerate event) {
        repository().findByManuscriptId(event.getManuscriptId()).ifPresent(entity -> {
            entity.setUpdatedAt(new Date());
            repository().save(entity);

            Registered published = new Registered(entity);
            published.setCreatedAt(new Date());
            published.publishAfterCommit();
        });
    }

    /**
     * 4. 표지 완료 후 등록 처리
     */
    public static void registerProcessedBook(CoverImageGenerated event) {
        repository().findByManuscriptId(event.getManuscriptId()).ifPresent(entity -> {
            entity.setUpdatedAt(new Date());
            repository().save(entity);

            Registered published = new Registered(entity);
            published.setCreatedAt(new Date());
            published.publishAfterCommit();
        });
    }

    /**
     * 5. 등록 완료 후 구독료 책정 정책
     */
    public static void subscriptionFeePolicy(Registered event) {
        repository().findByManuscriptId(event.getManuscriptId()).ifPresent(entity -> {
            entity.setSubscriptionFee(3900); // 예시
            entity.setStatus("PRICED");
            entity.setUpdatedAt(new Date());
            repository().save(entity);

            SubscriptionFeeCalculated published = new SubscriptionFeeCalculated(entity);
            published.setCalculatedAt(new Date());
            published.setCriteria("SUMMARY+IMAGE_LENGTH");
            published.publishAfterCommit();
        });
    }

    // 이벤트로부터 상태 변경 메서드 (optional)
    public void generateBookSummary(BookSummaryGenerate event) {
        this.summary = event.getSummary();
        this.status = "SUMMARY_CREATED";
        this.updatedAt = new Date();
    }

    public void registerProcessedBook(Registered event) {
        this.status = event.getStatus();
        this.updatedAt = new Date();
    }
}
