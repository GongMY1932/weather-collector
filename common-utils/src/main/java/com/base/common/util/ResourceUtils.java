package com.base.common.util;

import com.alibaba.fastjson.JSONObject;
import com.base.common.annotation.EnumDict;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: gongmy
 * @Date: 2022/11/2
 * @Description: 资源加载工具类
 * @version: 1.0
 */
@Slf4j
public class ResourceUtils {

    /**
     * 枚举字典数据
     */
    private static String enumDictData = null;
    /**
     * 所有枚举java类
     */
    private final static String CLASS_ENMU_PATTERN = "/**/**/*Enum.class";
    /**
     * 包路径 com.sdsat.socs
     */
    private final static String BASE_PACKAGE = "com.sdsat";
    /**
     * 枚举类中获取字典标签的方法名
     */
    private final static String GET_LABEL = "getLabel";
    /**
     * 枚举类中获取字典值的方法名
     */
    private final static String GET_VALUE = "getValue";

    public static String getEnumDictData(String enumKey) {

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(BASE_PACKAGE) + CLASS_ENMU_PATTERN;
        try {
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            for (Resource resource : resources) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String classname = reader.getClassMetadata().getClassName();
                Class<?> clazz = Class.forName(classname);
                EnumDict enumDict = clazz.getAnnotation(EnumDict.class);
                if (enumDict != null) {
                    EnumDict annotation = clazz.getAnnotation(EnumDict.class);
                    String key = annotation.value();
                    if (key.equals(enumKey)) {
                        Object[] enumConstants = clazz.getEnumConstants();
                        List<Map<String, Object>> list = new ArrayList<>();
                        for (Object o : enumConstants) {
                            Object label = clazz.getDeclaredMethod(GET_LABEL).invoke(o);
                            Object value = clazz.getDeclaredMethod(GET_VALUE).invoke(o);
                            Map<String, Object> map = new HashMap<>();
                            map.put("label", label);
                            map.put("value", value);
                            list.add(map);
                        }
                        enumDictData = (JSONObject.toJSONString(list));
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取枚举类字典数据异常:{}", e.getMessage());
        }
        return enumDictData;
    }


}
