package com.dongzy.common.config;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import com.dongzy.common.common.ProgramException;
import com.dongzy.common.common.Validate;
import com.dongzy.common.net.UrlUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

/**
 * 数据库连接配置实体
 * 数据源有两种类型
 * 1、jndi数据源
 * 2、标准数据源
 */
public class DatabaseConfigInfo implements Serializable {

    /**
     * 获取连接的最大登录时间为10秒钟
     */
    public final static int MAX_WAIT_TIME = 10_000;
    /**
     * 数据库的最大连接数量为2000个
     */
    public final static int MAX_ACTIVE = 2_000;
    /**
     * 设置当无法建立连接时尝试重新连接的时间间隔，单位毫秒，本配置为4秒钟
     */
    public final static int TIME_BETWEEN_ERROR = 5000;

    private final String jndiName;            //jndi数据源名称
    private final String driver;              //数据库驱动
    private final String url;                 //数据库地址
    private final String username;            //用户名
    private final String password;            //密码
    private final int maxtotal;               //连接池大小
    private final int maxwaitmillis;          //请求最大等待时间
    private Map<String, Set<String>> encryptMap = new CaseInsensitiveMap<>();           //加密的表和字段的范围
    private String encryptkey;                 //加密密钥

    /**
     * 根据传入的参数构造函数
     *
     * @param jndiName jndi名称
     */
    public DatabaseConfigInfo(String jndiName) {
        this.jndiName = jndiName;
        this.driver = null;
        this.url = null;
        this.username = null;
        this.password = null;
        this.maxtotal = -1;
        this.maxwaitmillis = -1;
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param url      数据库地址
     * @param username 用户名
     * @param password 密码
     */
    public DatabaseConfigInfo(String url, String username, String password) {
        this(null, url, username, password, MAX_ACTIVE, MAX_WAIT_TIME);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param driver   数据库驱动
     * @param url      数据库地址
     * @param username 用户名
     * @param password 密码
     */
    public DatabaseConfigInfo(String driver, String url, String username, String password) {
        this(driver, url, username, password, MAX_ACTIVE, MAX_WAIT_TIME);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param url           数据库地址
     * @param username      用户名
     * @param password      密码
     * @param maxtotal      连接池大小
     * @param maxwaitmillis 请求最大等待时间，单位毫秒
     */
    public DatabaseConfigInfo(String url, String username, String password, int maxtotal, int maxwaitmillis) {
        this(null, url, username, password, maxtotal, maxwaitmillis);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param driver        数据库驱动
     * @param url           数据库地址
     * @param username      用户名
     * @param password      密码
     * @param maxtotal      连接池大小
     * @param maxwaitmillis 请求最大等待时间，单位毫秒
     */
    public DatabaseConfigInfo(String driver, String url, String username, String password, int maxtotal, int maxwaitmillis) {
        Validate.notBlank(url, "数据库url不能为空！");
        Validate.notBlank(username, "数据库username不能为空！");
        Validate.notBlank(password, "数据库password不能为空！");
        Validate.isTrue(maxtotal > 0, "数据库最大连接数必须大于0！");
        Validate.isTrue(maxwaitmillis >= 1000, "数据库超时时间必须大于1000毫秒！");

        this.jndiName = null;
        this.driver = (driver == null) ? null : driver.trim();
        this.url = url.trim();
        this.username = username.trim();
        this.password = password.trim();
        this.maxtotal = maxtotal;
        this.maxwaitmillis = maxwaitmillis;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaxtotal() {
        return maxtotal;
    }

    public int getMaxwaitmillis() {
        return maxwaitmillis;
    }

    public Map<String, Set<String>> getEncryptMap() {
        return encryptMap;
    }

    public void setEncryptMap(Map<String, Set<String>> encryptMap) {
        this.encryptMap = encryptMap;
    }

    /**
     * 判断字段是否在被加密的范围内
     *
     * @param tableName  需要查询的表名
     * @param columnName 需要查询的字段
     * @return 是否包含在加密范围内
     */
    public boolean containsEncryptField(String tableName, String columnName) {
        Validate.notBlank(tableName, "表名不能为空！");
        Validate.notBlank(columnName, "字段名称不能为空");

        Set<String> columnNames = encryptMap.getOrDefault(tableName, null);
        if (columnNames != null) {
            for (String value : columnNames) {
                if (columnName.equalsIgnoreCase(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取加密密钥
     *
     * @return 加密密钥
     */
    public String getEncryptkey() {
        return encryptkey;
    }

    /**
     * 设置加密密钥
     *
     * @param encryptkey 加密密钥
     */
    public void setEncryptkey(String encryptkey) {
        this.encryptkey = encryptkey;
    }

    /**
     * 对连接字符串中的URL进行必要的处理
     *
     * @param url 需要处理的url地址
     * @return 返回处理后的url地址
     */
    public static String processUrl(String url) {
        try {
            //因为新的mysql驱动对参数有要求,所以防止错误,需要添加必要的参数
            if (url.startsWith("jdbc:mysql://")) {
                Map<String, String> paramsMap = UrlUtils.getParams(url);
                if (!paramsMap.containsKey("useSSL")) {
                    url = UrlUtils.addUrlParam(url, "useSSL", "false");
                }
                if (!paramsMap.containsKey("serverTimezone")) {
                    url = UrlUtils.addUrlParam(url, "serverTimezone", "CTT");
                }
                if (!paramsMap.containsKey("characterEncoding")) {
                    url = UrlUtils.addUrlParam(url, "characterEncoding", "UTF-8");
                }
                if (!paramsMap.containsKey("useUnicode")) {
                    url = UrlUtils.addUrlParam(url, "useUnicode", "true");
                }
            }
            return url;
        } catch (UnsupportedEncodingException e) {
            throw new ProgramException("添加mysql链接字符串参数时发生异常!", e);
        }
    }
}
