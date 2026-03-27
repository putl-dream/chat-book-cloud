package com.putl.userservice.controller.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fun.amireux.chat.book.minio.jackson.FileUrlSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Integer id;
    private Integer userId;
    private String username;
    private String email;
    @JsonSerialize(using = FileUrlSerializer.class)
    private String photo;
    private String profile;
    private String role;
    @Schema(description = "账号状态：0-正常，1-禁用")
    private Integer status;
}
