package com.zf.partnerback.common;

/**
 * @ClassName: Result
 * @Description: TODO
 * @Author: ZF
 * @date: 2022/12/22 17:18
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接口统一返回包装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private static final String CODE_SUCCESS = "200";
    private static final String CODE_SYS_ERROR = "500";

    private String code;
    private String msg;
    private Object data;

    public static Result success() {
        return new Result(CODE_SUCCESS, "操作成功", null);
    }

    public static Result success(Object data) {
        return new Result(CODE_SUCCESS, "操作成功", data);
    }

    public static Result error(String code, String msg) {
        return new Result(code, msg, null);
    }

    public static Result error(String msg) {
        return new Result(CODE_SYS_ERROR, msg, null);
    }

    public static Result error() {
        return new Result(CODE_SYS_ERROR, "系统错误", null);
    }

}