package com.example.demo.service.notify;

import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.User;

/***
 * @description 主题对象，维护一个观察者列表
 * @author ZhouYouMing
 * @date 2025/5/24 18:43
 */
public interface BookSubject {

    void register(BookObserver observer);

    void remove(BookObserver observer);

    void notify(User user, Book book);
}
