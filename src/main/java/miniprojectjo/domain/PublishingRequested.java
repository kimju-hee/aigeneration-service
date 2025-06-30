package miniprojectjo.domain;

import lombok.*;
import miniprojectjo.infra.AbstractEvent;
import java.util.Date;

//<<< DDD / Domain Event
@Data
@ToString
public class PublishingRequested extends AbstractEvent {

    private Long id;              // 이벤트 ID 또는 AI Book ID
    private String title;         // 도서 제목
    private Long authorId;        // 작성자 ID
    private String status;        // 요청 상태 (e.g., REQUESTED)
    private String content;       // 원고 내용
    private Date createdAt;       // 생성 시각 (선택)
    
    private String eventType;  // 헤더 역할 (type="PublishingRequested")

    public PublishingRequested() {
        super();
    }

    public PublishingRequested(Object aggregate) {
        super(aggregate);
    }
}
//>>> DDD / Domain Event
