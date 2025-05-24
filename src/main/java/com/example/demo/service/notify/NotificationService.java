package com.example.demo.service.notify;

import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * @description 被观察者
 * @author ZhouYouMing
 * @date 2025/5/24 18:45
 */
@Service
public class NotificationService implements BookSubject{

    //观察者列表
    private final static List<BookObserver> observers = new CopyOnWriteArrayList<>();

    @Override
    public void register(BookObserver observer) {
        observers.add(observer);
    }

    @Override
    public void remove(BookObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notify(User user, Book book) {
        for(BookObserver observer : observers){
            observer.createBookSendUserNotice(user, book);
        }
    }
}
