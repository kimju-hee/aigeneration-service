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

    @Lob
    private String coverImageUrl;

    private Integer subscriptionFee;

    private String status;

    private Date createdAt;

    private Date updatedAt;

    public static AiBookGenerationRepository repository() {
        return AigenerationApplication.applicationContext.getBean(AiBookGenerationRepository.class);
    }

    private static AiImageService aiImageService() {
        return AigenerationApplication.applicationContext.getBean(AiImageService.class);
    }

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

    public static void generateCoverImage(PublishingRequested event) {
        repository().findByManuscriptId(event.getId()).ifPresent(entity -> {
            String summary = entity.getSummary();
            if (summary == null || summary.isBlank()) {
                System.out.println("⚠️ 요약 정보 없음 - 이미지 생성 생략");
                return;
            }

            String imageUrl = aiImageService().generateCoverImageFromSummary(summary);

            if (imageUrl == null || imageUrl.isBlank()) {
                System.out.println("⚠️ 이미지 생성 실패 - URL이 비어 있음");
                return;
            }

            entity.setCoverImageUrl(imageUrl);
            entity.setStatus("COVER_GENERATED");
            entity.setUpdatedAt(new Date());
            repository().save(entity);

            CoverImageGenerated published = new CoverImageGenerated(entity);
            published.setCreatedAt(new Date());
            published.publishAfterCommit();
        });
    }

    public static void registerProcessedBook(BookSummaryGenerate event) {
        repository().findByManuscriptId(event.getManuscriptId()).ifPresent(entity -> {
            entity.setUpdatedAt(new Date());
            repository().save(entity);

            Registered published = new Registered(entity);
            published.setCreatedAt(new Date());
            published.publishAfterCommit();
        });
    }

    public static void registerProcessedBook(CoverImageGenerated event) {
        repository().findByManuscriptId(event.getManuscriptId()).ifPresent(entity -> {
            entity.setUpdatedAt(new Date());
            repository().save(entity);

            Registered published = new Registered(entity);
            published.setCreatedAt(new Date());
            published.publishAfterCommit();
        });
    }

    public static void subscriptionFeePolicy(Registered event) {
        repository().findByManuscriptId(event.getManuscriptId()).ifPresent(entity -> {
            String summary = entity.getSummary();
            String imageUrl = entity.getCoverImageUrl();

            if (summary == null || imageUrl == null) {
                System.out.println("⚠️ 요약 또는 표지 이미지가 없어 구독료를 책정할 수 없습니다.");
                return;
            }

            String prompt = String.format(
                "다음 도서 요약과 표지 이미지를 기반으로 적절한 구독료(₩1000 ~ ₩10000)를 숫자 하나로만 제시해줘.\n요약: %s\n표지 이미지 URL: %s",
                summary, imageUrl
            );

            String feeResponse = aiImageService().generateSummary(prompt);
            int fee;
            try {
                fee = Integer.parseInt(feeResponse.replaceAll("[^\\d]", "").trim());
            } catch (Exception e) {
                fee = 3900;
            }

            entity.setSubscriptionFee(fee);
            entity.setStatus("PRICED");
            entity.setUpdatedAt(new Date());
            repository().save(entity);

            SubscriptionFeeCalculated published = new SubscriptionFeeCalculated(entity);
            published.setCalculatedAt(new Date());
            published.setCriteria("SUMMARY+IMAGE_AI");
            published.setSubscriptionFee(fee);
            published.publishAfterCommit();
        });
    }

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
