package com.dongzy.common.mq;

import java.util.Collection;

/**
 * 消息处理结果接收接口
 *
 * @author zouyong
 * @since JDK1.6
 */
public interface QueueSetReceiver<T> {

    /**
     * 消息处理回调接口
     *
     * @param objects 回调的数据对象
     * @return 成功处理的数量，比如传入集合对象为10个，成功处理的对象数据为8个，那么返回值为8
     */
    int receive(Collection<T> objects);
}
