package fun.amireux.chat.book.framework.excel.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelRead {
    /**
     * sheet 名称
     */
    String sheetName();
    /**
     * 标题行数
     */
    int headRowNumber() default 1;
}
