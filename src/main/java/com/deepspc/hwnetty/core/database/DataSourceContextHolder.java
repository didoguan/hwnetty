package com.deepspc.hwnetty.core.database;

/**
 * 数据源上下文
 */
public class DataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    /**
     * 设置数据源名称
     * @param dataSourceName
     */
    public static void setDataSourceName(String dataSourceName) {
        contextHolder.set(dataSourceName);
    }

    /**
     * 获取数据源名称
     * @return
     */
    public static String getDataSourceName() {
        return contextHolder.get();
    }

    /**
     * 清除数据源
     */
    public static void clearDataSourceName() {
        contextHolder.remove();
    }
}
