package com.goya.service;

import com.goya.service.model.PromoModel;

/**
 * @author cj
 * @date 2019-09-21 - 14:07
 */

public interface PromoService {
    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);
}
