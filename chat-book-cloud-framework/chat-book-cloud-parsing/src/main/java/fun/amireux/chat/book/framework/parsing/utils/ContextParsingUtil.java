package fun.amireux.chat.book.framework.parsing.utils;

import fun.amireux.chat.book.framework.parsing.pojo.DocumentInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ContextParsingUtil {

    /**
     * 提取文件的文本内容
     * @param inputStream 文件输入流
     * @param fileName 文件名
     */
    DocumentInfo extractDocumentInfo(InputStream inputStream, String fileName) throws IOException;

    /**
     * 文本切分
     * @param text 文本
     */
    List<String> textSlicing(String text);

    List<String> textSlicing(String text, int miniBlockSize);
}
