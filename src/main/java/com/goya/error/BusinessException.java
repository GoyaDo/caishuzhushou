package com.goya.error;

/**
 * @author cj
 * @date 2019-09-16 - 23:31
 */
//包装器业务异常类实现
public class BusinessException extends Exception implements CommonError{

    //其实这个CommonError就是EmBusinessError类
    private CommonError commonError;


    //直接接收EmBusinessError的传参用于构造业务异常
    public BusinessException(CommonError commonError){
        super();//Exception自身会有一些初始化机制在里面
        this.commonError = commonError;
    }

    //接收自定义errMsg的方式构造业务异常
    public BusinessException(CommonError commonError,String errMsg){
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }



    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
