package com.putl.articleservice.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorTagHotStatDTO {
    private Integer id;
    private String name;
    private Long articleCount;
}
