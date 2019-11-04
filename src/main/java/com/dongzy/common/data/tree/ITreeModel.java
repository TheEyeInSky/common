package com.dongzy.common.data.tree;

import java.util.List;

public interface ITreeModel<T extends ITreeModel, V> {

    /**
     * 获取所有的子节点
     *
     * @return 子节点集合
     */
    List<T> getChildren();

    /**
     * 设置子节点
     *
     * @param children 子节点集合
     */
    void setChildren(List<T> children);

    /**
     * 获取Id
     *
     * @return Id
     */
    V getId();

    /**
     * 设置Id
     *
     * @param id Id
     */
    void setId(V id);

    /**
     * 获取父Id
     *
     * @return 父Id
     */
    V getParentId();

    /**
     * 设置父Id
     *
     * @param parentId 父Id
     */
    void setParentId(V parentId);

    /**
     * 获取节点名称
     *
     * @return 节点名称
     */
    String getName();

    /**
     * 设置节点名称
     *
     * @param name 节点名称
     */
    void setName(String name);
}
