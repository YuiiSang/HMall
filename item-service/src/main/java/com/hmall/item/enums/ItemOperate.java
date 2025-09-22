package com.hmall.item.enums;

import lombok.Getter;

@Getter
public enum ItemOperate {
    ADD(0, "新增商品"),
    UPDATE(1, "更新商品"),
    DELETE(2, "删除商品");
    private final int value;
    private final String desc;
    ItemOperate(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
