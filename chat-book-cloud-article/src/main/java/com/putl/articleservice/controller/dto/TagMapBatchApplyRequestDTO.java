package com.putl.articleservice.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class TagMapBatchApplyRequestDTO {

    private List<Integer> authorTagIds;
}
