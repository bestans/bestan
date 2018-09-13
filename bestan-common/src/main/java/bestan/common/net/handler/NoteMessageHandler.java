package bestan.common.net.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * messageHandler注解
 * @author yeyouhuan
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NoteMessageHandler {
	/**
	 * @return 如果是true，表示该handle废弃不使用
	 */
	boolean discard() default false;
	
	/**
	 * @return 如果不是空，那么handle将与该message建立映射关系，<p>
	 * 			否则根据handle名字映射message,<p>
	 * 			如aabbccHandle对应处理的消息为aabbcc
	 */
	String messageName() default "";
}
