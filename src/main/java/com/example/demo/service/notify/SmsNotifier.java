package com.example.demo.service.notify;

import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.User;
import org.springframework.stereotype.Service;

/***
 * @description 具体观察者：发送sms
 * @author ZhouYouMing
 * @date 2025/5/24 18:45
 */
@Service
public class SmsNotifier implements BookObserver {

    @Override
    public void createBookSendUserNotice(User user, Book book) {
        //发送短信通知
        System.out.println("发送短信，用户联系电话："+user.getPhone());
        System.out.println("新书上架了，快来看看，新书名称："+book.getName());
        //发送成功，将数据存入到数据库：通知记录表
        //具体的入库逻辑.....
    }
}
