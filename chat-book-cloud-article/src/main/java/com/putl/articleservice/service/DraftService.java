package com.putl.articleservice.service;

import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.api.dto.CreateDraftVersionRequest;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import com.putl.articleservice.api.dto.DraftVersionAdoptRequest;

public interface DraftService {

    CreateDraftResponse createDraft(CreateDraftRequest request);

    CreateDraftResponse createDraftVersion(CreateDraftVersionRequest request);

    DraftDetailDTO getDraftDetail(Integer draftId);

    void adoptVersion(DraftVersionAdoptRequest request);
}
