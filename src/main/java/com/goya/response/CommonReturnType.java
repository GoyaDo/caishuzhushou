package com.goya.response;

/**
 * @author cj
 * @date 2019-09-16 - 22:48
 */
public class CommonReturnType {
    //表名对应请求的返回处理结果"success"或"fail"
    private String status;

    //若status=success，则data内返回前端需要的json数据
    //若status=fail，则data内使用通用的错误码格式
    private Object data;


    //定义一个通用的创建方法
    /*
    这个方法的意义是当controller完成处理，我调用对应的create方法，如果它不带任何status，
    它对应的status就是success。然后它创建一个对应的CommonReturnType，并把对应的值返回
     */
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }

    //定义一个通用的创建返回对象的方法
    public static CommonReturnType create(Object result,String status){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
