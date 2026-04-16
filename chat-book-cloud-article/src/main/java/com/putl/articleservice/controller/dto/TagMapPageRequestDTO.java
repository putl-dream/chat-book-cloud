package com.putl.articleservice.controller.dto;

import lombok.Data;

@Data
public class TagMapPageRequestDTO extends PageRequestDTO {

    private String keyword;

    private Boolean mappedOnly;
}
