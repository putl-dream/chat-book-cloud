package com.putl.articleservice.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagRelationCountDTO {
    private Integer tagId;
    private Long articleCount;
}
