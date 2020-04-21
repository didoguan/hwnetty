package com.deepspc.hwnetty.core.database;

import com.deepspc.hwnetty.core.constant.BizConstant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;

/**
 * 多数据源切换的aop
 *
 */
@Aspect
public class MultiSourceExAop implements Ordered {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut(value = "@annotation(com.deepspc.hwnetty.core.database.DataSource)")
    private void cut() {

    }

    @Around("cut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;

        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());

        DataSource datasource = currentMethod.getAnnotation(DataSource.class);
        if (datasource != null) {
            DataSourceContextHolder.setDataSourceName(datasource.value());
            log.info("设置数据源为：" + datasource.value());
        } else {
            DataSourceContextHolder.setDataSourceName(BizConstant.MASTER);
            log.info("设置数据源为：" + BizConstant.MASTER);
        }

        try {
            return point.proceed();
        } finally {
            log.info("清空数据源信息！");
            DataSourceContextHolder.clearDataSourceName();
        }
    }

    /**
     * aop的顺序要早于spring的事务
     */
    @Override
    public int getOrder() {
        return 1;
    }

}
