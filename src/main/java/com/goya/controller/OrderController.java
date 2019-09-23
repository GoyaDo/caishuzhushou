package com.goya.controller;

import com.goya.error.BusinessException;
import com.goya.error.EmBusinessError;
import com.goya.response.CommonReturnType;
import com.goya.service.OrderService;
import com.goya.service.model.OrderModel;
import com.goya.service.model.UserModel;
import com.sun.xml.internal.rngom.parse.host.Base;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author cj
 * @date 2019-09-21 - 12:51
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "{*}")//ajax跨域请求问题
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FROMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
                                        @RequestParam(name = "amount")Integer amount,
                                        @RequestParam(name = "promoId",required = false)Integer promoId) throws BusinessException{

//        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登录，不能下单");
        }
        //获取用户登录信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登录，不能下单");
        }

//        if (isLogin == null||!isLogin.booleanValue()){
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登录，不能下单");
//        }

        //获取用户登录信息
//        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);
        return CommonReturnType.create(null);
    }

}
