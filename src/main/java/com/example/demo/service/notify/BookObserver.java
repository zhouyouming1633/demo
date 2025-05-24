package com.example.demo.service.notify;

import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.User;

/***
 * @description 观察者类
 * @author ZhouYouMing
 * @date 2025/5/24 18:42
 */
public interface BookObserver {

    void createBookSendUserNotice(User user, Book book);
}
