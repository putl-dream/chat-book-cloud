package fun.amireux.chat.book.framework.excel.factory;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.WriteHandler;
import fun.amireux.chat.book.framework.excel.annotations.ExcelWrite;
import fun.amireux.chat.book.framework.excel.handler.DefaultCellStyleHandler;
import fun.amireux.chat.book.framework.excel.handler.DefaultMergeStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;

@Slf4j
public class ExcelWriterBuilder<T> {
    private final OutputStream outputStream;
    private final Class<T> clazz;
    private String sheetName = "Sheet1";
    private List<T> data;
    private WriteHandler writeHandler;
    private int headRowNumber;
    private int[] mergeColumnIndexes;
    private CellWriteHandler cellWriteHandler;


    // 修改构造函数，添加调试信息
    public ExcelWriterBuilder(OutputStream outputStream, Class<T> clazz) {
        this.outputStream = outputStream;
        this.clazz = clazz;
        this.cellWriteHandler = new DefaultCellStyleHandler();
        ExcelWrite excelWrite = clazz.getAnnotation(ExcelWrite.class);
        if (excelWrite != null) {
            this.sheetName = excelWrite.sheetName();
            this.headRowNumber = excelWrite.headRowNumber();
            this.mergeColumnIndexes = excelWrite.mergeColumnIndexes();
        }
    }

    public ExcelWriterBuilder<T> sheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public ExcelWriterBuilder<T> data(List<T> data) {
        this.data = data;

        // 自动生成 valueExtractor
        BiFunction<T, Integer, String> valueExtractor = createValueExtractor(clazz);
        this.writeHandler = new DefaultMergeStrategy<>(
                data,
                mergeColumnIndexes,
                valueExtractor,
                headRowNumber
        );
        return this;
    }

    private BiFunction<T, Integer, String> createValueExtractor(Class<T> clazz) {
        // 获取所有字段（按声明顺序）
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
        }

        return (item, colIndex) -> {
            if (colIndex < 0 || colIndex >= fields.length) return null;
            try {
                Object value = fields[colIndex].get(item);
                return value != null ? value.toString() : null;
            } catch (IllegalAccessException e) {
                // 忽略字段访问异常
                return null;
            }
        };
    }


    public ExcelWriterBuilder<T> writeHandler(WriteHandler writeHandler) {
        this.writeHandler = writeHandler;
        return this;
    }

    public void run() {
        EasyExcel.write(outputStream, clazz)
                .registerWriteHandler(writeHandler)
                .registerWriteHandler(cellWriteHandler)
                .sheet(sheetName)
                .doWrite(data);
    }
}
