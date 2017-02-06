package org.rpcserver.test.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RpcService Annotation
 * Created by windwant on 2016/6/30.
 */
@Target(ElementType.TYPE) //标识类 接口 枚举
@Retention(RetentionPolicy.RUNTIME) //运行时有效
@Component
public @interface RpcService {
    Class<?> value();
}
