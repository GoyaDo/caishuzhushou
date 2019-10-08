package com.goya.service;

import com.goya.service.model.PromoModel;

/**
 * @author cj
 * @date 2019-09-21 - 14:07
 */

public interface PromoService {
    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    void  publishPromo(Integer promoId);

    //生成秒杀用的令牌
    String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId);
}
