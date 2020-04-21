package com.deepspc.hwnetty.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.deepspc.hwnetty.core.constant.BizConstant;
import com.deepspc.hwnetty.core.database.DynamicDataSource;
import com.deepspc.hwnetty.core.database.MultiSourceExAop;
import com.deepspc.hwnetty.core.properties.DataPoolProperties;
import com.deepspc.hwnetty.core.properties.DataSourceProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {"com.deepspc.hwnetty.modular.*.mapper"})
public class DataSourceConfig {

    @Resource
    private MybatisPlusProperties mybatisPlusProperties;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid.master")
    public DataSourceProperties masterDtaSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid.slave")
    public DataSourceProperties slaveDtaSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name="masterDataSource")
    public DruidDataSource masterDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        DataPoolProperties dataPoolProperties = new DataPoolProperties();
        DataSourceProperties master = masterDtaSourceProperties();
        master.config(dataSource);
        dataPoolProperties.config(dataSource);
        return dataSource;
    }

    @Bean(name="slaveDataSource")
    public DruidDataSource slaveDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        DataPoolProperties dataPoolProperties = new DataPoolProperties();
        DataSourceProperties slave = slaveDtaSourceProperties();
        slave.config(dataSource);
        dataPoolProperties.config(dataSource);
        return dataSource;
    }

    @Bean(name="dynamicDataSource")
    public DynamicDataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = DynamicDataSource.getInstance();
        DruidDataSource master = masterDataSource();
        DruidDataSource slave = slaveDataSource();
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put(BizConstant.MASTER, master);
        hashMap.put(BizConstant.SLAVE, slave);
        dynamicDataSource.setTargetDataSources(hashMap);
        dynamicDataSource.setDefaultTargetDataSource(master);

        return dynamicDataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("dynamicDataSource") DynamicDataSource dynamicDataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dynamicDataSource);
        bean.setMapperLocations(mybatisPlusProperties.resolveMapperLocations());
        bean.setTypeAliasesPackage(mybatisPlusProperties.getTypeAliasesPackage());
        bean.setConfiguration(mybatisPlusProperties.getConfiguration());

        return bean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public MultiSourceExAop multiSourceExAop() {
        return new MultiSourceExAop();
    }

    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager(DynamicDataSource dynamicDataSource) {
        return new DataSourceTransactionManager(dynamicDataSource);
    }

}
