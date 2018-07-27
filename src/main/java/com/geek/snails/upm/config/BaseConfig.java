package com.geek.snails.upm.config;

import com.geek.snails.upm.exception.ConfigBlankException;

import org.springframework.util.StringUtils;

public class BaseConfig {

    private String appkey;

    private String ssoRecallAddress;

    //todo 设置硬盘备份路径
    private String snapshotDirectory = "/home/qiso/shiroBackedData/";

    public BaseConfig(String appkey, String ssoRecallAddress, String snapshotDirectory) {
        if (StringUtils.isEmpty(appkey) || StringUtils.isEmpty(snapshotDirectory)) {
            throw new ConfigBlankException("appkey or ssoRecallAddress can not be null");
        }
        this.appkey = appkey;
        this.ssoRecallAddress = ssoRecallAddress;
        if (!StringUtils.isEmpty(snapshotDirectory)) {
            this.snapshotDirectory = snapshotDirectory;
        }
    }

    public String getAppkey() {
        return appkey;
    }

    public String getSsoRecallAddress() {
        return ssoRecallAddress;
    }

    public String getSnapshotDirectory() {
        return snapshotDirectory;
    }
}
