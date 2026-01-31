package fun.amireux.chat.book.framework.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;

import java.util.Collections;
import java.util.List;

public class BeanUtil {

    /**
     * 单个 Bean 转换
     */
    public static <T> T toBean(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        // 2.0.59 推荐写法：直接使用 copyTo
        return JSON.copyTo(source, clazz, JSONWriter.Feature.FieldBased);
    }

    /**
     * List 集合转换
     */
    public static <T> List<T> toBean(List<?> sourceList, Class<T> clazz) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }

        // 方案一：使用 JSONArray.from 转换（2.0.59 稳定版写法）
        return JSONArray.from(sourceList).toJavaList(clazz);

        // 方案二（备选）：如果 sourceList 本身很大，
        // 也可以使用 JSON.copyTo(sourceList, new TypeReference<List<T>>(clazz){}.getType())
    }
}