package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.api.dto.CreateDraftVersionRequest;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import com.putl.articleservice.api.dto.DraftVersionAdoptRequest;
import com.putl.articleservice.mapper.ArticleDraftMapper;
import com.putl.articleservice.mapper.ArticleDraftVersionMapper;
import com.putl.articleservice.mapper.entity.ArticleDraftDO;
import com.putl.articleservice.mapper.entity.ArticleDraftVersionDO;
import com.putl.articleservice.service.DraftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DraftServiceImpl implements DraftService {

    private final ArticleDraftMapper articleDraftMapper;
    private final ArticleDraftVersionMapper articleDraftVersionMapper;

    public DraftServiceImpl(ArticleDraftMapper articleDraftMapper,
                            ArticleDraftVersionMapper articleDraftVersionMapper) {
        this.articleDraftMapper = articleDraftMapper;
        this.articleDraftVersionMapper = articleDraftVersionMapper;
    }

    @Override
    @Transactional
    public CreateDraftResponse createDraft(CreateDraftRequest request) {
        ArticleDraftDO draft = ArticleDraftDO.builder()
                .userId(request.getUserId())
                .sourceSessionId(request.getSourceSessionId())
                .title(request.getTitle())
                .summary(request.getSummary())
                .content(request.getContent())
                .currentVersionNo(1)
                .status("DRAFT")
                .build();
        articleDraftMapper.insert(draft);

        articleDraftVersionMapper.insert(ArticleDraftVersionDO.builder()
                .draftId(draft.getId())
                .versionNo(1)
                .sourceType(request.getSourceType())
                .instruction(request.getInstruction())
                .title(request.getTitle())
                .summary(request.getSummary())
                .content(request.getContent())
                .adopted(1)
                .build());

        return CreateDraftResponse.builder()
                .draftId(draft.getId())
                .versionNo(1)
                .build();
    }

    @Override
    @Transactional
    public CreateDraftResponse createDraftVersion(CreateDraftVersionRequest request) {
        ArticleDraftDO draft = articleDraftMapper.selectById(request.getDraftId());
        Integer nextVersionNo = draft.getCurrentVersionNo() + 1;

        articleDraftVersionMapper.insert(ArticleDraftVersionDO.builder()
                .draftId(request.getDraftId())
                .versionNo(nextVersionNo)
                .sourceType(request.getSourceType())
                .instruction(request.getInstruction())
                .title(request.getTitle())
                .summary(request.getSummary())
                .content(request.getContent())
                .adopted(0)
                .build());

        return CreateDraftResponse.builder()
                .draftId(request.getDraftId())
                .versionNo(nextVersionNo)
                .build();
    }

    @Override
    public DraftDetailDTO getDraftDetail(Integer draftId) {
        ArticleDraftDO draft = articleDraftMapper.selectById(draftId);
        return DraftDetailDTO.builder()
                .draftId(draft.getId())
                .userId(draft.getUserId())
                .sourceSessionId(draft.getSourceSessionId())
                .title(draft.getTitle())
                .summary(draft.getSummary())
                .content(draft.getContent())
                .currentVersionNo(draft.getCurrentVersionNo())
                .status(draft.getStatus())
                .build();
    }

    @Override
    @Transactional
    public void adoptVersion(DraftVersionAdoptRequest request) {
        articleDraftVersionMapper.update(ArticleDraftVersionDO.builder()
                        .adopted(0)
                        .build(),
                Wrappers.<ArticleDraftVersionDO>lambdaUpdate()
                        .eq(ArticleDraftVersionDO::getDraftId, request.getDraftId()));

        ArticleDraftVersionDO version = articleDraftVersionMapper.selectOne(Wrappers.<ArticleDraftVersionDO>lambdaQuery()
                .eq(ArticleDraftVersionDO::getDraftId, request.getDraftId())
                .eq(ArticleDraftVersionDO::getVersionNo, request.getVersionNo())
                .last("limit 1"));

        articleDraftVersionMapper.updateById(ArticleDraftVersionDO.builder()
                .id(version.getId())
                .adopted(1)
                .build());

        articleDraftMapper.updateById(ArticleDraftDO.builder()
                .id(request.getDraftId())
                .title(version.getTitle())
                .summary(version.getSummary())
                .content(version.getContent())
                .currentVersionNo(version.getVersionNo())
                .build());
    }
}
