package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.Book;

/***
 * @description book dao操作类
 * @author ZhouYouMing
 * @date 2025/5/24 18:38
 */
public interface BookMapper extends BaseMapper<Book> {

    /***
     * @description 根据isbn号更新书籍状态
     * @author ZhouYouMing
     * @date 2025/5/24 18:39
     * @param book
     * @return Integer
     */
    Integer updateBookStatus(Book book);

    /***
     * @description 根据isbn号更新书籍作者、名称、出版商等信息
     * @author ZhouYouMing
     * @date 2025/5/24 18:39
     * @param book
     * @return Integer
     */
    Integer updateBookInfo(Book book);
}
