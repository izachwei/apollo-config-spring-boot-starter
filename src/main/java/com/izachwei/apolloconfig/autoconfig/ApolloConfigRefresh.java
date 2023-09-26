package com.izachwei.apolloconfig.autoconfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * <p>解决apollo @ConfigurationProperties注解配置无法热更新</p>
 * 收集 configKey 与 bean 的对应关系（{@code apolloConfigCache}）, 用于配置更新时（{@code ApolloConfigChangeListener}）找到对应 bean
 * 通过{@code refreshScope}刷新bean
 *
 * update changeKey -> ApolloConfigChangeListener -> apolloConfigCache.get(configKey) -> refreshScope.refresh()
 *
 * @author izachwei
 */
public class ApolloConfigRefresh implements ApplicationContextAware,
    BeanPostProcessor {

    private ApplicationContext applicationContext;

    private final Map<String, String> apolloConfigCache = new HashMap<>();

    @Autowired
    private RefreshScope refreshScope;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ApolloRefreshScope annotation = AnnotationUtils.findAnnotation(bean.getClass(), ApolloRefreshScope.class);
        if (annotation != null) {
            processConfigBean(bean, beanName);
        }
        return bean;
    }

    /**
     * 解析当前bean对象，建立 configKey 与 beanName 的对应关系存放在 {@code apolloConfigCache}
     *
     * @param bean bean对象
     * @param beanName beanName
     */
    private void processConfigBean(Object bean, String beanName) {
        ConfigurationProperties beanConfiguration = AnnotationUtils.findAnnotation(bean.getClass(),
            ConfigurationProperties.class);
        if (beanConfiguration != null) {
            // 当前是 configurationProperties 标记的对象
            addApolloConfig(beanConfiguration.prefix() + ".", beanName);
        } else {
            // 当前是普通bean，则解析当前bean依赖注入config bean
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                String[] beanNamesForType = applicationContext.getBeanNamesForType(declaredField.getType());
                for (String findBeanName : beanNamesForType) {
                    Object findBean = applicationContext.getBean(beanName);
                    Class<?> aClass = findBean.getClass();
                    ConfigurationProperties annotation = AnnotationUtils.findAnnotation(aClass,
                        ConfigurationProperties.class);
                    if (annotation != null) {
                        addApolloConfig(annotation.prefix() + ".", beanName);
                    }
                }

            }
        }
    }

    private void addApolloConfig(String configPrefix, String beanName) {
        this.apolloConfigCache.put(configPrefix, beanName);
    }

    @ApolloConfigChangeListener()
    private void refresh(ConfigChangeEvent changeEvent) {
        List<String> configBeanNameList = findConfigBeanNameByChangeEvent(changeEvent);
        configBeanNameList.forEach(configBeanName -> refreshScope.refresh(configBeanName));
    }

    /**
     * @param changeEvent
     * @return
     */
    private List<String> findConfigBeanNameByChangeEvent(ConfigChangeEvent changeEvent) {
        List<String> res = new ArrayList<>();
        return changeEvent.changedKeys().stream().map(
                changeKey -> apolloConfigCache.keySet().stream().filter(changeKey::startsWith).collect(Collectors.toList()))
            .flatMap(Collection::stream)
            .map(this.apolloConfigCache::get)
            .collect(Collectors.toList());
    }

    /**
     * TODO （暂未实现）遍历 BeanDefinition 的方式来解析 apolloConfig
     *
     * @param registry
     * @throws BeansException
     */
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        //for (String beanDefinitionName : beanDefinitionNames) {
        //    BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
        //    if (!StringUtils.isEmpty(beanDefinition.getBeanClassName())) {
        //        try {
        //            Class<?> resolvedClass = ClassUtils.forName(beanDefinition.getBeanClassName(),
        //                Thread.currentThread().getContextClassLoader());
        //            ApolloRefreshScope annotation = AnnotationUtils.findAnnotation(resolvedClass,
        //                ApolloRefreshScope.class);
        //            if (annotation != null) {
        //                String[] config = annotation.config();
        //                if (config.length < 1) {
        //                    Field[] declaredFields = resolvedClass.getDeclaredFields();
        //                    for (Field declaredField : declaredFields) {
        //                        Class<?> type = declaredField.getType();
        //
        //                    }
        //                }
        //            }
        //        } catch (ClassNotFoundException e) {
        //            throw new RuntimeException(e);
        //        }
        //    }
    }
}