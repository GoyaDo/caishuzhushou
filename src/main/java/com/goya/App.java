package com.goya;

import com.goya.dao.UserDOMapper;
import com.goya.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 */

//将这个类变成自动化可以支持配置的一个类，并且开启基于springboot的自动化配置
//@EnableAutoConfiguration
//指定他是主启动类,goya下的包依次扫描，自动发现service，commont等spring特定注解
@SpringBootApplication(scanBasePackages = {"com.goya"})
@RestController
//dao存放的位置放在这个包下
@MapperScan("com.goya.dao")

//可以解析aop的一些配置
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class App {

    /*


    //这里报错但不影响正常运行，可以给UserDOMapper加上@Repository,标识这个mapper，但是已经开启了自动扫描，所以可以用标注
    @Autowired
    private UserDOMapper userDOMapper;

    @RequestMapping("/")
    public String home() {
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if (userDO == null) {
            return "用户对象不存在";
        } else {
            return userDO.getName();
        }

    }
    */


    public static void main(String[] args) {
        System.out.println("Hello World!");
        SpringApplication.run(App.class, args);
    }
}
