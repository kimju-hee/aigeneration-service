package miniprojectjo.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

//<<< EDA / CQRS
@Entity
@Table(name = "ProcessedResult_table")
@Data
public class ProcessedResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(unique = true)
    private Long manuscriptId; // manuscriptId를 PK처럼 사용 (1:1 매핑)

    @Lob
    private String summary;

    @Lob // 📌 URL이 255자를 넘을 수 있으므로 @Lob 추가
    private String coverImageUrl;

    private Integer subscriptionFee;

    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
//>>> EDA / CQRS
