package bestan.common.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import bestan.common.util.Global;

public class DateValidate {
	public static final int minuteseconds = 60;
	public static final long secondMills = 1000;
	public static final long minuteMills = secondMills * minuteseconds;
	public static final long hourMills = minuteMills * minuteseconds;
	public static final long dayMills = hourMills * 24;
	public static final long weekMills = dayMills * 7;
	public static final long hourSeconds = minuteseconds * minuteseconds;
	public static final long daySeconds = hourSeconds * 24;
	
	// the time zone offset
	public static final long TIME_ZONE_OFFSET = Calendar.getInstance().getTimeZone().getRawOffset();
	
	// the time of 1970.1.1 is the thrusday in java, we need add the before three days
	public static final long addMills = dayMills * 3;
	
	public static final int YMDHms = 1;
	public static final int WHms = 2;
	public static final int Hms = 3;
	public static final int SwapTime = 4;
	public static final String HmsZeroStr = "00:00:00";
	
	/**
	 * get the days from 1970.1.1
	 * 
	 * @param time long
	 * @return days
	 */
	public static int getCurrentDay(long time) {

		return (int) ((time + TIME_ZONE_OFFSET) / dayMills);
	}

	/**
	 * get the weeks from 1970.1.1
	 * 
	 * @param time long
	 * @return weeks
	 */
	public static int getCurrentWeek(long time) {

		return (int) ((time + TIME_ZONE_OFFSET + addMills) / weekMills) + 1;
	}
	
	/**
	 * get the between days
	 * 
	 * @param firstT long
	 * @param secondT long
	 * 
	 * @return betweendays
	 */
	public static int getDaysBetween1And2(long firstT,long secondT){
		return Math.abs(getCurrentDay(firstT) - getCurrentDay(secondT));
	}
	
	/**
	 * is the same day
	 * 
	 * @param firstT mills
	 * @param secondT mills
	 * 
	 * @return true is the same day
	 */
	public static boolean isTheSameDayByMillSecond(long firstT,long secondT) {
		return getCurrentDay(firstT) == getCurrentDay(secondT);
	}
	
	/**
	 * is the same day
	 * 
	 * @param firstT second
	 * @param secondT second
	 * 
	 * @return true is the same day
	 */
	public static boolean isTheSameDayBySecond(int firstT, int secondT){
		return getCurrentDay(firstT * secondMills) == getCurrentDay(secondT * secondMills);
	}
	
	/**
	 * is the same day
	 * 
	 * @param firstT second
	 * @param secondT second
	 * 
	 * @return true is the same day
	 */
	public static boolean isTheSameDayBySecond(int firstT, int secondT, int deltaSecond){
		return isTheSameDayBySecond(firstT + deltaSecond, secondT + deltaSecond);
	}
	
	/**
	 * is the same week
	 * 
	 * @param firstT
	 * @param secondT
	 * 
	 * @return true is the same week
	 */
	public static boolean isTheSameWeek(long firstT,long secondT) {
		if (getCurrentWeek(firstT) == getCurrentWeek(secondT)) {
			return true;
		}
		return false;
	}
	
	/**
	 * get the day of time in 23:59:59
	 * 
	 * @return time Calendar
	 */
	public static Calendar getLastSecondCalendar(){
		Calendar current = Calendar.getInstance();
		current.set(Calendar.HOUR_OF_DAY, 23);
		current.set(Calendar.MINUTE, 59);
		current.set(Calendar.SECOND, 59);
		return current;
	}
	
	/**
	 * get the day of time in 23:59:59
	 * 
	 * @return sec mill
	 */
	public static long getLastSecond(){
		Calendar current = Calendar.getInstance();
		current.set(Calendar.HOUR_OF_DAY, 23);
		current.set(Calendar.MINUTE, 59);
		current.set(Calendar.SECOND, 59);
		return current.getTimeInMillis();
	}
	
	/**
	 * get the day`s first sec 00:00:00
	 * 
	 * @param time
	 * @return first sec 
	 */
	public static long getDayFirstSecond(long time){
		return time - (time + TIME_ZONE_OFFSET) % dayMills;
	}
	
