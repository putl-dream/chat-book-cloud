package fun.amireux.chat.book.framework.parsing.utils.impl;

import fun.amireux.chat.book.framework.parsing.pojo.DocumentInfo;
import fun.amireux.chat.book.framework.parsing.utils.ContextParsingUtil;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class ContextPassingUtilImpl implements ContextParsingUtil {

    /**
     * 使用 Apache Tika 从输入流中提取文档信息
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名 (用于 Tika 推断类型)
     * @return 包含内容、元数据和 MIME 类型的 DocumentInfo 对象
     * @throws IOException 如果解析过程中发生错误
     */
    public DocumentInfo extractDocumentInfo(InputStream inputStream, String fileName) throws IOException {
        DocumentInfo documentInfo = new DocumentInfo();

        try {
            // 创建处理程序来捕获内容
            ContentHandler handler = new BodyContentHandler();
            // 元数据对象
            Metadata metadata = new Metadata();
            // 设置文件名，帮助 Tika 更好地检测类型
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);

            // 使用 AutoDetectParser 自动选择合适的解析器
            Parser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);

            // 执行解析
            parser.parse(inputStream, handler, metadata, context);

            // 设置提取到的信息
            documentInfo.setContent(handler.toString().trim());
            documentInfo.setMimeType(metadata.get(Metadata.CONTENT_TYPE));

            // 将 Metadata 转换为 Map<String, String> 以便于 JSON 序列化
            Map<String, String> metadataMap = new HashMap<>();
            for (String name : metadata.names()) {
                metadataMap.put(name, metadata.get(name));
            }
            documentInfo.setMetadata(metadataMap);

        } catch (Exception e) {
            throw new IOException("解析文档时出错: " + e.getMessage(), e);
        }

        return documentInfo;
    }
}
