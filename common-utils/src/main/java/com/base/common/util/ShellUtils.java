package com.base.common.util;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * @ClassName ShellUtils
 * @Description linux系统内命令行执行工具
 * @Author ZH
 * @Date 2021/6/17 17:21
 * @Version 1.0
 */
@Slf4j
public class ShellUtils {
    /**
     * 调用脚本
     *
     * @param shell
     * @param outFilePath
     * @param args
     * @param evnpsMap
     * @param workspace
     * @throws IOException
     */
    public static String callShell(String shell, String outFilePath, String[] args, Map<String, String> evnpsMap, String workspace) {
        StringBuilder cmd = new StringBuilder(shell);
        for (String arg : args) {
            cmd.append(" ").append(arg);
        }
        ArrayList<String> evnpsList = new ArrayList<>();
        if (evnpsMap != null) {
            for (Map.Entry<String, String> entry : evnpsMap.entrySet()) {
                evnpsList.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        File dir = new File(workspace);
        BufferedWriter output = null;
        if (StringUtils.isNotBlank(outFilePath)) {
            File outFile = new File(outFilePath);
            try {
                output = new BufferedWriter(new FileWriter(outFile));
            } catch (IOException e) {
                e.printStackTrace();
                output = null;
            }
        }
        BufferedReader input = null;
        StringBuilder resultString = new StringBuilder();
        try {
            log.info("执行命令[cmd = {}]", cmd.toString());
            Process process = Runtime.getRuntime().exec(cmd.toString(), evnpsList.toArray(new String[0]), dir);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = input.readLine()) != null) {
                log.info(line);
                if (output != null) {
                    output.append(line).append("\r\n");
                }
                resultString.append(line).append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            AssertUtils.throwTrue(true, e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultString.toString();
    }

    /**
     * 调用脚本
     *
     * @param shell
     * @param outFilePath
     * @param args
     * @param evnps
     * @param workspace
     * @throws IOException
     */
    public static String callShellProcess(String shell, String outFilePath, String[] args, Map<String, String> evnps, String workspace) {
        String[] shellArgs = new String[]{shell};
        String[] commands = ArrayUtil.addAll(shellArgs, args);
        ProcessBuilder pb = new ProcessBuilder(commands);
        //设置环境变量
        if (evnps != null) {
            pb.environment().putAll(evnps);
        }
        if (StringUtils.isNotBlank(workspace)) {
            File dir = new File(workspace);
            pb.directory(dir);
        }
        if (StringUtils.isNotBlank(outFilePath)) {
            File outFile = new File(outFilePath);
            pb.redirectOutput(ProcessBuilder.Redirect.to(outFile));
        }
        try {
            log.info("执行命令[cmd = {}]", (Object) commands);
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            AssertUtils.throwTrue(true, e.getMessage());
        }
        return JSON.toJSONString(commands) + "执行完成";
    }

}
