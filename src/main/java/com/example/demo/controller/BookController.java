package com.example.demo.controller;


import com.example.demo.emums.BookStatusEnum;
import com.example.demo.emums.CodeMsg;
import com.example.demo.model.entity.Book;
import com.example.demo.model.input.BookParamIn;
import com.example.demo.model.output.Response;
import com.example.demo.service.book.BookService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/***
 * @description 图书操作控制器
 * @author ZhouYouMing
 * @date 2025/5/24 18:19
 */
@Log4j2
@RestController
@RequestMapping("api/v1")
public class BookController {

    @Autowired
    private BookService bookService;

    /***
     * @description 根据作者或者其他条件分页查询图书列表
     * @author ZhouYouMing
     * @date 2025/5/24 18:20
     * @param paramIn
     * @return Response<List < Book>>
     */
    @ResponseBody
    @RequestMapping(value = "/book",method = RequestMethod.GET)
    public Response<List<Book>> getBookPage(@RequestBody BookParamIn paramIn){
        try {
            return bookService.getBookPage(paramIn);
        } catch (Exception e) {
            log.error("BookController getBookPage exception",e);
            return  Response.error(CodeMsg.SYSTEM_ERROR);
        }
    }

    /***
     * @description 创建图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:20
     * @param book
     * @return Response<Boolean>
     */
    @ResponseBody
    @RequestMapping(value = "/book",method = RequestMethod.POST)
    public Response<Boolean> createBook(@RequestBody Book book){
        try {
            return bookService.createBook(book);
        } catch (Exception e) {
            log.error("BookController createBook exception",e);
            return  Response.error(CodeMsg.SYSTEM_ERROR);
        }
    }

    /***
     * @description 修改图书信息
     * @author ZhouYouMing
     * @date 2025/5/24 18:20
     * @param book
     * @return Response<Boolean>
     */
    @ResponseBody
    @RequestMapping(value = "/book",method = RequestMethod.PUT)
    public Response<Boolean> updateBook(@RequestBody Book book){
        try {
            return bookService.modifyBook(book);
        } catch (Exception e) {
            log.error("BookController updateBook exception",e);
            return  Response.error(CodeMsg.SYSTEM_ERROR);
        }
    }

    /***
     * @description 删除图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:20
     * @param isbn
     * @return Response<Boolean>
     */
    @ResponseBody
    @RequestMapping(value = "/book/{isbn}",method = RequestMethod.DELETE)
    public Response<Boolean> deleteBook(@PathVariable("isbn") String isbn){
        try {
            Book book = new Book();
            book.setIsbn(isbn);
            book.setStatus(BookStatusEnum.DEL.getStatus());
            return bookService.delBook(book);
        } catch (Exception e) {
            log.error("BookController deleteBook exception",e);
        }
        return  Response.error(CodeMsg.SYSTEM_ERROR);
    }


}
