package com.goya.service;

import com.goya.controller.viewobject.UserVO;
import com.goya.error.BusinessException;
import com.goya.service.model.UserModel;

/**
 * @author cj
 * @date 2019-09-11 - 18:40
 */
public interface UserService {


    //通过用户id获取用户对象的方法
    //通过这个service能够获取用户的领域模型的对象
    UserModel getUserById(Integer id);

    //用户注册请求
    void register(UserModel userModel) throws BusinessException;

    //用户登录
    /*
    telphone:用户注册手机
    password:用户加密后的密码
     */
    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;

    //通过缓存获取用户对象
    UserModel getUserbyIdInCache(Integer id);
}
