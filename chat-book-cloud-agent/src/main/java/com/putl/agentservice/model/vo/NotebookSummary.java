package com.putl.agentservice.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotebookSummary {

    private String goal;

    @Builder.Default
    private List<String> facts = new ArrayList<>();

    @Builder.Default
    private List<String> openQuestions = new ArrayList<>();

    @Builder.Default
    private List<String> outline = new ArrayList<>();

    @Builder.Default
    private List<String> styleRules = new ArrayList<>();

    private String summary;
}
