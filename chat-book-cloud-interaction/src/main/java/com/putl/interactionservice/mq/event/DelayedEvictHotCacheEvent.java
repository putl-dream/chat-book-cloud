package com.putl.interactionservice.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelayedEvictHotCacheEvent {

    private String eventId;

    private String sourceEventId;

    private Integer articleId;
}
