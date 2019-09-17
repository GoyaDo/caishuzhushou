package com.goya.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author cj
 * @date 2019-09-04 - 00:35
 */
@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;

    //实现校验方法并返回校验结果
    public ValidationResult validate(Object bean){
        ValidationResult result = new ValidationResult();
        //若对应的bean里面一些参数的规则有违背了对应violation定义的annotation的话，这个set里就会有这个值
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if (constraintViolationSet.size()>0){
            //有错误
            result.setHasErrors(true);
            //有错误就遍历这个set
            constraintViolationSet.forEach(constraintViolation->{
                //可以获取到哪个字段发生了错误
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        return result;
    }

    //当bean初始化完毕会回调ValidatorImpl的afterPropertiesSet方法
    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过工厂的初始化方式使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
