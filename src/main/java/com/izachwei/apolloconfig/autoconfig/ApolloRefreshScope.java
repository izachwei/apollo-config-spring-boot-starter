package com.izachwei.apolloconfig.autoconfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * 用于标识该SpringBean下引用的 @ConfigurationProperties配置会热更新
 * <p>解决 apollo @ConfigurationProperties 注解配置无法热更新</p>
 *
 * @author izachwei
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Documented
@RefreshScope
public @interface ApolloRefreshScope {

    /**
     * 需要热更新的配置类（BeanName）
     *
     * @return {config1, config2}
     */
    String[] config() default {};

    /**
     * 覆盖默认值 ScopedProxyMode.DEFAULT
     * 这里很重要，不覆盖默认值则会导致 refreshScope 中的 cache 数据异常，key不以scopedTarget开始。
     *
     * @return Scope.proxyMode
     * @see Scope#proxyMode()
     */
    ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;
}
