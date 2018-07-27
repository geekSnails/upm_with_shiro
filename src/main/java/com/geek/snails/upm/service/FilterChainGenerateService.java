package com.geek.snails.upm.service;

import com.geek.snails.upm.config.BaseConfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.config.IniFilterChainResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class FilterChainGenerateService {

    private static final Logger logger =
            LoggerFactory.getLogger(FilterChainGenerateService.class);

    //todo 设置自定义接口域名，用于获取路径和权限规则
    private static final String UPM_URL_RULER_URL =
            "http://***/upms/url/ruler?appName=%s";

    private static final String URL_RULE_FILE = "url_ruler_file.txt";

    public static String defaultDefinitions = "";

    private DataLoaderService dataLoaderService;

    private BaseConfig baseConfig;

    public Map<String, String> generateFilterChain(String definitions) {
        defaultDefinitions = definitions;
        //从配置文件加载权限配置
        Ini ini = new Ini();
        ini.load(definitions);
        Ini.Section section = ini.getSection(IniFilterChainResolverFactory.URLS);
        if (CollectionUtils.isEmpty(section)) {
            section = ini.getSection(Ini.DEFAULT_SECTION_NAME);
        }

        Map<String, String> urLRules = fetchURLRules();
        if (urLRules != null) {
            for (String key : urLRules.keySet()) {
                section.put(key, urLRules.get(key));
            }
        }
        writeChains2LocalDisk(section);
        return section;
    }

    private Map<String, String> fetchURLRules() {
        String result = dataLoaderService.loadData(
                String.format(UPM_URL_RULER_URL, baseConfig.getAppkey()),
                baseConfig.getSnapshotDirectory() + URL_RULE_FILE
        );
        Map<String, String> urlRules = JSONObject.parseObject(
                result,
                new TypeReference<TreeMap<String, String>>() {
                }
        );
        return urlRules;
    }

    private void writeChains2LocalDisk(Map<String, String> chains) {
        if (CollectionUtils.isEmpty(chains)) {
            return;
        }
        try {
            File file = new File(baseConfig.getSnapshotDirectory() + URL_RULE_FILE);
            FileUtils.writeStringToFile(file, JSON.toJSONString(chains), "UTF-8");
        } catch (IOException e) {
            logger.warn("Failed to write url ruler to local disk");
        }
    }

    public void setDataLoaderService(DataLoaderService dataLoaderService) {
        this.dataLoaderService = dataLoaderService;
    }

    public void setBaseConfig(BaseConfig baseConfig) {
        this.baseConfig = baseConfig;
    }

}
