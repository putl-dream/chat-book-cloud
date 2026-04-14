package com.putl.interactionservice.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotScoreChangedEvent {

    private String eventId;

    private Integer articleId;

    private Double delta;

    private String actionType;
}
