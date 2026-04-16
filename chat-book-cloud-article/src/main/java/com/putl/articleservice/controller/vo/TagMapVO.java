package com.putl.articleservice.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagMapVO {

    private Integer authorTagId;

    private String authorTagName;

    private Long articleCount;

    private List<SystemTagVO> systemTags;
}
