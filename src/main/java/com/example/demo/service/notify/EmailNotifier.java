package com.example.demo.service.notify;

import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.User;
import org.springframework.stereotype.Service;

/***
 * @description 具体观察者：发送email
 * @author ZhouYouMing
 * @date 2025/5/24 18:45
 */
@Service
public class EmailNotifier implements BookObserver {
    @Override
    public void createBookSendUserNotice(User user, Book book) {
        //发送email通知
        System.out.println("发送email，用户邮箱地址："+user.getEmail());
        System.out.println("新书上架了，快来看看，新书名称："+book.getName());
        //将数据存入到数据库：通知记录表
        //具体的入库逻辑.....
    }
}
