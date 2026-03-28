package com.putl.agentservice.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiInvocationResult<T> {

    private T data;

    private Integer tokenInput;

    private Integer tokenOutput;

    private Integer latencyMs;

    private String model;
}
