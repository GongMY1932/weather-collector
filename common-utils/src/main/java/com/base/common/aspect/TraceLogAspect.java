//package com.base.common.aspect;
//
//import com.alibaba.fastjson.JSON;
//import com.base.common.annotation.AutoLog;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.servlet.http.HttpServletRequest;
//import java.lang.reflect.Method;
//import java.util.Deque;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ConcurrentLinkedDeque;
//
///**
// * 功能描述：记录SpringBean方法的调用日志
// *
// * @Author: ZH
// * @Date: 2020/7/14 14:26
// */
//@Aspect
//@Component
//@Slf4j
//@ConditionalOnProperty(name = "sys.tracelog.enabled", havingValue = "true", matchIfMissing = false)
//public class TraceLogAspect {
//
//    //	当前线程副本
//    private static final ThreadLocal<Deque<MethodInfo>> threadLocal = new ThreadLocal<Deque<MethodInfo>>() {
//        protected synchronized Deque<MethodInfo> initialValue() {
//            return new ConcurrentLinkedDeque<MethodInfo>();
//        }
//    };
//
//    /**
//     * 定义切点
//     */
//    @Pointcut("@annotation(com.sdsat.common.annotation.AutoLog)")
//    public void log() {
//    }
//
//    /**
//     * 处理请求前处理
//     *
//     * @param joinPoint 连接点
//     */
//    @Before("log()")
//    public void doBefore(JoinPoint joinPoint) {
//        StringBuilder sb = new StringBuilder();
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if (servletRequestAttributes == null) {
//            return;
//        }
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//        String[] parameterNames = methodSignature.getParameterNames();
//        Object[] args = joinPoint.getArgs();
//
//        String methodName = request.getMethod();
//        Class<?>[] argTypes = new Class[joinPoint.getArgs().length];
//        for (int i = 0; i < args.length; i++) {
//            argTypes[i] = args[i].getClass();
//        }
//        Method method = null;
//        String interDesc = null;
//        try {
//            method = joinPoint.getTarget().getClass().getMethod(joinPoint.getSignature().getName(), argTypes);
//        } catch (NoSuchMethodException | SecurityException e) {
//            e.printStackTrace();
//        }
//        if (method == null) {
//            return;
//        }
//        AutoLog apiOperation = method.getAnnotation(AutoLog.class);
//        if (!StringUtils.isEmpty(apiOperation)) {
//            interDesc = apiOperation.value();
//        }
//        MethodInfo methodInfo = new MethodInfo();
//        long currentTimeMillis = System.currentTimeMillis();
//        methodInfo.setInterDesc(interDesc);
//        methodInfo.setTimeThreadLocal(currentTimeMillis);
//        threadLocal.get().add(methodInfo);
//
//        Map<String, Object> params = new HashMap<>();
//        for (int i = 0; i < parameterNames.length; i++) {
//            String parameterName = parameterNames[i];
//            Object parameterValue = args[i];
//            if (parameterValue instanceof MultipartFile) {
//                Map<String, Object> f = getFileParam((MultipartFile) parameterValue);
//                params.put(parameterName, f);
//                continue;
//            }
//            params.put(parameterName, parameterValue);
//        }
//        sb.append("Methods-Start: ").append(interDesc).append(" ");  //方法开始+时间戳
//        sb.append(" [IP : ").append(request.getRemoteAddr()).append("]");  //调用者IP
//        sb.append(" [HTTP_METHOD : ").append(methodName).append("]");  //方法全路径
//        sb.append(" [CLASS_METHOD : ").append(joinPoint.getSignature().getDeclaringTypeName()).append(".").append(joinPoint.getSignature().getName()).append("]");
//        String paramsStr = JSON.toJSONString(params);
//        if (!StringUtils.isEmpty(paramsStr) && paramsStr.length() > 300) {
//            paramsStr = paramsStr.substring(0, 297).concat("...");
//        }
//        sb.append(" [ARGS :").append("=>").append(paramsStr).append("]");  //方法参数
//        log.info(sb.toString());
//    }
//
//    /**
//     * 处理请求后返回
//     *
//     * @param obj 返回值
//     */
//    @AfterReturning(pointcut = "log()", returning = "obj")
//    public void afterReturning(Object obj) {
//        Deque<MethodInfo> deque = threadLocal.get();
//        if (deque == null) {
//            return;
//        }
//        MethodInfo methodInfo = deque.pop();
//        long consuming = System.currentTimeMillis() - methodInfo.getTimeThreadLocal();
//        log.info("Methods-End " + methodInfo.getInterDesc() + " 耗时[" + consuming + "] => " + JSON.toJSONString(obj));
//    }
//
//    private Map<String, Object> getFileParam(MultipartFile file) {
//        Map<String, Object> params = new HashMap<>();
//        params.put("文件名", file.getOriginalFilename());
//        params.put("文件类型", file.getContentType());
//        return params;
//    }
//
//    @Data
//    private static class MethodInfo {
//        private Long timeThreadLocal;
//        private String interDesc;
//    }
//
//}
