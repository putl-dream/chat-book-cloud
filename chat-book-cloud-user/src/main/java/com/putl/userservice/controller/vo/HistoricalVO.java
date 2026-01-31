package com.putl.userservice.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "HistoricalVO")
public class HistoricalVO {
    @Schema(description = "id")
    private Integer id;

}
