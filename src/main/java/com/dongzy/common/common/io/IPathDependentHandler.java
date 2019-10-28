package com.dongzy.common.common.io;

import java.nio.file.WatchEvent;
import java.util.List;

/**
 * 文件缓存依赖处理程序接口类，如果监视的文件夹发生变化，将会触发本接口实现类
 *
 * @author zouyong
 * @since JDK1.0
 */
public interface IPathDependentHandler {

    /**
     * 处理文件变化所需要的操作
     *
     * @param watchEvents 监视事件集合
     * @return 处理影响的数量
     */
    int process(List<WatchEvent<?>> watchEvents);

}
