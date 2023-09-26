package com.izachwei.apolloconfig.autoconfig;

import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author izachwei
 */
@Configuration
@ConditionalOnClass({ApolloConfigChangeListener.class})
public class ApolloConfigAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApolloConfigRefresh apolloConfigRefresh() {
        return new ApolloConfigRefresh();
    }
}