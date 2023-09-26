<p align="center">
	<strong>一个基于 SpringBoot 的 Apollo Config Starter </strong>
</p>

<p align="center">
    <a>
        <img src="https://img.shields.io/badge/JDK-1.8+-green.svg" >
    </a>
    <a>
        <img src="https://img.shields.io/badge/springBoot-2.2.5.RELEASE-green.svg" >
    </a>
<a>
        <img src="https://img.shields.io/badge/springCloud-Hoxton.SR3-green.svg" >
    </a>
    <a href="https://www.jetbrains.com">
        <img src="https://img.shields.io/badge/IntelliJ%20IDEA-support-blue.svg" >
    </a>
    <a>
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" >
    </a>
</p>

# apollo-config-spring-boot-starter

## 介绍

用于解决 Apollo 配置中心使用 @ConfigurationProperties 配置实时更新失败

## 使用说明

* 引入依赖

```xml

<dependency>
    <groupId>io.github.izachwei</groupId>
    <artifactId>apollo-config-spring-boot-starter</artifactId>
    <version>1.0.0-RELEASE</version>
</dependency>
```

* 在 @ConfigurationProperties bean 上添加注解 `@ApolloConfigRefresh`，开启配置热更新


## 原理

```html
apollo update key -> ApolloConfigChangeListener -> apolloConfigCache.get(configKey) -> refreshScope.refresh()
```