# 功能描述
一般公司内部有很多个系统，几乎每个系统都要涉及权限管理，如果每个系统都实现自己的权限管理逻辑和权限管理页面，这是一项繁琐和重复的工作，本软件就是为了解决这个问题。   
本软件提供一个简单的Jar包，其他系统通过引入这个Jar包，以及简单的配置，就可以进行鉴权和授权相关工作。  

## 软件依赖  
本软件没有提供登录的相关页面，需要借助公司的SSO服务来完成鉴权功能。  
本软件授权需要依赖Http接口，需要自己实现统一的权限管理页面并且提供接口，本软件依赖对应的接口来完成授权工作。  

## 接入步骤
1、下载本软件，使用maven完成jar包安装，其他项目引入Jar包。  
2、引入过滤器。 
```
<filter>
  <filter-name>shiroFilter</filter-name>
  <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
  <init-param>
    <param-name>targetFilterLifecycle</param-name>
    <param-value>true</param-value>
  </init-param>
</filter>
<filter-mapping>
  <filter-name>shiroFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```  
3、在classpath目录下，配置shiro.properties文件,为了防止缺少配置文件，导致权限问题，启动时，如果未配置必填配置，会启动失败.  
```    
app_key=test //应用在UPM中注册的唯一表示，必填
sso_recall_address=localhost:80 //SSO登录成功的回调地址，必填
snapshot_directory=/home/qiso/shiroBackedData/  //权限硬盘的备份地址，选填，有默认值。
```  
## 待完善点
1、目前权限粒度控制的比较大，只控制到了role和url的级别。  
2、需要自己开发权限管理页面以及提供权限相关接口。  
