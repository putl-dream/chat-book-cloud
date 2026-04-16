package com.putl.articleservice.service;

import com.putl.articleservice.controller.vo.TagMapVO;
import com.putl.articleservice.utils.PageResult;

import java.util.List;

public interface TagMapService {

    PageResult<TagMapVO> getPage(Integer pageNo, Integer pageSize, String keyword, Boolean mappedOnly);

    void updateMappings(Integer authorTagId, List<Integer> systemTagIds);

    void batchApply(List<Integer> authorTagIds);
}
