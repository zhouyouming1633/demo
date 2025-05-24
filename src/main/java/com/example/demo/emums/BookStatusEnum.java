package com.example.demo.emums;

/***
 * @description
 * @author ZhouYouMing
 * @date 2025/5/24 18:36
 */
public enum BookStatusEnum {

    //状态：0-下架 1-在库 2-删除
    OFF_LINE(0,"下架"),
    ON_LINE(1,"在库"),
    DEL(2,"借出");

    BookStatusEnum(Integer status, String desc) {
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

    public static boolean checkStatus(Integer status){
        boolean flag = false;
        if(status!=null){
            for(BookStatusEnum bookStatusEnum : BookStatusEnum.values()){
                if(bookStatusEnum.getStatus().equals(status)){
                    flag = true;
                }
            }
        }
        return flag;
    }
}
