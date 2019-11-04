package com.dongzy.common.thread;

import com.dongzy.common.common.SystemUtils;
import com.dongzy.common.common.command.ExeCommand;

/**
 * 系统进程管理工具
 */
public class ProcessUtils {

    /**
     * 强制终止指定名称的进程
     *
     * @param processName 进程的名称
     */
    public static void killall(String processName) {
        switch (SystemUtils.currentOperatorSystem()) {
            case WINDOWS:
                windowsKill(processName);
                break;
            case MACOS:
                macosKill(processName);
                break;
            case LINUX:
                linuxKill(processName);
                break;
            case OTHER:
                throw new RuntimeException("无法支持当前操作系统的终止进程操作！");
        }
    }

    private static void windowsKill(String processName) {
        String cmd = "taskkill /f /im " + processName + " /t";
        try {
            ExeCommand.execAsync(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void macosKill(String processName) {
        String cmd = "killall " + processName;
        try {
            ExeCommand.execAsync(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void linuxKill(String processName) {
        String cmd = "killall " + processName;
        try {
            ExeCommand.execAsync(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
