package fun.amireux.chat.book.framework.common.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class BeanUtil {

    private static final Logger log = LoggerFactory.getLogger(BeanUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    }

    /**
     * 单个 Bean 转换
     */
    public static <T> T toBean(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        try {
            if (source instanceof String) {
                return objectMapper.readValue((String) source, clazz);
            }
            return objectMapper.convertValue(source, clazz);
        } catch (Exception e) {
            log.error("BeanUtil toBean error", e);
            throw new RuntimeException("BeanUtil toBean error", e);
        }
    }

    /**
     * List 集合转换
     */
    public static <T> List<T> toBean(List<?> sourceList, Class<T> clazz) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.convertValue(sourceList, listType);
        } catch (IllegalArgumentException e) {
            log.error("BeanUtil toBean List error", e);
            throw new RuntimeException("BeanUtil toBean List error", e);
        }
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("BeanUtil toJsonString error", e);
            throw new RuntimeException("BeanUtil toJsonString error", e);
        }
    }
}
