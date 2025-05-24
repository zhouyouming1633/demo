package com.example.demo.service.book.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.emums.CodeMsg;
import com.example.demo.mapper.BookMapper;
import com.example.demo.model.entity.Book;
import com.example.demo.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/***
 * @description book基本crud操作抽象类
 * @author ZhouYouMing
 * @date 2025/5/24 18:30
 */
public abstract class BookDeal {

    @Autowired
    public BookMapper bookMapper;

    /***
     * @description 创建图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:31
     * @param book
     * @return Response
     */
    public final Response create(Book book){
        if(checkBookExists(book.getIsbn())){
            return Response.error(CodeMsg.BOOK_CREATE_ERROR);
        }else{
            return createBook(book);
        }
    }

    /***
     * @description 修改图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:32
     * @param book
     * @return Response
     */
    public final Response update(Book book){
        if(checkBookExists(book.getIsbn())){
            return updateBook(book);
        }else{
            return Response.error(CodeMsg.BOOK_UPDATE_ERROR);
        }
    }

    /***
     * @description 删除图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:32
     * @param book
     * @return Response
     */
    public final Response del(Book book){
        if(checkBookExists(book.getIsbn())){
            return delBook(book);
        }else{
            return Response.error(CodeMsg.BOOK_DEL_ERROR);
        }
    }

    /***
     * @description 校验图书是否存在，通用的逻辑，CRUD都必须做这个校验
     * @author ZhouYouMing
     * @date 2025/5/24 18:32
     * @param isbn
     * @return boolean
     */
    private boolean checkBookExists(String isbn){
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isbn", isbn);
        Book book = bookMapper.selectOne(queryWrapper);
        if(book == null){
            //不存在
            return false;
        }
        return true;
    }

    //创建图书，子类去实现
    protected abstract Response createBook(Book book);

    //修改图书，子类去实现
    protected abstract Response updateBook(Book book);

    //删除图书，子类去实现
    protected abstract Response delBook(Book book);
}
