package com.bithu.zhongshihua.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    private Boolean success;

    private Object data;
}
