package fun.amireux.chat.book.framework.excel;

import fun.amireux.chat.book.framework.excel.factory.ExcelReaderBuilder;
import fun.amireux.chat.book.framework.excel.factory.ExcelSheetBuilder;
import fun.amireux.chat.book.framework.excel.factory.ExcelWriterBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ExcelTemplate {
    public static <T> ExcelReaderBuilder<T> read(InputStream inputStream, Class<T> clazz) {
        return new ExcelReaderBuilder<>(inputStream, clazz);
    }

    public static List<String> getSheets(InputStream inputStream) {
        ExcelSheetBuilder excelSheetBuilder = new ExcelSheetBuilder(inputStream);
        excelSheetBuilder.run();
        return excelSheetBuilder.getSheets();
    }

    public static <T> ExcelWriterBuilder<T> write(OutputStream outputStream, Class<T> clazz) {
        return new ExcelWriterBuilder<>(outputStream, clazz);
    }
}
