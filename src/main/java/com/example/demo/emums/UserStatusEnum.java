package com.example.demo.emums;

/***
 * @description 用户账户类状态枚举
 * @author ZhouYouMing
 * @date 2025/5/24 18:38
 */
public enum UserStatusEnum {

    //状态：0-无效 1-有效
    EFFECTIVE(1,"有效"),
    INVALID(0,"无效");

    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    //状态码
    private Integer status;

    //错误信息
    private String desc;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
