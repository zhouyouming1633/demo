package com.example.demo;

import com.example.demo.emums.BookStatusEnum;
import com.example.demo.model.entity.Book;
import com.example.demo.model.input.BookParamIn;
import com.example.demo.model.output.Response;
import com.example.demo.service.book.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author ZhouYouMing
 * @description TODO
 * @date 2025/5/24 15:46
 */
@SpringBootTest
public class BookServiceUnitTest {

    @Autowired
    private BookService bookService;


    @Test
    void testGetBookPage_data_not_empty() {
        BookParamIn paramIn = new BookParamIn();
        paramIn.setAuthor("jim");
        Response<List<Book>> response =  bookService.getBookPage(paramIn);
        assertEquals(true, response.getData().size()>0);
    }

    @Test
    void testGetBookPage_data_empty() {
        BookParamIn paramIn = new BookParamIn();
        paramIn.setAuthor("null");
        Response<List<Book>> response =  bookService.getBookPage(paramIn);
        assertEquals(true, response.getData().size()==0);
    }

    @Test
    void testCreate_book_success() {
        Book book = new Book();
        book.setAuthor("jim");
        //随机生成
        long randomNum = (long) (Math.random() * (9999999999999L - 1000000000000L + 1)) + 1000000000000L;
        book.setIsbn(randomNum+"");
        book.setStatus(BookStatusEnum.ON_LINE.getStatus());
        book.setName("jack");
        book.setPublisher("浙江工业");
        book.setCreateTime(new Date());
        book.setUpdateTime(new Date());
        Response<Boolean> response =  bookService.createBook(book);
        assertEquals(true, response.getData());
    }

    @Test
    void testCreate_book_error() {
        Book book = new Book();
        book.setAuthor("jim");
        book.setIsbn("978-7-544-25");
        book.setStatus(BookStatusEnum.ON_LINE.getStatus());
        book.setName("jack");
        book.setPublisher("浙江工业");
        book.setCreateTime(new Date());
        book.setUpdateTime(new Date());
        Response<Boolean> response =  bookService.createBook(book);
        assertEquals(false, response.isSuccess());
    }

    @Test
    void testDel_book_success() {
        Book book = new Book();
        book.setIsbn("978-7-544-26");
        book.setStatus(BookStatusEnum.DEL.getStatus());
        Response<Boolean> response =  bookService.delBook(book);
        assertEquals(true, response.isSuccess());
    }

    @Test
    void testDel_book_error() {
        Book book = new Book();
        book.setIsbn("7883311336251");
        book.setStatus(BookStatusEnum.DEL.getStatus());
        Response<Boolean> response =  bookService.delBook(book);
        assertEquals(false, response.isSuccess());
    }

    @Test
    void testUpdate_book_success() {
        Book book = new Book();
        book.setIsbn("978-7-544-25");
        book.setAuthor("jim");
        book.setStatus(BookStatusEnum.ON_LINE.getStatus());
        book.setName("jack");
        book.setPublisher("浙江工业");
        Response<Boolean> response =  bookService.modifyBook(book);
        assertEquals(true, response.isSuccess());
    }

    @Test
    void testUpdate_book_error() {
        Book book = new Book();
        book.setIsbn("7883311336251");
        book.setAuthor("jim");
        book.setStatus(BookStatusEnum.ON_LINE.getStatus());
        book.setName("jack");
        book.setPublisher("浙江工业");
        Response<Boolean> response =  bookService.modifyBook(book);
        assertEquals(false, response.isSuccess());
    }


}
