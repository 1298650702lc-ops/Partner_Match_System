package com.jsoft.Common;
/**
 * 队伍状态枚举类
 * @Author F4EN
 */
public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private Integer value;
    private String text;
    public static TeamStatusEnum getEnumById(Integer value) {
        if(value == null) {
            return null;
        }
        TeamStatusEnum[] enumAry = TeamStatusEnum.values();
        for (TeamStatusEnum anEnumAry : enumAry) {
            if (anEnumAry.value.equals(value)) {
                return anEnumAry;
            }
        }
        return null;
    }
    TeamStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }
    public int getValue() {
        return value;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
