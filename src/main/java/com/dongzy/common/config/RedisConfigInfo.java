package com.dongzy.common.config;

import java.io.Serializable;

/**
 * 数据库连接配置实体
 */
public class RedisConfigInfo implements Serializable {

    private String host;                //redis服务器ip或主机名
    private int port = 6379;            //redis端口
    private int index = 0;              //redis默认数据库编号
    private String password;            //redis访问密码

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
