package bestan.common.lua;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 提供一个lua配置注解：指定配置文件，是否需要载入到内存
 * 
 * @author bestan
 * @date:   2018年8月2日 下午7:54:06 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaAnnotation {
	/**
	 * @return 对应lua配置的路径
	 */
	String path();
	
	/**
	 * @return 是否载入lua文件
	 */
	boolean load() default true;
}
