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

    /**
     * Backward compatible with already queued messages that do not carry this field.
     */
    private Integer attempt;
}
