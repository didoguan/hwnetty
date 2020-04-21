package com.deepspc.hwnetty.core.database;

import java.lang.annotation.*;

/**
 * 
 * 多数据源注解
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DataSource {

	String value() default "";
}
