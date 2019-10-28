package com.dongzy.common.common.text;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * xml文件处理工具类
 */
public class XmlNodeUtils {

    //解析xpath的类
    private final XPath xPath;

    public static XmlNodeUtils createXmlNodeUtils() {
        return new XmlNodeUtils(null);
    }

    public static XmlNodeUtils createXmlNodeUtils(NamespaceContext namespaceContext) {
        return new XmlNodeUtils(namespaceContext);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param namespaceContext 命令空间定义函数
     */
    private XmlNodeUtils(NamespaceContext namespaceContext) {
        xPath = XPathFactory.newInstance().newXPath();
        if (namespaceContext != null) {
            xPath.setNamespaceContext(namespaceContext);
        }
    }

    /**
     * 获取符合表达式的节点集合
     *
     * @param xpath    xpath表达式
     * @param document 需要查询的xml文档
     */
    public NodeList getNodes(String xpath, Document document) throws XPathExpressionException {
        return (NodeList) xPath.evaluate(xpath, document, XPathConstants.NODESET);
    }

    /**
     * 获取符合表达式的节点集合
     *
     * @param xpath    xpath表达式
     * @param document 需要查询的xml文档
     */
    public List<Element> getElements(String xpath, Document document) throws XPathExpressionException {
        NodeList nodeList = getNodes(xpath, document);
        List<Element> nodes = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add((Element) nodeList.item(i));
        }
        return nodes;
    }

    /**
     * 获取符合表达式的第一个节点
     *
     * @param xpath    xpath表达式
     * @param document 需要查询的xml文档
     */
    public Node getSingleNode(String xpath, Document document) throws XPathExpressionException {
        NodeList nodeList = getNodes(xpath, document);
        return (nodeList.getLength() > 0) ? nodeList.item(0) : null;
    }

    /**
     * 获取符合表达式的第一个节点
     *
     * @param xpath    xpath表达式
     * @param document 需要查询的xml文档
     */
    public Element getSingleElement(String xpath, Document document) throws XPathExpressionException {
        NodeList nodeList = getNodes(xpath, document);
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                return (Element) nodeList.item(i);
            }
        }
        return null;
    }

    /**
     * 获取当前节点的所有子节点
     *
     * @param node 当前节点
     */
    public List<Element> getChildElements(Node node) {
        NodeList nodeList = node.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            if (subNode instanceof Element) {
                elements.add((Element) subNode);
            }
        }
        return elements;
    }

    /**
     * 获取当前节点自定名称的子节点集合
     *
     * @param node          当前的节点
     * @param childNodeName 子节点名称
     */
    public List<Element> getChildElements(Node node, String childNodeName) {
        NodeList nodeList = node.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            if (subNode instanceof Element) {
                Element element = (Element) subNode;
                if (Objects.equals(element.getLocalName(), childNodeName)) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }

    /**
     * 获取当前节点自定名称的子节点
     *
     * @param node          当前的节点
     * @param childNodeName 子节点名称
     */
    public Element getSingleChildElement(Node node, String childNodeName) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            if (subNode instanceof Element) {
                Element element = (Element) subNode;
                if (Objects.equals(element.getLocalName(), childNodeName)) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * 清空当前节点内容
     *
     * @param element 需要清楚内容的节点
     */
    public void clear(Element element) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            element.removeChild(nodeList.item(i));
        }
    }

}
