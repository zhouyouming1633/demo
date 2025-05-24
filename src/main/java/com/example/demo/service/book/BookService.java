package com.example.demo.service.book;

import com.example.demo.model.entity.Book;
import com.example.demo.model.input.BookParamIn;
import com.example.demo.model.output.Response;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 * @description book服务类
 * @author ZhouYouMing
 * @date 2025/5/24 18:21
 */
@Service
public interface BookService {

    /***
     * @description 根据作者名称分页查询
     * @author ZhouYouMing
     * @date 2025/5/24 18:21
     * @param paramIn
     * @return Response<List < Book>>
     */
     Response<List<Book>> getBookPage(BookParamIn paramIn);

    /***
     * @description 创建图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:21
     * @param book
     * @return Response<Boolean>
     */
    Response<Boolean> createBook(Book book);


    /***
     * @description 修改图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:21
     * @param book
     * @return Response<Boolean>
     */
    Response<Boolean> modifyBook(Book book);

    /***
     * @description 删除图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:21
     * @param book
     * @return Response<Boolean>
     */
    Response<Boolean> delBook(Book book);
}
