package com.geek.snails.upm.service;

import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FilterChainAutoReloadService {

    private static final Logger logger =
            LoggerFactory.getLogger(FilterChainAutoReloadService.class);

    private AbstractShiroFilter shiroFilter;

    private FilterChainGenerateService filterChainGenerateService;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    protected void init() {
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                try {
                    logger.info("start to reload filter chain");
                    Map<String, String> chains = filterChainGenerateService.generateFilterChain(
                            FilterChainGenerateService.defaultDefinitions);
                    reloadFilterChain(chains);
                } catch (Exception e) {
                    logger.error("Failed to reload filter chain", e);
                } finally {
                    stopWatch.stop();
                    logger.info(
                            "End to reload filter chain, the total time = [}",
                            stopWatch.getTotalTimeMillis()
                    );
                }
            }
        }, 100, 60, TimeUnit.SECONDS);
    }

    private void reloadFilterChain(Map<String, String> chains) {
        if(CollectionUtils.isEmpty(chains)){
            return;
        }
        PathMatchingFilterChainResolver resolver =
                (PathMatchingFilterChainResolver) shiroFilter.getFilterChainResolver();
        // 过滤管理器
        DefaultFilterChainManager manager =
                (DefaultFilterChainManager) resolver.getFilterChainManager();

        //重新生成过滤链
        manager.getFilterChains().clear();
        for (String key : chains.keySet()) {
            String chain = chains.get(key);
            manager.createChain(key, chain.trim().replace(" ", ""));
        }
    }

    public void setShiroFilter(AbstractShiroFilter shiroFilter) {
        this.shiroFilter = shiroFilter;
    }

    public void setFilterChainGenerateService(
            FilterChainGenerateService filterChainGenerateService
    ) {
        this.filterChainGenerateService = filterChainGenerateService;
    }
}
