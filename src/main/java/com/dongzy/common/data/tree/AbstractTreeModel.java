package com.dongzy.common.data.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有树状结构的抽象父类
 */
public abstract class AbstractTreeModel<T extends ITreeModel, V> implements ITreeModel<T, V> {

    private V id;
    private V parentId;
    private String name;
    private List<T> children = new ArrayList<>();

    @Override
    public List<T> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<T> children) {
        if (children != null) {
            this.children = children;
        }
    }

    @Override
    public V getId() {
        return id;
    }

    @Override
    public void setId(V id) {
        this.id = id;
    }

    @Override
    public V getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(V parentId) {
        this.parentId = parentId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
