package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.entity.User;

/***
 * @description 用户账户操作类
 * @author ZhouYouMing
 * @date 2025/5/24 18:40
 */
public interface  UserMapper extends BaseMapper<User> {

    /***
     * @description 根据唯一pin账号查询用户信息
     * @author ZhouYouMing
     * @date 2025/5/23 21:54
     * @param pin
     * @return User
     */
    User selectUserByPin(String pin);
}
