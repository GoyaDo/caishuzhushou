package com.goya.controller;

import com.alibaba.druid.util.StringUtils;
import com.goya.controller.viewobject.UserVO;
import com.goya.error.BusinessException;
import com.goya.error.EmBusinessError;
import com.goya.response.CommonReturnType;
import com.goya.service.UserService;
import com.goya.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author cj
 * @date 2019-09-11 - 18:36
 */
//controller标记用来被spring扫描到，这个controller的name就是user
@Controller("user")
//它的url上用过/user访问到
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "{*}")//ajax跨域请求问题
//Access to XMLHttpRequest at 'http://localhost:8080/user/getotp'
// from origin 'null' has been blocked by CORS policy:
// No 'Access-Control-Allow-Origin' header is present on the requested resource.
//DEFAULT_ALLOW_CREDENTIALS=true:需配合xhrFields授信后使得跨域session共享
public class UserController extends BaseController{

    @Autowired
    private UserService userService;


    //通过bean的方式注入进来
    @Autowired
    private HttpServletRequest httpServletRequest;//这是一个单例的模式


    //用户登录接口
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FROMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone)||StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登录服务，用来校验用户登录是否合法
        UserModel userModel = userService.validateLogin(telphone,this.EncodeByMd5(password));

        //将登录凭证加入到用户登录成功的session内,假设是单点session登录
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        return CommonReturnType.create(null);
    }


    //用户注册接口
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FROMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的otpcode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        //com.alibaba.druid.util.StringUtils;内部做了非null处理
        if (!StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);
        return CommonReturnType.create(null);

    }

    //md5加密
    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }

    //用户获取otp短信接口
    //method这样的标注是说对应的getotp方法必须得映射到httppost请求才能够生效
    @RequestMapping(value = "/getotp",method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FROMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone){
        //需要一定规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;//此时随机数取值[10000,109999)
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码同对应用户的手机号关联，使用httpsession的方式绑定他的手机号与otpcode
        //在企业级应用里，这样的key-value形式一般封装在redis中
        httpServletRequest.getSession().setAttribute(telphone,otpCode);

        //将OTP验证码通过短信通道发送给用户，省略，涉及短信通道的一些流程
        //可以买一些第三方软件服务的通道，然后以最简单Http.post的方式，将一个短信模板的内容post给用户的手机号即可



        //在企业里用log4j，这里是为了简单,在正式开发不能打印
        System.out.println("telphone = " + telphone + " & otpCode = " +otpCode);

        return CommonReturnType.create(null);

    }




    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id")Integer id) throws BusinessException {//@RequestParam接收一个name="id"的入参
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的对应用户信息不存在
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        //将核心领域模型用户对象转化为可供UI使用的viewobjec
        UserVO userVO =  convertFromModel(userModel);

        //返回通用对象
        return CommonReturnType.create(userVO);
    }

    //将model转换成vo
    private UserVO convertFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
