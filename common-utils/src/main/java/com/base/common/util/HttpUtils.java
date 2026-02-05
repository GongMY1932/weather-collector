package com.base.common.util;

import com.base.common.exception.MyException;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

@Slf4j
public class HttpUtils {

    /**
     * 发送 GET 请求（支持请求头和URL参数）
     *
     * @param url     请求地址
     * @param params  URL参数 Map
     * @param headers 请求头 Map
     * @return 响应内容
     * @throws IOException IO异常
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        // 构建带参数的URL，参数做URL编码
        if (params != null && !params.isEmpty()) {
            String paramsStr = buildUrlParams(params);
            url += (url.contains("?") ? "&" : "?") + paramsStr;
        }
        HttpURLConnection conn = null;
        try {
            URL myUrl = new URL(url);
            conn = (HttpURLConnection) myUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(30000); // 连接超时 30秒
            conn.setReadTimeout(30000);    // 读取超时 30秒

            // 设置默认请求头
            conn.setRequestProperty("Accept", "application/json, */*;q=0.8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate"); // 支持Gzip压缩
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java)");

            // 设置自定义请求头（会覆盖同名的默认请求头）
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // 获取响应码
            int responseCode = conn.getResponseCode();
            log.info("GET请求响应码: {}, URL: {}", responseCode, url);
            // 正确处理输入流/错误流
            InputStream inputStream = null;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }
            // 无任何流可读取，直接返回空
            if (inputStream == null) {
                log.warn("GET请求无响应流，URL: {}", url);
                return "";
            }
            // 检查响应是否使用Gzip压缩
            String contentEncoding = conn.getContentEncoding();
            boolean isGzip = contentEncoding != null && contentEncoding.toLowerCase().contains("gzip");
            log.info("Content-Encoding: {}, 是否Gzip压缩: {}", contentEncoding, isGzip);

            // try-with-resources 自动关闭流，零泄漏
            try (InputStream rawStream = inputStream;
                 InputStream decompressedStream = isGzip ? new GZIPInputStream(rawStream) : rawStream;
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = decompressedStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] responseBytes = outputStream.toByteArray();

                // JSON响应强制使用UTF-8编码
                String charset = StandardCharsets.UTF_8.name();
                String contentType = conn.getContentType();
                log.info("Content-Type: {}", contentType);

                // 如果Content-Type明确指定了charset，则使用指定的编码（但JSON通常都是UTF-8）
                if (contentType != null && contentType.contains("charset")) {
                    String[] parts = contentType.split(";");
                    for (String part : parts) {
                        part = part.trim();
                        if (part.toLowerCase().startsWith("charset=")) {
                            String detectedCharset = part.substring("charset=".length()).trim();
                            // 移除可能的引号
                            if (detectedCharset.startsWith("\"") && detectedCharset.endsWith("\"")) {
                                detectedCharset = detectedCharset.substring(1, detectedCharset.length() - 1);
                            }
                            // 对于JSON，即使检测到其他编码，也优先使用UTF-8
                            if (contentType.contains("application/json")) {
                                charset = StandardCharsets.UTF_8.name();
                                log.info("JSON响应，强制使用UTF-8编码");
                            } else {
                                charset = detectedCharset;
                            }
                            break;
                        }
                    }
                }

                log.info("使用字符编码: {}, 响应字节长度: {} (压缩前: {})", 
                        charset, responseBytes.length, isGzip ? "已解压" : "未压缩");
                
                // 最终转换，使用UTF-8编码（JSON标准编码）
                String result = new String(responseBytes, charset);
                log.info("GET请求完成，响应字符串长度: {} 字符", result.length());
                return result;
            }

        } catch (Exception e) {
            log.error("GET请求失败，URL: {}", url, e);
            throw new IOException("GET请求失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String buildUrlParams(Map<String, String> params) {
        Set<String> keySet = params.keySet();
        List<String> keyList = new ArrayList<String>(keySet);
        Collections.sort(keyList);
        StringBuffer sb = new StringBuffer("");
        for (String key : keyList) {
            String value = params.get(key);
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            sb.append(key);
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(params.get(key), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            sb.append("&");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 发送 POST 请求
     *
     * @param url     请求地址
     * @param body    请求体（JSON 字符串）
     * @param headers 请求头 Map
     * @return 响应内容
     * @throws IOException IO异常
     */
    public static String post(String url, String body, Map<String, String> headers) throws IOException {
        HttpURLConnection connection = null;
        OutputStreamWriter writer = null;
        BufferedReader reader = null;

        try {
            // 创建连接
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(30000); // 连接超时 30秒
            connection.setReadTimeout(30000);    // 读取超时 30秒

            // 设置默认请求头
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "Keep-Alive");

            // 设置自定义请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 发送请求体
            if (body != null && !body.isEmpty()) {
                writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                writer.write(body);
                writer.flush();
            }

            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
            } else {
                reader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)
                );
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();

        } finally {
            // 关闭资源
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 发送 POST 请求（无自定义 Header）
     *
     * @param url  请求地址
     * @param body 请求体（JSON 字符串）
     * @return 响应内容
     * @throws IOException IO异常
     */
    public static String post(String url, String body) throws IOException {
        return post(url, body, null);
    }

    /**
     * 发送 POST 请求（使用 Map 构建 Header）
     *
     * @param url     请求地址
     * @param body    请求体（JSON 字符串）
     * @param headers 请求头 Map
     * @return 响应内容
     * @throws IOException IO异常
     */
    public static String postWithHeaders(String url, String body, Map<String, String> headers) throws IOException {
        return post(url, body, headers);
    }


}
