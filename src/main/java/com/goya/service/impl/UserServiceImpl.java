package com.goya.service.impl;

import com.goya.dao.UserDOMapper;
import com.goya.dao.UserPasswordDOMapper;
import com.goya.dataobject.UserDO;
import com.goya.dataobject.UserPasswordDO;
import com.goya.error.BusinessException;
import com.goya.error.EmBusinessError;
import com.goya.service.UserService;
import com.goya.service.model.UserModel;
import com.goya.validator.ValidationResult;
import com.goya.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;

/**
 * @author cj
 * @date 2019-09-11 - 18:41
 */
//这个标注表示对应的UserServiceImpl就是spring的service
//可以通过这个service获取用户的领域模型的对象
@Service
public class UserServiceImpl implements UserService {

    //这里报错但不影响正常运行，可以给UserDOMapper加上@Repository,标识这个mapper，但是已经开启了自动扫描，所以可以用标注
    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdomapper获取到对应的用户dataobject
        //通过userDOMapper查询主键的方式能够获取到对应的userDO
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);


        if (userDO == null){
            return null;
        }
        //通过用户id获取对应的用户加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);

        //通过这个方法返回usermodel到controller层
        return convertFromDataObject(userDO,userPasswordDO);

    }

    @Override
    //加上事务标签，避免如果userdo插进去了，userpassworddo没有插进去，就会出现不在一个事务的问题
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //判空处理
//        if (StringUtils.isEmpty(userModel.getName())
//                || userModel.getGender() == null
//                || userModel.getAge() == null
//                || StringUtils.isEmpty(userModel.getTelphone())) {
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }
        //模型校验
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }


        //实现model->dataobject方法
        UserDO userDO = convertFromModel(userModel);
        try {
            //如果用insert的话，对应字段是null的话，那就会用null覆盖掉数据库里的默认值
            //如果是insertSelective的话，对应用程序dataobject那个null的意思就是未定义，不覆盖
            //这种在update里面很有用，因为如果对应字段如果是个null的话，那我就认定不更新它这个字段
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已存在");
        }

        userModel.setId(userDO.getId());

        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
        return;
    }
    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过用户手机获取用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);

        if (userDO == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO= userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if (!StringUtils.equals(encrptPassword,userModel.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserDO convertFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

    //定义一个返回值是usermodel的方法，它可以通过userDo和userPasswordDO可以完美的组装成一个UserModel对象
    private UserModel convertFromDataObject(UserDO userDo, UserPasswordDO userPasswordDO) {
        //非空校验
        if (userDo == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        //使用BeanUtils的copy方法把对应的userDO属性copy到userModel
        BeanUtils.copyProperties(userDo, userModel);

        if (userPasswordDO != null) {
            //这里不能使用copy，因为它内部byid字段是重复的，因此我们使用get/set方法把这个password设置进去
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        //返回出去
        return userModel;

    }
}
