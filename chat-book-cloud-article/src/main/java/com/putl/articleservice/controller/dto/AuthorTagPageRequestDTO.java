package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "作者标签后台分页请求")
public class AuthorTagPageRequestDTO extends PageRequestDTO {

    private String keyword;
}
