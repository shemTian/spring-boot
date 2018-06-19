package com.qik.demo.config.web.msgconvert;

import java.lang.annotation.*;

/**
 * AccountParameter
 * 作用等同于{@link org.springframework.web.bind.annotation.RequestBody}
 * 并且额外填入account信息，从cookie,header<br/>
 * 注解参数必需为对象,且需要有accountId属性,转换出accountId信息填入注解对象的accountId属性
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/30 12:02
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AccountParameter {
}
