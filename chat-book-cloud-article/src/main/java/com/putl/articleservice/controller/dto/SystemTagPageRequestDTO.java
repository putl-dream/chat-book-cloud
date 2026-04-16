package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统标签分页请求")
public class SystemTagPageRequestDTO extends PageRequestDTO {

    private String keyword;

    private String dimension;

    private String status;
}
