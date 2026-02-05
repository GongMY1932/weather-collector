package com.base.common.util;

import com.base.common.exception.MyException;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能描述：
 *
 * @Author: shigf
 * @Date: 2020/8/4 18:57
 */
@Slf4j
public class HttpRequestUtils {

    /**
     * get请求
     *
     * @param url
     * @return
     */
    public static JSONObject doGetJson(String url) {
        log.debug("get request 请求地址:[{}]", url);
        JSONObject jsonObject = null;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(url);
            //构建超时等配置信息
//            RequestConfig config = RequestConfig.custom().setConnectTimeout(10000) //连接超时时间
//                    .setConnectionRequestTimeout(1000) //从连接池中取的连接的最长时间
//                    .setSocketTimeout(10 *1000) //数据传输的超时时间
//                    .setStaleConnectionCheckEnabled(true) //提交请求前测试连接是否可用
//                    .build();
            //设置请求配置时间
//            httpGet.setConfig(config);
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");
                jsonObject = JSONObject.fromObject(result);
            }
            httpGet.releaseConnection();
        } catch (IOException e) {
            log.error("Get请求失败! URL=[{}]", url);
            throw new MyException(e.getMessage());
        }
        return jsonObject;
    }

    /**
     * get请求
     *
     * @param url
     * @return
     */
    public static String doGetJson(String url, String token) {
        log.info("发起GET请求 请求地址:[url = {},token = {}]", url, token);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        try {
            httpGet.addHeader("X-Access-Token", token);
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");
                log.info("GET请求结果[result = {}]", result);
                return result;
            }
        } catch (IOException e) {
            log.error("Get请求失败! URL=[{}]", url);
            throw new MyException(e.getMessage());
        } finally {
            httpGet.releaseConnection();
        }
        return null;
    }

    /**
     * 预支付发送Post请求返回Json
     *
     * @param url
     * @param map
     * @return
     */
    public static JSONObject doPostMap(String url, Map<String, Object> map, String contentType) {
        log.debug("Post request 请求地址:[{}],参数 :[{}]", url, map);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        }
        JSONObject jsonObject = null;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(url);

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            httpPost.setHeader("Content-type", contentType);
            HttpResponse response = client.execute(httpPost);
            org.apache.http.HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");
                log.debug("Post response 请求地址:[{}],返回值 :[{}]", url, result);
                jsonObject = JSONObject.fromObject(result);
            }
            httpPost.releaseConnection();
        } catch (IOException e) {
            log.error("Post请求失败! URL=[{}]", url);
            throw new MyException(e.getMessage());
        }
        return jsonObject;
    }

    /**
     * json格式返回JsonObject的post请求
     *
     * @param url
     * @param jsonParam
     * @return
     */
    public static JSONObject doPostJson(String url, JSONObject jsonParam, String contentType, String token) {
        HttpPost httpPost = new HttpPost(url);
        //超时时间等参数
//        RequestConfig defaultRequestConfig = RequestConfig.custom()
//                .setSocketTimeout(20000)
//                .setConnectTimeout(20000)
//                .setConnectionRequestTimeout(20000)
//                .setStaleConnectionCheckEnabled(true)
//                .build();
        CloseableHttpClient client = HttpClients.custom().build();
        //json方式
        StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");//解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType(contentType);
        httpPost.setEntity(entity);
        httpPost.setHeader("X-Access-Token", token);
        JSONObject jsonObject = null;
        try {
            HttpResponse response = client.execute(httpPost);
            org.apache.http.HttpEntity resentity = response.getEntity();
            if (resentity != null) {
                String result = EntityUtils.toString(resentity, "UTF-8");
                log.debug("Post response 请求地址:[{}],返回值 :[{}]", url, result);
                jsonObject = JSONObject.fromObject(result);
            }
        } catch (IOException e) {
            System.out.println("请求失败! URL=" + url);
            throw new MyException(e.getMessage());
        } finally {
            httpPost.releaseConnection();
        }
        return jsonObject;
    }

    public static String getString(JSONObject json, String key) {
        return json.getString(key);
    }

    /**
     * 获取data数据包
     *
     * @param json
     * @return
     */
    public static JSONObject getData(JSONObject json, String key) {
        return JSONObject.fromObject(getString(json, key));
    }

    /**
     * 是否成功
     *
     * @param json
     * @return
     */
    public static boolean isSuccess(JSONObject json) {
        return "200".equals(json.getString("code"));
    }
}
