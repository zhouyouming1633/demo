package com.example.demo.service.book.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.emums.BookStatusEnum;
import com.example.demo.emums.CodeMsg;
import com.example.demo.emums.UserStatusEnum;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.User;
import com.example.demo.model.input.BookParamIn;
import com.example.demo.model.output.Response;
import com.example.demo.service.book.BookService;
import com.example.demo.service.book.biz.BookBiz;
import com.example.demo.service.notify.EmailNotifier;
import com.example.demo.service.notify.NotificationService;
import com.example.demo.service.notify.SmsNotifier;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/***
 * @description 图书服务实现类
 * @author ZhouYouMing
 * @date 2025/5/24 18:22
 */
@Service
@Log4j2
public class BookServiceImpl implements BookService {

    @Autowired
    public BookMapper bookMapper;

    @Autowired
    public UserMapper userMapper;

    @Autowired
    public BookBiz bookBiz;

    @Autowired
    public NotificationService notificationService;

    @Autowired
    public EmailNotifier emailNotifier;

    @Autowired
    public SmsNotifier smsNotifier;

    @PostConstruct
    public void initNotification(){
        notificationService.register(emailNotifier);
        notificationService.register(smsNotifier);
    }

    /***
     * @description 根据作者名称分页查询
     * @author ZhouYouMing
     * @date 2025/5/24 18:21
     * @param paramIn
     * @return Response<List < Book>>
     */
    @Override
    public Response<List<Book>> getBookPage(BookParamIn paramIn) {
        //参数校验
        if(paramIn == null){
            return Response.error(CodeMsg.PARAM_ERROR.getCode(),CodeMsg.PARAM_ERROR.getMsg());
        }
        //构建查询参数
        Page<Book> page = new Page<>(paramIn.getPageNo(), paramIn.getPageSize());
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(paramIn.getAuthor())){
            queryWrapper.eq("author", paramIn.getAuthor());
        }
        //获取分页结果
        IPage<Book> res = bookMapper.selectPage(page, queryWrapper);
        if(res != null){
            return Response.success(res.getRecords());
        }
        return Response.success(new ArrayList<>());
    }

    /***
     * @description 创建图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:21
     * @param book
     * @return Response<Boolean>
     */
    @Override
    public Response<Boolean> createBook(Book book) {
        //必填参数校验
        if(book == null || StringUtils.isAllBlank(book.getIsbn(),book.getName(),book.getAuthor())){
            return Response.error(CodeMsg.PARAM_ERROR);
        }
        //时间设置
        book.setCreateTime(new Date());
        book.setUpdateTime(new Date());
        //创建图书，使用了模板方法模式
        Response response = bookBiz.create(book);
        if(response.isSuccess()){
            //创建成功，异步执行通知用户有新书上架了，无需等待结果返回
            CompletableFuture.runAsync(() -> {
                try {
                    //发送通知，模拟取前10位用户进行通知
                    Page<User> page = new Page<>(1, 10);
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("status", UserStatusEnum.EFFECTIVE.getStatus());
                    IPage<User> res = userMapper.selectPage(page, queryWrapper);
                    if(res != null && !CollectionUtils.isEmpty(res.getRecords())){
                        for(User user : res.getRecords()){
                            //发送通知，使用了观察者模式
                            notificationService.notify(user,book);
                        }
                    }
                } catch (Exception e) {
                    log.error("BookService createBook notify error",e);
                }
            });

        }
        return response;
    }

    /***
     * @description 修改图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:21
     * @param book
     * @return Response<Boolean>
     */
    @Override
    public Response<Boolean> modifyBook(Book book) {
        //参数校验
        if(book == null || StringUtils.isBlank(book.getIsbn())){
            return Response.error(CodeMsg.PARAM_ERROR);
        }
        //状态合法校验
        if(book.getStatus()!=null && !BookStatusEnum.checkStatus(book.getStatus())){
            return Response.error(CodeMsg.PARAM_ERROR);
        }
        return bookBiz.update(book);
    }

    /***
     * @description 删除图书
     * @author ZhouYouMing
     * @date 2025/5/24 18:21
     * @param book
     * @return Response<Boolean>
     */
    @Override
    public Response<Boolean> delBook(Book book) {
        //isbn编号是必传的
        if(StringUtils.isBlank(book.getIsbn())){
            return Response.error(CodeMsg.PARAM_ERROR);
        }
        return bookBiz.del(book);
    }
}
