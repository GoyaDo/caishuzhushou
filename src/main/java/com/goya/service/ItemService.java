package com.goya.service;

import com.goya.error.BusinessException;
import com.goya.service.model.ItemModel;

import java.util.List;

/**
 * @author cj
 * @date 2019-09-20 - 15:03
 */
public interface ItemService {
    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //商品详情浏览
    ItemModel getItemById(Integer id);
}
