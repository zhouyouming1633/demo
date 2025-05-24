package com.example.demo.service.book.biz;

import com.example.demo.emums.BookStatusEnum;
import com.example.demo.emums.CodeMsg;
import com.example.demo.model.entity.Book;
import com.example.demo.model.output.Response;
import org.springframework.stereotype.Service;

/***
 * @description 图书服务业务处理
 * @author ZhouYouMing
 * @date 2025/5/24 18:34
 */
@Service
public class BookBiz extends BookDeal {

    /***
     * @description 创建图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:35
     * @param book
     * @return Response
     */
    @Override
    protected Response createBook(Book book) {
        int r = bookMapper.insert(book);
        if(r > 0){
            return Response.success(true);
        }
        return Response.error(CodeMsg.BOOK_CREATE_ERROR);
    }

    /***
     * @description 更新图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:35
     * @param book
     * @return Response
     */
    @Override
    protected Response updateBook(Book book) {
        Integer u = bookMapper.updateBookInfo(book);
        if(u != null && u > 0){
            return Response.success(true);
        }
        return Response.error(CodeMsg.BOOK_UPDATE_ERROR);
    }

    /***
     * @description 删除图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:35
     * @param book
     * @return Response
     */
    @Override
    protected Response delBook(Book book) {
        //删除
        book.setStatus(BookStatusEnum.DEL.getStatus());
        Integer u = bookMapper.updateBookStatus(book);
        if(u != null && u > 0){
            return Response.success(true);
        }
        return Response.error(CodeMsg.BOOK_DEL_ERROR);
    }
}
