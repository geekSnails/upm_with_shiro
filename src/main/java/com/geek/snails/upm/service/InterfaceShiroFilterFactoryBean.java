package com.geek.snails.upm.service;

import org.apache.shiro.spring.web.ShiroFilterFactoryBean;

import java.util.Map;

public class InterfaceShiroFilterFactoryBean extends ShiroFilterFactoryBean {

    private FilterChainGenerateService filterChainGenerateService;

    @Override
    public void setFilterChainDefinitions(String definitions) {
        Map<String, String> filterChains =
                filterChainGenerateService.generateFilterChain(definitions);
        setFilterChainDefinitionMap(filterChains);
    }

    public void setFilterChainGenerateService(
            FilterChainGenerateService filterChainGenerateService
    ) {
        this.filterChainGenerateService = filterChainGenerateService;
    }
}
