package fun.amireux.chat.book.framework.parsing.utils;

import fun.amireux.chat.book.framework.parsing.pojo.DocumentInfo;

import java.io.IOException;
import java.io.InputStream;

public interface ContextParsingUtil {

    /**
     * 提取文件的文本内容
     */
    DocumentInfo extractDocumentInfo(InputStream inputStream, String fileName) throws IOException;


}
