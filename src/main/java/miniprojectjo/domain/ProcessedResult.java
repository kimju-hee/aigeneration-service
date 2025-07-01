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
    private Long manuscriptId;// manuscriptId를 PK로 활용 (1:1 매핑)

    private String summary;

    private String coverImageUrl;

    private Integer subscriptionFee;

    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
//>>> EDA / CQRS
