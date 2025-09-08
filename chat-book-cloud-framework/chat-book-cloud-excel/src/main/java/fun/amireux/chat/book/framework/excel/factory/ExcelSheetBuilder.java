package fun.amireux.chat.book.framework.excel.factory;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import lombok.Getter;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelSheetBuilder {
    private final InputStream inputStream;
    @Getter
    private List<String> sheets;

    public ExcelSheetBuilder(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void run() {
        ExcelReader excelReader = EasyExcel.read(inputStream).build();
        this.sheets = excelReader.excelExecutor().sheetList()
                .stream()
                .map(ReadSheet::getSheetName)
                .collect(Collectors.toList());
        excelReader.finish();
    }
}
