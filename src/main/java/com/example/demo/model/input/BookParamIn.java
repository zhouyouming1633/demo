package com.example.demo.model.input;

import lombok.Data;

/***
 * @description 图书服务查询参数对象
 * @author ZhouYouMing
 * @date 2025/5/24 18:41
 */
@Data
public class BookParamIn {

    //作者
    private String author;

    //当前页
    private Integer pageNo = 1;

    //每页数量
    private Integer pageSize = 10;

    public Integer getPageSize() {
        if(pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        return pageSize;
    }

    public Integer getPageNo() {
        if(pageNo == null || pageNo <= 0) {
            pageNo = 1;
        }
        return pageNo;
    }
}
