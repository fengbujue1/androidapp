package com.bithu.zhongshihua.dto.chat;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChatExtends {
    private String wikiPromptTpl;

    private BigDecimal wikiFilterScore = new BigDecimal(65);

    private BigDecimal temperature = new BigDecimal("0.5");

}
