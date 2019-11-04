package com.dongzy.common.config;


import java.io.Serializable;

/**
 * 数据库配置项实体
 */
public class ConfigItemInfo implements Serializable {

    private String appCode;             //app编码
    private String namespace;           //命名空间
    private String key;                 //配置项的完整路径
    private String value;               //配置项的值
    private String remark;              //备注
    private boolean enabled;            //是否启用

    /**
     * 根据传入的参数构造函数
     *
     * @param appCode   app编码
     * @param namespace 命名空间
     * @param key       key值
     */
    public ConfigItemInfo(String appCode, String namespace, String key) {
        this.appCode = appCode;
        this.namespace = namespace;
        this.key = key;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
