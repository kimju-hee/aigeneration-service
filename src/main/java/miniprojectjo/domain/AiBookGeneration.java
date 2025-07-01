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

    // Repository 빈 주입받기 위한 static 메서드
    public static AiBookGenerationRepository repository() {
        return AigenerationApplication.applicationContext.getBean(AiBookGenerationRepository.class);
    }

    // AiImageService 빈 주입 메서드 (GPT API 호출 담당 서비스)
    private static AiImageService aiImageService() {
        return AigenerationApplication.applicationContext.getBean(AiImageService.class);
    }

    /**
     * 1. 도서 요약 생성 요청 처리
     */
    public static void generateBookSummary(PublishingRequested event) {
        AiBookGeneration entity = new AiBookGeneration();
        entity.setManuscriptId(event.getId());

        // GPT 요약 API 호출 (원고 내용은 event.getContent()에서 가져옴)
        String summary = aiImageService().generateSummary(event.getContent());
        entity.setSummary(summary);
        entity.setStatus("SUMMARY_CREATED");
        entity.setCreatedAt(new Date());
        entity.setUpdatedAt(new Date());


        repository().save(entity);

        // 도서 요약 생성 이벤트 발행
        BookSummaryGenerate published = new BookSummaryGenerate(entity);
        published.setCreatedAt(new Date());
        published.publishAfterCommit();
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

            if (published.getManuscriptId() == null || published.getCoverImageUrl() == null) {
                System.out.println("⚠️ CoverImageGenerated 이벤트 생성 실패 - 필드 누락");
                System.out.println("📭 현재 이벤트 상태: manuscriptId=" + published.getManuscriptId()
                    + ", coverImageUrl=" + published.getCoverImageUrl());
                return;
            }

            System.out.println("📦 발행될 CoverImageGenerated: manuscriptId=" + published.getManuscriptId()
                + ", coverImageUrl=" + published.getCoverImageUrl());
            published.logAsJson(); // 이벤트 직렬화 테스트 로그
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
            entity.setSubscriptionFee(3900); // 예시 구독료
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
