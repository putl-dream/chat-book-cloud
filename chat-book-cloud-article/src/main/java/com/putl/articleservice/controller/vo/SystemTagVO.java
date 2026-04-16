package com.putl.articleservice.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统标签")
public class SystemTagVO {

    private Integer id;

    private String name;

    private String code;

    private String dimension;

    private String description;

    private String status;

    private Integer sort;

    private BigDecimal recommendWeight;

    private Long articleCount;
}