	/**
	 * get the first second in the moths first
	 * 
	 * @param time
	 * 
	 * @return sec mill
	 */
	public static long getMonthFirstSecond(long time){
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	/**
	 * get the last second in the moths last 23:59:59
	 * 
	 * @param time
	 * 
	 * @return sec mill
	 */
	public static long getMonthLastSecond(long time){
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	/**
	 * is the same month
	 * 
	 * @param firstT
	 * @param secondT
	 * 
	 * @return true is the same month
	 */
	public static boolean isTheSameMonth(long firstT, long secondT){
		final Calendar cal1 = Calendar.getInstance();
		final Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(firstT);
		cal2.setTimeInMillis(secondT);
		
		if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) {
			return true;
		}
			
		return false;
	}
	
	/**
	 * is the same year
	 * 
	 * @param firstT
	 * @param secondT
	 * 
	 * @return true is the same year else false
	 */
	public static boolean isTheSameYear(long firstT, long secondT){
		final Calendar cal1 = Calendar.getInstance();
		final Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(firstT);
		cal2.setTimeInMillis(secondT);
		
		if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
			return true;
		}
			
		return false;
	}
	
//	/**
//	 * format to string
//	 * month need plus 1
//	 * 
//	 * @param time
//	 * @param format eg: yyyy-mm-dd 
//	 * 
//	 * @return the time string by the format
//	 */
//	private static String formatTimeByType(long time, String format){
//		String myFormat = null == format ? "HH:mm:ss" : format;
//		SimpleDateFormat fomat = new SimpleDateFormat(myFormat);
//		return fomat.format(time);
//	}
	
