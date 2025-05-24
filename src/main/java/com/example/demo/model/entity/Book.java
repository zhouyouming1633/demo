package com.example.demo.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/***
 * @description book 实体类
 * @author ZhouYouMing
 * @date 2025/5/24 18:41
 */
@Data
@TableName("book")
public class Book {

    /**
     * 主键Id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图书ISBN编码
     */
    private String isbn;

    /**
     * 图书名称
     */
    private String name;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版商
     */
    private String publisher;

    /**
     * 状态：0-下架 1-在库 2-借出 3-删除
     */
    private Integer status; // 使用枚举类型

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
