package fun.amireux.chat.book.framework.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import fun.amireux.chat.book.framework.excel.annotations.ExcelSheet;
import fun.amireux.chat.book.framework.excel.listener.AbstractListener;
import fun.amireux.chat.book.framework.excel.listener.GenericMergeStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class ExcelTemplate {

    private final ApplicationContext applicationContext;

    public ExcelTemplate(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 写 Excel
     */
    public <T> void write(HttpServletResponse response, String fileName, Class<T> clazz, List<T> data) throws IOException {
        ExcelSheet sheetConfig = clazz.getAnnotation(ExcelSheet.class);
        String sheetName = (sheetConfig != null ? sheetConfig.name() : "Sheet1");

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + encodedFileName + ".xlsx");

        ExcelWriterBuilder writerBuilder = EasyExcel.write(response.getOutputStream(), clazz);

        // ✅ 支持通用合并策略
        if (sheetConfig != null && sheetConfig.mergeHandler() == GenericMergeStrategy.class) {
            int[] mergeColumnIndexes = sheetConfig.mergeColumnIndexes();
            if (mergeColumnIndexes.length > 0 && sheetConfig.valueExtractor() != ExcelSheet.ValueExtractor.class) {
                try {
                    // 创建提取器实例
                    ExcelSheet.ValueExtractor<T> extractor = (ExcelSheet.ValueExtractor<T>) sheetConfig.valueExtractor().newInstance();
                    GenericMergeStrategy<T> mergeStrategy = new GenericMergeStrategy<>(data, mergeColumnIndexes, extractor::getValue);
                    writerBuilder.registerWriteHandler(mergeStrategy);
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("创建 ValueExtractor 实例失败", e);
                    throw new RuntimeException("无法创建合并处理器", e);
                }
            }
        }

        writerBuilder.sheet(sheetName).doWrite(data);
    }

    /**
     * 将数据写入 Excel 并保存到本地文件
     *
     * @param filePath 本地保存路径，例如："/tmp/export/data.xlsx"
     * @param fileName 文件名（不含路径）
     * @param clazz    数据类，包含 Excel 头信息注解
     * @param data     数据列表
     * @param <T>      数据类型
     * @throws IOException 写入失败时抛出
     */
    public <T> void write(String filePath, String fileName, Class<T> clazz, List<T> data) throws IOException {
        ExcelSheet sheetConfig = clazz.getAnnotation(ExcelSheet.class);
        String sheetName = (sheetConfig != null ? sheetConfig.name() : "Sheet1");

        // 构建完整文件路径
        File file = new File(filePath, fileName + ".xlsx");
        // 确保目录存在
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (OutputStream out = new FileOutputStream(file)) {
            ExcelWriterBuilder writerBuilder = EasyExcel.write(out, clazz);

            // ✅ 支持通用合并策略
            if (sheetConfig != null && sheetConfig.mergeHandler() == GenericMergeStrategy.class) {
                int[] mergeColumnIndexes = sheetConfig.mergeColumnIndexes();
                if (mergeColumnIndexes.length > 0 && sheetConfig.valueExtractor() != ExcelSheet.ValueExtractor.class) {
                    // 创建提取器实例
                    ExcelSheet.ValueExtractor<T> extractor = (ExcelSheet.ValueExtractor<T>) sheetConfig.valueExtractor().newInstance();
                    GenericMergeStrategy<T> mergeStrategy = new GenericMergeStrategy<>(data, mergeColumnIndexes, extractor::getValue);
                    writerBuilder.registerWriteHandler(mergeStrategy);
                }
            } else if (sheetConfig != null && sheetConfig.mergeHandler() != AbstractMergeStrategy.class) {
                AbstractMergeStrategy handler = applicationContext.getBean(sheetConfig.mergeHandler());
                writerBuilder.registerWriteHandler(handler);
            }

            // 写入数据
            writerBuilder.sheet(sheetName).doWrite(data);
        } catch (IOException e) {
            log.error("写入文件失败", e);
            throw new IOException("写入文件失败", e);
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("创建 ValueExtractor 实例失败", e);
            throw new RuntimeException("无法创建合并处理器", e);
        }
    }

    /**
     * 读 Excel
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> read(MultipartFile file, Class<T> clazz) throws IOException {
        ExcelSheet sheetConfig = clazz.getAnnotation(ExcelSheet.class);

        ExcelReaderBuilder readerBuilder = EasyExcel.read(file.getInputStream(), clazz, null);

        AbstractListener<T> listener = null;
        if (sheetConfig != null && sheetConfig.readListener() != AbstractListener.class) {
            listener = (AbstractListener<T>) applicationContext.getBean(sheetConfig.readListener());
            listener.reset();
            readerBuilder.registerReadListener(listener);
        }

        String sheetName = (sheetConfig != null ? sheetConfig.name() : null);
        int headRowNumber = (sheetConfig != null ? sheetConfig.headRowNumber() : 1);

        ExcelReaderSheetBuilder sheetBuilder = (sheetName != null ?
                readerBuilder.sheet(sheetName) : readerBuilder.sheet());
        sheetBuilder.headRowNumber(headRowNumber).doRead();

        return listener.getCachedDataList();
    }
}
