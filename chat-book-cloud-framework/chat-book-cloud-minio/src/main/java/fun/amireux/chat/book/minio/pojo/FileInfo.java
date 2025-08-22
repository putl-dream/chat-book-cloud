package fun.amireux.chat.book.minio.pojo;

import org.springframework.web.multipart.MultipartFile;

public record FileInfo(String fileMd5, MultipartFile file) {
}

