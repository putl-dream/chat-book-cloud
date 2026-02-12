package fun.amireux.chat.book.minio.utils;

import fun.amireux.chat.book.minio.pojo.FileInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface MinioUpdateUtil {

    /**
     * Upload file with specific file info and user id (legacy)
     */
    String uploadFile(FileInfo fileInfo, Object userId, String fileName);

    /**
     * Upload multipart file to a specific path
     * @param file The file to upload
     * @param objectName The full object path in the bucket (e.g. "images/123.jpg")
     * @return The object name if successful, null otherwise
     */
    String uploadFile(MultipartFile file, String objectName);

    String getFileUrl(String storagePath, int hours);
    
    /**
     * Get the public URL for the file (if configured)
     */
    String getPublicFileUrl(String storagePath);

    void downloadFile(String storagePath, HttpServletResponse response);

    String uploadChunkFile(FileInfo chunkInfo, int chunkIndex);

    String mergeChunkFiles(List<String> storagePaths, String fileName);

    default String getFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unknown";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "unknown";
        }

        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();

        // 根据文件扩展名返回文件类型
        return switch (extension) {
            case "pdf" -> "PDF文档";
            case "doc", "docx" -> "Word文档";
            case "xls", "xlsx" -> "Excel表格";
            case "ppt", "pptx" -> "PowerPoint演示文稿";
            case "txt" -> "文本文件";
            case "md" -> "Markdown文档";
            case "jpg", "jpeg" -> "JPEG图片";
            case "png" -> "PNG图片";
            case "gif" -> "GIF图片";
            case "bmp" -> "BMP图片";
            case "svg" -> "SVG图片";
            case "mp4" -> "MP4视频";
            case "avi" -> "AVI视频";
            case "mov" -> "MOV视频";
            case "wmv" -> "WMV视频";
            case "mp3" -> "MP3音频";
            case "wav" -> "WAV音频";
            case "flac" -> "FLAC音频";
            case "zip" -> "ZIP压缩包";
            case "rar" -> "RAR压缩包";
            case "7z" -> "7Z压缩包";
            case "tar" -> "TAR压缩包";
            case "gz" -> "GZ压缩包";
            case "json" -> "JSON文件";
            case "xml" -> "XML文件";
            case "csv" -> "CSV文件";
            case "html", "htm" -> "HTML文件";
            case "css" -> "CSS文件";
            case "js" -> "JavaScript文件";
            case "java" -> "Java源码";
            case "py" -> "Python源码";
            case "cpp", "c" -> "C/C++源码";
            case "sql" -> "SQL文件";
            default -> extension.toUpperCase() + "文件";
        };
    }
}
