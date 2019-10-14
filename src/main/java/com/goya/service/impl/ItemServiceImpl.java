package com.goya.service.impl;

import com.goya.dao.ItemDOMapper;
import com.goya.dao.ItemStockDOMapper;
import com.goya.dao.StockLogDOMapper;
import com.goya.dataobject.ItemDO;
import com.goya.dataobject.ItemStockDO;
import com.goya.dataobject.StockLogDO;
import com.goya.error.BusinessException;
import com.goya.error.EmBusinessError;
import com.goya.mq.MqProducer;
import com.goya.service.ItemService;
import com.goya.service.PromoService;
import com.goya.service.model.ItemModel;
import com.goya.service.model.PromoModel;
import com.goya.validator.ValidationResult;
import com.goya.validator.ValidatorImpl;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author cj
 * @date 2019-09-20 - 15:05
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;


    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if (itemModel == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        //不会copy类型不一样的对象
        BeanUtils.copyProperties(itemModel,itemDO);
        //需要转换对象
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        if (itemModel== null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }
        //转化itemmodel->dataobject
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        //转出itemStockDO
        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);


        itemStockDOMapper.insertSelective(itemStockDO);

        //返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    //商品列表浏览
    @Override
    public List<ItemModel> listItem() {

        List<ItemDO> itemDOList = itemDOMapper.listItem();
        //使用java8的streamAPI，将里面的每一个ItemDO，map成一个ItemModel
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null){
            return null;
        }

        //操作获得库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        //将dataobject->model
        ItemModel itemModel = convertModelFromDataObject(itemDO,itemStockDO);

        //获取活动商品信息，看对应的商品是否在秒杀活动内
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus().intValue()!=3){
            //把对应秒杀信息设置在model上面
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        //affectedRow：影响的条目数
//        int affectedRow = itemStockDOMapper.decreaseStock(itemId, amount);
       long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue()*-1);
        if (result > 0) {
            //更新库存成功
//            boolean mqResult = mqProducer.asyncReduceStock(itemId,amount);
//            if (!mqResult){
//                redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
//                return false;
//            }
            return true;
        }else if (result == 0){
            //打上库存已售罄的标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid"+itemId,"true");
            return true;
        }else {
            //更新库存失败
            increaseStock(itemId,amount);
            return false;
        }

    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) throws BusinessException {
        redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
        return true;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId,amount);
    }


    //item及promo model缓存模型
    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_"+id);
        if (itemModel == null){
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_"+id,itemModel);
            redisTemplate.expire("item_validate_"+id,10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean mqResult = mqProducer.asyncReduceStock(itemId,amount);
        return mqResult;
    }

    //初始化库存的流水
    @Override
    @Transactional
    public String initStock_log(Integer itemId, Integer amount) {
        //设置stockLog的状态
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        //使用UUID的方式生成stockLogId，并且作为它对应的主键，set到数据库里
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        //设置状态
        stockLogDO.setStatus(1);

        stockLogDOMapper.insertSelective(stockLogDO);
        return stockLogDO.getStockLogId();
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        //手动处理price对象
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}
