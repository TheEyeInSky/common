package com.dongzy.common.common.text;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

/**
 * XML命令空间定义类
 */
public class ZouyongNamespaceContext implements NamespaceContext {

    /**
     * This method returns the uri for all prefixes needed. Wherever possible
     * it uses XMLConstants.
     *
     * @param prefix
     * @return uri
     */
    public String getNamespaceURI(String prefix) {
        switch (prefix) {
            case XMLConstants.DEFAULT_NS_PREFIX:
            case "core":
                return "http://www.zouyong.org/schema/core";
            case "remote":
                return "http://www.zouyong.org/schema/remote";
            case "dbconn":
                return "http://www.zouyong.org/schema/dbconn";
            case "redis":
                return "http://www.zouyong.org/schema/redisconn";
            default:
                return getExtNamespaceURI(prefix);
        }
    }

    public String getPrefix(String namespaceURI) {
        // Not needed in this context.
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        // Not needed in this context.
        return null;
    }

    public String getExtNamespaceURI(String prefix) {
        return XMLConstants.NULL_NS_URI;
    }
}
