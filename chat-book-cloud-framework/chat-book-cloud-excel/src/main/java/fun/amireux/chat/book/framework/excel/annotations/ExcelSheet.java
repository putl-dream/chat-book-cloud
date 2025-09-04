package fun.amireux.chat.book.framework.excel.annotations;

import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import fun.amireux.chat.book.framework.excel.listener.AbstractListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSheet {
    /**
     * sheet 名称
     */
    String name();

    /**
     * 标题行数
     */
    int headRowNumber() default 1;

    /**
     * 指定解析器
     */
    Class<? extends AbstractListener> readListener() default AbstractListener.class;

    /**
     * 指定合并处理器
     */
    Class<? extends AbstractMergeStrategy> mergeHandler() default AbstractMergeStrategy.class;

    /**
     * 合并列索引
     */
    int[] mergeColumnIndexes() default {};

    // 新增：指定值提取器（函数式接口实现类）
    Class<? extends ValueExtractor> valueExtractor() default ValueExtractor.class;

    // 函数式接口
    @FunctionalInterface
    interface ValueExtractor<T> {
        String getValue(T item, int columnIndex);
    }
}
