package fun.amireux.chat.book.framework.parsing.pojo;

import lombok.Data;

import java.util.Map;

/**
 * 表示从文档中提取出的信息
 */
@Data
public class DocumentInfo {
    private String content; // 文档的文本内容
    private Map<String, String> metadata; // 文档的元数据 (如作者、标题、创建日期等)
    private String mimeType; // 文件的 MIME 类型
}