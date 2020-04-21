package com.deepspc.hwnetty.core.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    private static DynamicDataSource dynamicDataSource;

    private DynamicDataSource() {

    }

    public static DynamicDataSource getInstance() {
        if (null == dynamicDataSource) {
            synchronized(DynamicDataSource.class) {
                dynamicDataSource = new DynamicDataSource();
                return dynamicDataSource;
            }
        }
        return dynamicDataSource;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceName();
    }
}
