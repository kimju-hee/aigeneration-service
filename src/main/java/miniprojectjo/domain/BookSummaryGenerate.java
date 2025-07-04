package miniprojectjo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import miniprojectjo.infra.AbstractEvent;

import java.util.Date;

@Getter
@Setter
@JsonSerialize
@JsonDeserialize
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
public class BookSummaryGenerate extends AbstractEvent {

    private Long id;
    private Long manuscriptId;
    private String summary;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Date createdAt;

    @JsonCreator
    public BookSummaryGenerate(
        @JsonProperty("id") Long id,
        @JsonProperty("manuscriptId") Long manuscriptId,
        @JsonProperty("summary") String summary,
        @JsonProperty("createdAt") Date createdAt
    ) {
        this.id = id;
        this.manuscriptId = manuscriptId;
        this.summary = summary;
        this.createdAt = createdAt;
        this.setEventType("BookSummaryGenerate");
    }

    public BookSummaryGenerate(AiBookGeneration aggregate) {
        super(aggregate);
        this.id = aggregate.getId();
        this.manuscriptId = aggregate.getManuscriptId();
        this.summary = aggregate.getSummary();
        this.createdAt = new Date();
        this.setEventType("BookSummaryGenerate");
    }
}
