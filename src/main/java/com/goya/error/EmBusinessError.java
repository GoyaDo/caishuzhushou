package com.goya.error;

/**
 * @author cj
 * @date 2019-09-16 - 23:15
 */
//把对应的error信息取出来
public enum EmBusinessError implements CommonError{

    //通用错误类型10001
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知错误"),

    //20000开头为用户信息相关错误定义
    USER_NOT_EXIST(20001,"用户不存在"),
    //这个信息不能太明显
    USER_LOGIN_FAIL(20002,"用户手机号或密码不正确"),
    //用户还未登录
    USER_NOT_LOGIN(20003,"用户还未登录"),

    //30000开头为交易信息错误定义
    STOCK_NOT_ENOUGH(30001,"库存不足"),
    ;

    //当USER_NOT_EXIST被定义出来之后，
    // 它可以直接通过对应的构造方法构造出来一个实现了CommonError接口的子类
    //这个子类是个enum类型

    private EmBusinessError(int errCode,String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
