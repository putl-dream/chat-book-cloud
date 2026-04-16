package com.putl.articleservice.controller.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SystemTagSaveRequestDTO {

    private Integer id;

    private String name;

    private String code;

    private String dimension;

    private String description;

    private String status;

    private Integer sort;

    private BigDecimal recommendWeight;
}
