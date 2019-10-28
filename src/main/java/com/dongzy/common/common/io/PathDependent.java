package com.dongzy.common.common.io;

import org.slf4j.Logger;
import com.gee4j.log.TextLoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 路径依赖类，如果此路径中的内容发生了变化，那么将触发注册的监听程序
 * 用途：监控一些核心配置的变化，如果配置文件被修改，系统能够自动重新加载新的配置
 * 可以添加多个监控的路径，也可以添加多个处理程序。
 * 当路径内容发生变化时，所有的处理程序都将被触发
 * <p>
 * 执行strart方法启动监控，执行close方法，可以停止监控
 *
 * @author zouyong
 * @since JDK1.6
 */
public final class PathDependent {

    private final static Logger LOGGER = TextLoggerFactory.getInstance().getLogger(PathDependent.class);
    private final static PathDependent pathDependent = new PathDependent();

    private final Collection<Path> dependentPath;       //依赖的路径集合
    private final Collection<IPathDependentHandler> handlers;
    private WatchService watchService;
    private AtomicInteger processCount = new AtomicInteger();
    private volatile boolean isStart = false;

    /**
     * 获取当前类的实例
     */
    public static PathDependent getInstance() {
        return pathDependent;
    }

    /**
     * 获取累计处理的次数
     *
     * @return 累计处理次数
     */
    public int getProcessCount() {
        return processCount.get();
    }

    /**
     * 默认构造器
     */
    public PathDependent() {
        dependentPath = new ArrayList<>();
        handlers = new ArrayList<>();
    }

    /**
     * 添加需要监控的路径
     *
     * @param fullPath 需要监控的路径
     */
    public void addPath(String fullPath) {
        addPath(Paths.get(fullPath));
    }

    /**
     * 需要监控的资源文件，系统会自动查找资源文件所在的路径
     *
     * @param resourceName 资源的文件名
     */
    public void addResource(String resourceName) throws FileNotFoundException {
        URL url = new ClassLoaderWrapper().getResource(resourceName);
        final Path path = PathUtils.fromUrl(url).getParent();
        dependentPath.add(path);
    }

    /**
     * 添加需要监控的path对象
     *
     * @param path path对象
     */
    public void addPath(Path path) {
        if (!dependentPath.contains(path)) {
            //如果是文件，那么监控文件所在的目录
            Path newPath = (path.toFile().isDirectory()) ? path : path.getParent();
            dependentPath.add(newPath);
        }
    }

    /**
     * 添加事件处理程序，如果目录内容发生变化，会触发处理程序
     * 我们可以添加若干个处理程序，所有处理程序都将被触发执行
     *
     * @param handler 变化事件处理程序
     */
    public void addPathDependentHandler(IPathDependentHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }

    /**
     * 开始监控路径，如果内容发生变化，那么将触发处理程序
     */
    public void start() {

        if (isStart) {
            return;
        }
        if (handlers.isEmpty() || dependentPath.isEmpty()) {
            return;
        }
        isStart = true;

        try {
            if (watchService != null) {
                watchService.close();
            }
            watchService = FileSystems.getDefault().newWatchService();

            new Thread() {
                @Override
                public void run() {
                    for (Path path : dependentPath) {
                        try {
                            path.register(watchService,
                                    StandardWatchEventKinds.ENTRY_MODIFY,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_DELETE);
                        } catch (Exception ex) {
                            LOGGER.error("注册监控路径时发生异常！", ex);
                        }
                    }

                    try {
                        while (true) {
                            WatchKey key = watchService.take();
                            List<WatchEvent<?>> events = key.pollEvents();
                            if (events.size() > 0) {
                                for (IPathDependentHandler handler : handlers) {
                                    processCount.getAndAdd(handler.process(events));
                                }
                            }
                            if (!key.reset()) {
                                break;
                            }
                            Thread.yield();
                        }
                    } catch (Exception ex) {
                        LOGGER.error("", ex);
                    }
                }
            }.start();
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
    }

    /**
     * 通知监控路径的变化
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (watchService != null) {
            watchService.close();
        }
        isStart = false;
    }
}