	public static long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}
	
	public static int getCurrentTimeSecond(){
		return (int)(System.currentTimeMillis()/secondMills);
	}
	
	public static Calendar getCalendarByTime(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		return now;
	}
	
	/**
	 * get date eg: 2013-09-02 --> 20130902 
	 * 
	 * @param time
	 * @return
	 */
	public static int getDateInteger(long time){
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		String _month = month + "";
		String _day = day + "";
		
		if(month < 10) {
			_month = "0" + _month;
		}
			
		if(day < 10) {
			_day = "0" + _day;
		}
			
		String date = year + _month +_day;
		
		return Integer.parseInt(date);
	}
	
	/**
	 * get date eg: 2013-09-02
	 * 
	 * @param time
	 * @return
	 */
	public static String getDateYMD(long time){
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		String _month = month + "";
		String _day = day + "";
		
		if(month < 10) {
			_month = "0" + _month;
		}
			
		if(day < 10) {
			_day = "0" + _day;
		}
			
		StringBuilder sb = new StringBuilder();
		sb.append(year).append("-")
		  .append(_month).append("-")
		  .append(_day);
		
		return sb.toString();
	}
	
	/**
	 * get time by HHmmss format  eg: 23:30:11 --> 233011
	 * if param is -1 then get the current time
	 * 
	 * @param time
	 * @return
	 */
	public static long getHHmmss(long time)
	{
		Calendar calendar = Calendar.getInstance();
		if (time > 0) {
			calendar.setTimeInMillis(time);
		}
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		return hour*10000+minute*100+second;
	}
	
	/**
	 * 获取小时开始的时间
	 * @param time
	 * @return
	 */
	public static int getHourTime(int time){		
		return time - (Calendar.getInstance().get(Calendar.MINUTE) * minuteseconds);
	}
	
	/**
	 * get time by HH:mm:ss format  eg: 23:30:11
	 * if param is -1 then get the current time
	 * 
	 * @param time
	 * @return
	 */
	public static String getHHmmssStr(long time)
	{
		Calendar calendar = Calendar.getInstance();
		if (time > 0) {
			calendar.setTimeInMillis(time);
		}
		
		StringBuffer sBuffer = new StringBuffer();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		
		if(10 > hour) {
			sBuffer.append("0" + hour);
		} else {
			sBuffer.append(hour);
		}
		sBuffer.append(":");
		if(10 > minute) {
			sBuffer.append("0" + minute);
		} else {
			sBuffer.append(minute);
		}
		sBuffer.append(":");
		if(10 > second) {
			sBuffer.append("0" + second);
		} else {
			sBuffer.append(second);
		}
		
		return sBuffer.toString();
	}
	
	/**
	 * format "2014-12-05 23:45:31"
	 * @param time
	 * @return
	 */
	public static boolean checkYMDHmsFormat(String time){  
        StringBuffer year_mounth_date_hour_minute_second = new StringBuffer();  
        //[1-9]{1}[0-9]{3}:匹配年1000-9999  
        year_mounth_date_hour_minute_second.append("[1-9]{1}[0-9]{3}").append("-");  
        //([1-9]|[0]{1}[1-9]{1}|[1]{1}[0-2]{1})匹配月:1-12或01-12或  
        year_mounth_date_hour_minute_second.append("([1-9]|[0]{1}[1-9]{1}|[1]{1}[0-2]{1})").append("-");  
        //([1-9]|[0]{1}[1-9]{1}|[1-2]{1}[0-9]{1}|[3]{1}[0-1]{1})匹配日:1-31或01-31  
        year_mounth_date_hour_minute_second.append("([1-9]|[0]{1}[1-9]{1}|[1-2]{1}[0-9]{1}|[3]{1}[0-1]{1})").append("\\s*");  
        //([0-9]|[0-1]{1}[0-9]{1}|[2]{1}[0-3]{1}):匹配小时:00-23或0-23  
        year_mounth_date_hour_minute_second.append("([0-9]|[0-1]{1}[0-9]{1}|[2]{1}[0-3]{1}):");  
        //([0-9]|[0-5]{1}[0-9]{1}):匹配分钟:00-59或0-59  
        year_mounth_date_hour_minute_second.append("([0-9]|[0-5]{1}[0-9]{1}):");  
        //([0-9]|[0-5]{1}[0-9]{1}):匹配秒:00-59或0-59  
        year_mounth_date_hour_minute_second.append("([0-9]|[0-5]{1}[0-9]{1})");  
        //匹配的字符串  
        String regTime = year_mounth_date_hour_minute_second.toString();  
//        if(time.indexOf("/") != -1){  
//        	time = time.replace("/", "-");  
//        }  

        Pattern pattern = Pattern.compile(regTime);  
        return pattern.matcher(time).matches();  
    }  
	
	/**
	 * format "23:10:23"
	 * @param time
	 * @return
	 */
	public static boolean checkHmsFormat(String time){  
        StringBuffer hour_minute_second = new StringBuffer();    
        //([0-9]|[0-1]{1}[0-9]{1}|[2]{1}[0-3]{1}):匹配小时:00-23或0-23  
        hour_minute_second.append("([0-9]|[0-1]{1}[0-9]{1}|[2]{1}[0-3]{1}):");  
        //([0-9]|[0-5]{1}[0-9]{1}):匹配分钟:00-59或0-59  
        hour_minute_second.append("([0-9]|[0-5]{1}[0-9]{1}):");  
        //([0-9]|[0-5]{1}[0-9]{1}):匹配秒:00-59或0-59  
        hour_minute_second.append("([0-9]|[0-5]{1}[0-9]{1})");  
        //匹配的字符串  
        String regTime = hour_minute_second.toString();  
        Pattern pattern = Pattern.compile(regTime);  
        return pattern.matcher(time).matches();  
    }
	
	/**
	 * format "4,23:10:23"
	 * @param time
	 * @return
	 */
	public static boolean checkWHmsFormat(String time){  
        StringBuffer weekday_hour_minute_second = new StringBuffer();    
        //([1-7]{1}), 匹配 周几  
        weekday_hour_minute_second.append("([1-7]{1}),");  
        //([0-9]|[0-1]{1}[0-9]{1}|[2]{1}[0-3]{1}):匹配小时:00-23或0-23  
        weekday_hour_minute_second.append("([0-9]|[0-1]{1}[0-9]{1}|[2]{1}[0-3]{1}):");  
        //([0-9]|[0-5]{1}[0-9]{1}):匹配分钟:00-59或0-59  
        weekday_hour_minute_second.append("([0-9]|[0-5]{1}[0-9]{1}):");  
        //([0-9]|[0-5]{1}[0-9]{1}):匹配秒:00-59或0-59  
        weekday_hour_minute_second.append("([0-9]|[0-5]{1}[0-9]{1})");  
        //匹配的字符串  
        String regTime = weekday_hour_minute_second.toString();  
        Pattern pattern = Pattern.compile(regTime);  
        return pattern.matcher(time).matches();  
    }
	
	/**
	 * get format type
	 * 
	 * @param str
	 * @return
	 *         YMDHms "2014-12-05 23:45:31"
	 *         WHms   "23:10:23"
	 *         Hms    "4,23:10:23"
	 */
	public static int getTimerFormatType(String str) {
		if(null == str || str.isEmpty() || Global.INVALID_STRING.equals(str)) {
			return Global.INVALID_VALUE;
		}
		
		if(Global.INVALID_VALUE != str.indexOf("%", 0)) {
			return SwapTime;
		}
		
		if(Global.INVALID_VALUE != str.indexOf("-", 0)) {
			return YMDHms;
		}
		
		if(1 == str.indexOf(",", 0)) {
			return WHms;
		}
		
		if(Global.INVALID_VALUE != str.indexOf(":", 0)) {
			return Hms;
		}
		
		return Global.INVALID_VALUE;
	}
	
	/**
	 * use the YYYY-MM-DD HH:mm:ss format get time
	 * use the HH:mm:ss format get time
	 * use the weedDay,HH:mm:ss format get time
	 * 
	 * @param str format time
	 * 
	 * @return sec mill
	 */
	public static long parseDate(String str) {
		if(str.equals("") || str.equals("-1")) {
			return Global.INVALID_VALUE;
		}
		
		try {			
			String strParse = str.replace("：", ":");
			
			// check string YYYY-MM-DD HH:mm:ss
			if(Global.INVALID_VALUE == strParse.indexOf("-", 0)) {
				Calendar calendar = Calendar.getInstance();
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH) + 1;
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				strParse = year + "-" + month + "-" + day + " " + str;
			}
			
			SimpleDateFormat YYYYMMDDHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return YYYYMMDDHHmmss.parse(strParse).getTime();
		} catch (Exception e) {
			throw new RuntimeException("time format is invalid:" + str, e);
		}
	}
	
	/**
	 * get the day of week
	 * if param is -1 then get current day of week else get by time 
	 * 
	 * @param time
	 * @return
	 */
	public static byte getDayOfWeek(long time)
	{
		Calendar calendar = Calendar.getInstance();
		if (time > 0) {
			calendar.setTimeInMillis(time);
		}
		
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		day--;
		day = (day == 0 ? 7 : day);
		return Byte.valueOf(""+day);
	}
	
	/**
	 * get the week index of month
	 * 
	 * @param week index
	 * @return
	 */
	public static int getWeekofMonth(long time)
	{
		int firstWeekDay = getDayOfWeek(getMonthFirstSecond(time));
		int deltaDay = 0;
		if(1 != firstWeekDay) {
			deltaDay = 7 - firstWeekDay + 1;
		}
		int dayMonth = getDateInteger(time) % 100 - deltaDay;
		
		if(dayMonth <= 0) {
			return 1;
		}
		
		int deltaIndex = (0 != (dayMonth % 7)) ? 1 : 0;
		int deltaIndex2 = (0 < deltaDay) ? 1 : 0;
		return dayMonth / 7 + deltaIndex + deltaIndex2;
	}
	
	/**
	 * get current hour
	 * @return
	 */
	public static int getCurrentHour(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(DateValidate.getCurrentTimeMillis());
		return calendar.get(Calendar.HOUR_OF_DAY);
	}
}
