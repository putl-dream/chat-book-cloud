package fun.amireux.chat.book.framework.excel.factory;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.ReadListener;
import fun.amireux.chat.book.framework.excel.annotations.ExcelRead;
import fun.amireux.chat.book.framework.excel.handler.DefaultListener;

import java.io.InputStream;
import java.util.List;

public class ExcelReaderBuilder<T>  {
    private final InputStream inputStream;
    private final Class<T> clazz;
    private String sheetName;
    private int headRowNumber = 1;
    private ReadListener<T> readListener;

    public ExcelReaderBuilder(InputStream inputStream, Class<T> clazz) {
        this.inputStream = inputStream;
        this.clazz = clazz;
        ExcelRead annotation = clazz.getAnnotation(ExcelRead.class);
        if (annotation != null) {
            this.sheetName = annotation.sheetName();
            this.headRowNumber = annotation.headRowNumber();
            this.readListener = new DefaultListener<>(clazz);
        }
    }

    public ExcelReaderBuilder<T> sheet(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public ExcelReaderBuilder<T> headRowNumber(int headRowNumber) {
        this.headRowNumber = headRowNumber;
        return this;
    }

    public List<T> runList() {
        run(readListener);
        return ((DefaultListener<T>) readListener).getDataList();
    }

    public void run(ReadListener<T> readListener) {
        this.readListener = readListener;
        EasyExcel.read(inputStream, clazz, readListener).sheet(sheetName).headRowNumber(headRowNumber).doRead();
    }
}
