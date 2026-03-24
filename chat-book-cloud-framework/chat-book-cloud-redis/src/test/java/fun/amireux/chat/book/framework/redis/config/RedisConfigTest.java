package fun.amireux.chat.book.framework.redis.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    void shouldRestoreFinalPageObjectFromRedisSerializer() {
        GenericJackson2JsonRedisSerializer serializer = redisConfig.createRedisSerializer();
        CachedPage source = new CachedPage(
                List.of(new CachedArticle(1, "redis-type-safe")),
                1L,
                LocalDateTime.of(2026, 3, 21, 15, 27, 58)
        );

        byte[] bytes = serializer.serialize(source);
        Object restored = serializer.deserialize(bytes);

        assertThat(restored).isInstanceOf(CachedPage.class);

        CachedPage page = (CachedPage) restored;
        assertThat(page.getTotal()).isEqualTo(1L);
        assertThat(page.getCachedAt()).isEqualTo(source.getCachedAt());
        assertThat(page.getList()).hasSize(1);
        assertThat(page.getList().get(0)).isInstanceOf(CachedArticle.class);
        assertThat(page.getList().get(0).getId()).isEqualTo(1);
        assertThat(page.getList().get(0).getTitle()).isEqualTo("redis-type-safe");
    }

    static final class CachedPage {
        private List<CachedArticle> list;
        private Long total;
        private LocalDateTime cachedAt;

        CachedPage() {
        }

        CachedPage(List<CachedArticle> list, Long total, LocalDateTime cachedAt) {
            this.list = list;
            this.total = total;
            this.cachedAt = cachedAt;
        }

        public List<CachedArticle> getList() {
            return list;
        }

        public Long getTotal() {
            return total;
        }

        public LocalDateTime getCachedAt() {
            return cachedAt;
        }
    }

    static final class CachedArticle {
        private Integer id;
        private String title;

        CachedArticle() {
        }

        CachedArticle(Integer id, String title) {
            this.id = id;
            this.title = title;
        }

        public Integer getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }
}
