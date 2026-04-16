package com.putl.articleservice.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class TagMapUpdateRequestDTO {

    private Integer authorTagId;

    private List<Integer> systemTagIds;
}
