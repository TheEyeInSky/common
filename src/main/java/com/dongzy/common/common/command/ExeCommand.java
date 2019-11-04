package com.dongzy.common.common.command;

import com.dongzy.common.common.SystemUtils;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.io.TextFile;
import com.dongzy.common.common.io.zip.ZipUtil;
import com.dongzy.common.common.io.zip.exception.ZipException;
import com.dongzy.common.common.text.StringBuilderExt;

import java.io.*;
import java.util.Locale;

/**
 * 执行DOS命令辅助类
 */
public final class ExeCommand {

    /**
     * 标准身份运行程序
     *
     * @param cmd 需要执行的命令
     */
    public static void execAsync(String cmd) {
        final String[] command = processCmd(cmd);
        new Thread(() -> {
            try {
                Runtime rt = Runtime.getRuntime(); // 获取运行时系统
                rt.exec(command); // 执行命令
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 标准身份运行程序
     *
     * @param cmd 需要执行的命令
     * @return 命令执行返回的结果
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    public static String exec(String cmd) throws IOException, InterruptedException {
        final String[] command = processCmd(cmd);
        StringBuilderExt stringBuilderExt = new StringBuilderExt();

        Process proc = Runtime.getRuntime().exec(command); // 执行命令
        String encoding;
        switch (Locale.getDefault().getLanguage()) {
            case "zh":
                encoding = "GBK";
                break;
            default:
                encoding = "UTF-8";
                break;
        }
        try (InputStream stderr = proc.getInputStream()) { // 获取输入流
            try (InputStreamReader isr = new InputStreamReader(stderr, encoding)) {
                try (BufferedReader br = new BufferedReader(isr)) {
                    while (true) { // 打印出命令执行的结果
                        String line = br.readLine();
                        if (line != null) {
                            stringBuilderExt.appendLine(line);
                        } else {
                            break;
                        }
                    }
                    proc.waitFor();
                }
            }
        }

        return stringBuilderExt.toString();
    }

    /**
     * 将命令的执行结果写入到指定的文件中
     *
     * @param cmd  需要执行的命令
     * @param file 需要保存的文件
     */
    public static void execToFile(String cmd, File file) throws IOException, InterruptedException {
        Validate.notBlank(cmd, "命令不能为空");
        Validate.notNull(file, "文件不能为空");

        if (file.getName().toLowerCase().endsWith(".zip")) {
            final String[] command = processCmd(cmd);
            Process process = Runtime.getRuntime().exec(command); // 执行命令
            ZipUtil zipUtil = new ZipUtil(file);
            try {
                zipUtil.zip(process.getInputStream(), file.getName().substring(0, file.getName().length() - 4));
            } catch (ZipException e) {
                throw new IOException(e);
            }
            process.waitFor();
        } else {
            try (TextFile textFile = new TextFile(file)) {
                textFile.write(exec(cmd));
            }
        }
    }

    /**
     * 对原始的命令进行加工处理
     *
     * @param cmd 命令加工处理
     * @return 处理后的命令
     */
    private static String[] processCmd(String cmd) {
        String[] strings = new String[3];
        switch (SystemUtils.currentOperatorSystem()) {
            case WINDOWS:
                strings[0] = "cmd";
                strings[1] = "/c";
                strings[2] = cmd;
                break;
            case LINUX:
                strings[0] = "/bin/sh";
                strings[1] = "-c";
                strings[2] = cmd;
                break;
            default:
                break;
        }
        return strings;
    }
}
