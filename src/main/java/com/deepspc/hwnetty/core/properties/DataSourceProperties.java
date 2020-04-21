package com.deepspc.hwnetty.core.properties;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;

@Data
public class DataSourceProperties {
    private String url;

    private String username;

    private String password;

    private String driverClassName;

    public DataSourceProperties() {

    }

    public void config(DruidDataSource dataSource) {
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setValidationQuery(getValidateQueryByUrl(url));
    }

    private String getValidateQueryByUrl(String url) {
        if (url.contains("oracle")) {
            return "select 1 from dual";
        } else if (url.contains("postgresql")) {
            return "select version()";
        } else {
            return "select 1";
        }
    }
}
