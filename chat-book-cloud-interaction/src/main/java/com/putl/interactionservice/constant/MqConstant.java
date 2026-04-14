package com.putl.interactionservice.constant;

public final class MqConstant {

    private MqConstant() {
    }

    public static final String HOT_EXCHANGE = "interaction.hot.exchange";

    public static final String HOT_SCORE_CHANGED_QUEUE = "interaction.hot.score.changed.queue";
    public static final String HOT_SCORE_CHANGED_ROUTING_KEY = "interaction.hot.score.changed";

    public static final String HOT_CACHE_EVICT_DELAY_QUEUE = "interaction.hot.cache.evict.delay.queue";
    public static final String HOT_CACHE_EVICT_DELAY_ROUTING_KEY = "interaction.hot.cache.evict.delay";

    public static final String HOT_CACHE_EVICT_EXECUTE_QUEUE = "interaction.hot.cache.evict.execute.queue";
    public static final String HOT_CACHE_EVICT_EXECUTE_ROUTING_KEY = "interaction.hot.cache.evict.execute";
}
