package com.dongzy.common.config;

import com.dongzy.common.common.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Objects;

/**
 * 提供远程对象访问的服务的站点配置实体
 * 包括服务器ip和port信息
 * 本配置适用于远程访问的客户端程序使用
 */
public class NacosConfigInfo implements Serializable {

    //服务主机或IP地址
    private String host;
    //提供服务的端口号
    private int port;
    //远程接口的服务版本信息
    private String version;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NacosConfigInfo)) {
            return false;
        }
        NacosConfigInfo entity = (NacosConfigInfo) obj;
        return Objects.equals(entity.getHost(), getHost()) && Objects.equals(entity.getPort(), getPort()) && Objects.equals(entity.getVersion(), getVersion());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
