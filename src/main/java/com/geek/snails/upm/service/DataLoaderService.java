package com.geek.snails.upm.service;

import com.geek.snails.upm.exception.DataLoadException;
import com.geek.snails.upm.util.HttpClientUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DataLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceAuthorizingRealm.class);

    private HttpClient httpClient;

    public String loadData(String url, String backedFilePath) {
        try {
            return loadDataFromUrl(url);
        } catch (Exception e) {
            logger.warn("Failed to load data from url, the url = {}", url, e);
            return loadDataFromLocalFile(backedFilePath);
        }
    }

    private String loadDataFromUrl(String url) {
        String result = HttpClientUtil.simpleProxyGet(url, httpClient);
        JSONObject jsonObject =
                (JSONObject) JSONPath.eval(JSON.parseObject(result), "$.data");
        return jsonObject.toJSONString();
    }

    private String loadDataFromLocalFile(String backedFilePath) {
        try {
            File file = new File(backedFilePath);
            String result = FileUtils.readFileToString(file, "UTF-8");
            return result;
        } catch (Exception e) {
            throw new DataLoadException("Failed to load data from local disk" + backedFilePath, e);
        }
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
