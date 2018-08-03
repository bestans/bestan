package bestan.common.lua;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * lua配置的参数所用注解
 * @author bestan
 * @date:   2018年8月2日 下午8:44:00 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaParamAnnotation {
	/**
	 * @return 是否可选配置项，true表示lua中配置项不一定有
	 */
	boolean optional() default false;
}
