package com.geek.snails.upm.service;

import com.geek.snails.upm.config.BaseConfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InterfaceAuthorizingRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceAuthorizingRealm.class);

    //todo 设置自定义接口域名， 获取全部用户的权限信息
    private static final String UPM_USER_ROLE_ALL_URL =
            "http://***/upms/user/role/all?appName=%s";

    private static final String USER_ROLE_FILE = "user_role_file.txt";

    private static final String LOAD_CACHE_KEY = "user_role_names_key";

    private DataLoaderService dataLoaderService;

    private BaseConfig baseConfig;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private LoadingCache<String, Map<String, String>> allUserRoleNames = CacheBuilder.newBuilder()
            .maximumSize(100)
            .refreshAfterWrite(1, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, Map<String, String>>() {
                        public Map<String, String> load(String key) throws IOException {
                            Map<String, String> userRoleNames = loadUserRoleNames();
                            writeUserRoleNames2LocalDisk(userRoleNames);
                            return userRoleNames;
                        }

                        public ListenableFuture<Map<String, String>> reload(
                                final String key,
                                final Map<String, String> urlRules
                        ) {
                            // asynchronous load data
                            ListenableFutureTask<Map<String, String>> task =
                                    ListenableFutureTask.create
                                            (new Callable<Map<String, String>>() {
                                                public Map<String, String> call() {
                                                    Map<String, String> userRoleNames =
                                                            loadUserRoleNames();
                                                    writeUserRoleNames2LocalDisk(userRoleNames);
                                                    return userRoleNames;
                                                }
                                            });
                            executor.execute(task);
                            return task;
                        }
                    }
            );

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String username = (String) principalCollection.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Set<String> roles = fetchUserRoles(username);
        authorizationInfo.setRoles(roles);
       /* Set<String> permissions = fetchUserPermission(username);
        authorizationInfo.setStringPermissions(permissions);*/
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws
            AuthenticationException {
        return null;
    }

    private Map<String, String> loadUserRoleNames() {
        try {
            String url = String.format(UPM_USER_ROLE_ALL_URL, baseConfig.getAppkey());
            String filePath = baseConfig.getSnapshotDirectory() + USER_ROLE_FILE;
            String result = dataLoaderService.loadData(url, filePath);
            Map<String, String> userRoleNames = JSONObject.parseObject(
                    result,
                    new TypeReference<Map<String, String>>() {
                    }
            );
            return userRoleNames;
        } catch (Exception e) {
            logger.error("Failed to load user role names", e);
            return Collections.emptyMap();
        }
    }

    private void writeUserRoleNames2LocalDisk(Map<String, String> userRoleNames) {
        try {
            if (CollectionUtils.isEmpty(userRoleNames)) {
                return;
            }
            File file = new File(baseConfig.getSnapshotDirectory() + USER_ROLE_FILE);
            FileUtils.writeStringToFile(file, JSON.toJSONString(userRoleNames), "UTF-8");
        } catch (IOException e) {
            logger.warn("Failed to write user role names to local disk");
        }
    }

    private Set<String> fetchUserRoles(String userName) {
        try {
            Map<String, String> userRoleNames = allUserRoleNames.get(LOAD_CACHE_KEY);
            if (userRoleNames == null) {
                return Collections.emptySet();
            }
            String roleNameStr = userRoleNames.get(userName);
            if (StringUtils.isEmpty(userRoleNames)) {
                return Collections.emptySet();
            }
            String[] roleNames = roleNameStr.split(",");
            Set<String> roleNameSet = new HashSet<>();
            for (String roleName : roleNames) {
                roleNameSet.add(roleName);
            }
            return roleNameSet;
        } catch (ExecutionException e) {
            logger.error("Failed to get user role names from cache");
            return Collections.emptySet();
        }
    }

    public void setDataLoaderService(DataLoaderService dataLoaderService) {
        this.dataLoaderService = dataLoaderService;
    }

    public void setBaseConfig(BaseConfig baseConfig) {
        this.baseConfig = baseConfig;
    }
}
