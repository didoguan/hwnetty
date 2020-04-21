package com.deepspc.hwnetty.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.deepspc.hwnetty.core.properties.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Mybatis插件配置
 */
@Configuration
public class MybatisPluginConfiguration {

    @Resource
    private DataSourceProperties masterDtaSourceProperties;

    /**
     * 分页插件
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        if (masterDtaSourceProperties.getUrl().contains("oracle")) {
            paginationInterceptor.setDialectType(DbType.ORACLE.getDb());
        } else if (masterDtaSourceProperties.getUrl().contains("postgresql")) {
            paginationInterceptor.setDialectType(DbType.POSTGRE_SQL.getDb());
        } else if (masterDtaSourceProperties.getUrl().contains("sqlserver")) {
            paginationInterceptor.setDialectType(DbType.SQL_SERVER2005.getDb());
        } else {
            paginationInterceptor.setDialectType(DbType.MYSQL.getDb());
        }
        return paginationInterceptor;
    }

    /**
     * 乐观锁
     * @return
     */
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }
}
