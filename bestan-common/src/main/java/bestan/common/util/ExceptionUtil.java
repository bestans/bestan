package bestan.common.util;

import org.apache.commons.mail.HtmlEmail;

/**
 * @author yeyouhuan
 *
 */
public class ExceptionUtil {
	public static String getLog(Throwable e) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("[[message=").append(e.getMessage());
		stringBuilder.append(",trace=");
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement traceElement : trace)
            stringBuilder.append("\n\tat ").append(traceElement);
        
        stringBuilder.append("]]");
		return stringBuilder.toString();
	}

	//邮箱验证码
	public static boolean sendEmail(String emailaddress,String code){
		try {
			HtmlEmail email = new HtmlEmail();//不用更改
			email.setHostName("127.0.0.1");//需要修改，126邮箱为smtp.126.com,163邮箱为163.smtp.com，QQ为smtp.qq.com
			email.setSmtpPort(2525);
			email.setCharset("UTF-8");
			email.addTo(emailaddress);// 收件地址
 
			email.setFrom("youhuanye@163.com", "aa");//此处填邮箱地址和用户名,用户名可以任意填写
 
			email.setAuthentication("youhuanye@163.com", "tianya1988188"); //此处填写邮箱地址和客户端授权码
 
			email.setSubject("孙大大通讯");//此处填写邮件名，邮件名可任意填写
			email.setMsg("尊敬的用户您好,您本次注册的验证码是:" + code);//此处填写邮件内容
 
			email.send();
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
}
