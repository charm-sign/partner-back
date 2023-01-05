package com.zf.partnerback.common.enums;

import lombok.Getter;

/**
 * 定义枚举，防止前端乱发type,规范输入
 */
@Getter
public enum EmailCodeEnum {
    REGISTER("REGISTER", "register:"),//注册
    RESET_PASSWORD("RESETPASSWORD", "resetPassword:"),//重置密码
    LOGIN("LOGIN", "login:"),//登录
    CHANGE_PASSWORD("CHANGEPASSWORD", "changePassword:"),//修改密码
    UNKNOWN("", "");

    private final String type;
    private final String value;

    EmailCodeEnum(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public static String getValue(String type) {
        EmailCodeEnum[] values = values();
        for (EmailCodeEnum codeEnum : values) {
            if (type.equals(codeEnum.type)) {
                return codeEnum.value;
            }
        }
        return "";
    }

    public static EmailCodeEnum getEnum(String type) {
        EmailCodeEnum[] values = values();
        for (EmailCodeEnum codeEnum : values) {
            if (type.equals(codeEnum.type)) {
                return codeEnum;
            }
        }
        return UNKNOWN;
    }
}
