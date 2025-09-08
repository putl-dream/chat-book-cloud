package fun.amireux.chat.book.framework.excel.handler;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通用 Excel Listener，支持自动填充合并单元格数据
 *
 * @param <T> Excel 映射的实体类
 */
@Slf4j
public class DefaultListener<T> implements ReadListener<T> {

    /** 存放解析出来的数据 */
    @Getter
    private final List<T> dataList = new ArrayList<>();

    /** 上一行非空数据，用于填充合并单元格 */
    private final T lastNonEmptyData;

    /** 缓存需要处理的字段（带 @ExcelProperty 的字段） */
    private final List<Field> excelFields;

    /**
     * 构造方法，传入实体类型
     *
     * @param clazz 实体类
     */
    public DefaultListener(Class<T> clazz) {
        try {
            this.lastNonEmptyData = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("无法实例化 " + clazz.getName(), e);
        }
        this.excelFields = initExcelFields(clazz);
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        // 填充空字段（合并单元格情况）
        fillEmptyFields(data, lastNonEmptyData);

        // 更新上一行非空数据
        updateNonEmptyFields(data, lastNonEmptyData);

        // 存入缓存
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成！总共解析到 {} 条数据", dataList.size());
    }

    /**
     * 填充当前对象的空字段
     */
    private void fillEmptyFields(T current, T lastNonEmpty) {
        for (Field field : excelFields) {
            field.setAccessible(true);
            try {
                Object value = field.get(current);
                if (isEmpty(value)) {
                    field.set(current, field.get(lastNonEmpty));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("填充字段失败：" + field.getName(), e);
            }
        }
    }

    /**
     * 更新最后一条非空数据
     */
    private void updateNonEmptyFields(T current, T lastNonEmpty) {
        for (Field field : excelFields) {
            field.setAccessible(true);
            try {
                Object value = field.get(current);
                if (!isEmpty(value)) {
                    field.set(lastNonEmpty, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("更新字段失败：" + field.getName(), e);
            }
        }
    }

    /**
     * 初始化需要处理的 Excel 字段
     */
    private List<Field> initExcelFields(Class<T> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> tempClass = clazz;
        while (tempClass != null && tempClass != Object.class) {
            fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }
        fields.removeIf(f -> !f.isAnnotationPresent(ExcelProperty.class));
        return fields;
    }

    /**
     * 判断值是否为空
     */
    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        return false;
    }
}
