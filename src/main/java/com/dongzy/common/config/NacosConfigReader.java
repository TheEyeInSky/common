package com.dongzy.common.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * 采用nacos作为实现层的共享配置访问基础类
 */
public class NacosConfigReader implements IConfigReader {

    private final static int DEFAULT_TIME_OUT = 3_000;      //默认超时时间为3秒钟
    private static ConfigService configService;

    @Override
    public Collection<ConfigItemInfo> getConfigItems(String namespace, String appCode) {
        ConfigService configService = getConfigService();
        Collection<ConfigItemInfo> configItemInfoes;
        try {
            String string = configService.getConfig(appCode, namespace, DEFAULT_TIME_OUT);
            Properties properties = new Properties();
            properties.load(new StringReader(string));

            configItemInfoes = new ArrayList<>(properties.size());
            for (String name : properties.stringPropertyNames()) {
                ConfigItemInfo info = new ConfigItemInfo(appCode, namespace, name);
                info.setValue(properties.getProperty(name));
                configItemInfoes.add(info);
            }
        } catch (NacosException | IOException e) {
            configItemInfoes = new ArrayList<>();
        }
        return configItemInfoes;
    }

    //获取读取nacos配置的类
    private synchronized static ConfigService getConfigService() {
        if (configService == null) {
            try {
                Properties properties = new Properties();
                String address = String.format("%s:%s", SystemConfig.getConfig().getValue(SystemConfig.NACOS_SERVER_HOST),
                        SystemConfig.getConfig().getValue(SystemConfig.NACOS_SERVER_PORT));
                properties.put("serverAddr", address);
                configService = NacosFactory.createConfigService(properties);
            } catch (NacosException e) {
                configService = null;
                e.printStackTrace();
            }
        }
        return configService;
    }

}
