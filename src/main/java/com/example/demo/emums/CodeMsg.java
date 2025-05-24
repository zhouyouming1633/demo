package com.example.demo.emums;

import lombok.Data;

/***
 * @description 状态码枚举
 * @author ZhouYouMing
 * @date 2025/5/24 18:37
 */
public enum CodeMsg {

    //成功状态码
    SUCCESS("200","success"),

    //失败状态码
    FAIL("-99","fail"),

    //系统异常
    SYSTEM_ERROR("500","error"),

    //参数错误
    PARAM_ERROR("10000","param error"),

    //图书创建失败
    BOOK_CREATE_ERROR("10001","book create fail"),

    //图书更新失败
    BOOK_UPDATE_ERROR("10002","book update fail"),

    //图书删除
    BOOK_DEL_ERROR("10003","book del fail");

    CodeMsg(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    //状态码
    private String code;

    //错误信息
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
