package com.goya.service;

import com.goya.error.BusinessException;
import com.goya.service.model.OrderModel;

/**
 * @author cj
 * @date 2019-09-20 - 23:04
 */
public interface OrderService {
    //什么用户下，下单的商品，数量，完成对应的交易的过程
    //创建订单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException;
    //1、通过前端url上传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已开始
    //2、直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单


    String generateOrderNo();

}
