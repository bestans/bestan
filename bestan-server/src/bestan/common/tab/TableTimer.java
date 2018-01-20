package bestan.common.tab;

import bestan.common.exception.TableLoadException;
import bestan.common.timer.DateValidate;
import bestan.common.util.Global;

public class TableTimer extends TableInterface
{
    public int id;
    
    /**
     * beginTimeStr ~ endTimeStr
     * 数组中的元素是互斥的 如果有相同交集则验证不过
     * 
     * intervalTime 只有是在endtime -1的时候才起作用
     * endTimeStr 可以是 -1  主要为了  intervalTime 和 loopNum 服务，或者 一天的一个固定时间点服务
     */
    public String[] beginTimeStr;
    public String[] endTimeStr;
    public String desc;
    public int loopNum;
    public int intervalTime;

    /**
     * type
     * 1 == DateValidate.YMDHms  2014-12-23 11:00:00
     * 2 == DateValidate.WHms    2,11:00:00  周二十一点
     * 3 == DateValidate.Hms  11:00:00
     * 4 == $deltaDay time, %deltaDay time  替换的内容
     * 
     */
    public int type;
    
    /**
     * beginWeekDay
     * type == DateValidate.WHms 记住 周几
     */
    public int beginWeekDay[];
    
    /**
     * endWeekDay
     * type == DateValidate.WHms 记住 周几
     */
    public int endWeekDay[];
    
    public int checkcount;
    
    /**
     * deltaDay
     * deltaDay 在 type == DateValidate.WHms || DateValidate.Hms
     * endTime > beginTime 则 deltaDay = 7 || 1
     */
    public int deltaDay[];
    
    /**
     * 自循环时间
     * type == DateValidate.WHms  autoLoopMillTime = 每周
     * type == DateValidate.Hms   autoLoopMillTime = 每日
     */
    public long autoLoopMillTime = Global.INVALID_VALUE;
    
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * set 开始时间
	 * @param beginTime
	 * @throws TableLoadException 
	 */
	public void setBeginTime(String beginTime) throws TableLoadException {
		String[] parseStrList = beginTime.split("\\|");
		this.beginTimeStr = new String[parseStrList.length];
		this.beginWeekDay = new int[parseStrList.length];
		for (int i = 0; i < parseStrList.length; i++) {
			int nType = DateValidate.getTimerFormatType(parseStrList[i]);
			if(Global.INVALID_VALUE == nType) {
				throw new TableLoadException("Timer.tab id:" + this.id + " begin time format is invalid");
			}
			this.beginTimeStr[i] = parseStrList[i];
			this.type = nType;
			this.beginWeekDay[i] = Global.INVALID_VALUE;
			
			if(DateValidate.Hms == this.type) {
				// 检测配置
				if(!DateValidate.checkHmsFormat(parseStrList[i])) {
					throw new TableLoadException("Timer.tab id:" + this.id + " Hms format invalid, str:" + parseStrList[i]);
				}
				this.autoLoopMillTime = DateValidate.dayMills;
			} else if(DateValidate.WHms == this.type) {
				// 检测配置
				if(!DateValidate.checkWHmsFormat(parseStrList[i])) {
					throw new TableLoadException("Timer.tab id:" + this.id + " WHms format invalid, str:" + parseStrList[i]);
				}
				this.autoLoopMillTime = DateValidate.weekMills;
				this.beginWeekDay[i] = parseStrList[i].charAt(0) - '1' + 1;
				this.beginTimeStr[i] = parseStrList[i].substring(2);
			} else if(DateValidate.YMDHms == this.type) {
				// 检测配置
				if(!DateValidate.checkYMDHmsFormat(parseStrList[i])) {
					throw new TableLoadException("Timer.tab id:" + this.id + " YMDHms format invalid, str:" + parseStrList[i]);
				}
			} else if(DateValidate.SwapTime == this.type) {
				System.out.println("Need Swap to YMDHms Time Format, beginTime:" + this.beginTimeStr[i]);
			}
        }
		
		this.checkcount = parseStrList.length;
	}


	/**
	 * 设置结束时间
	 * @param endTime
	 * @throws TableLoadException 
	 */
	public void setEndTime(String endTime) throws TableLoadException {
		String[] parseStrList = endTime.split("\\|");
		if(this.checkcount != parseStrList.length) {
			throw new TableLoadException("Timer.tab begin time and endtime num is un same!!!");
		}
		
		this.endTimeStr = new String[parseStrList.length];
		this.deltaDay = new int[parseStrList.length];
		this.endWeekDay = new int[parseStrList.length];
		for (int i = 0; i < parseStrList.length; i++) {
			this.deltaDay[i] = 0;
			this.endTimeStr[i] = parseStrList[i];
			this.endWeekDay[i] = Global.INVALID_VALUE;
			if(!Global.INVALID_STRING.equals(parseStrList[i])) {
				int nType = DateValidate.getTimerFormatType(parseStrList[i]);
				// 判断 开始时间和结束时间的格式是否相同
				if(this.type != nType) {
					throw new TableLoadException("Timer.tab id:" + this.id + " begin time format and end time format is diffrent");
				}
				
				// 判断 结束时间是否大于开始时间
				if(DateValidate.YMDHms == this.type) {
					// 检测时间格式
					if(!DateValidate.checkYMDHmsFormat(parseStrList[i])) {
						throw new TableLoadException("Timer.tab id:" + this.id + " YMDHms format invalid, str:" + parseStrList[i]);
					}
					// 固定时间 : 结束时间不能大于开始时间
					if(0 < this.beginTimeStr[i].compareTo(this.endTimeStr[i])) {
						throw new TableLoadException("Timer.tab id:" + this.id + " begin time is bigger than end time");
					}
				} 
				
				if(DateValidate.Hms == this.type) {
					// 检测时间格式
					if(!DateValidate.checkHmsFormat(parseStrList[i])) {
						throw new TableLoadException("Timer.tab id:" + this.id + " Hms format invalid, str:" + parseStrList[i]);
					}
					
					// 判断 deltaDay
					if(0 < this.beginTimeStr[i].compareTo(this.endTimeStr[i])) {
						this.deltaDay[i] = 1;
					}
				} else if(DateValidate.WHms == this.type) {
					// 检测时间格式
					if(!DateValidate.checkWHmsFormat(parseStrList[i])) {
						throw new TableLoadException("Timer.tab id:" + this.id + " WHms format invalid, str:" + parseStrList[i]);
					}
					
					this.endWeekDay[i] = parseStrList[i].charAt(0) - '1' + 1;
					this.endTimeStr[i] = parseStrList[i].substring(2);
					// 判断 deltaDay
					if(this.beginWeekDay[i] > this.endWeekDay[i]) {
						this.deltaDay[i] = 7;
					} else if (this.beginWeekDay[i] == this.endWeekDay[i]) {
						// 如果 week 相同 则判断 开始时间必须大于结束时间
						if(0 < this.beginTimeStr[i].compareTo(this.endTimeStr[i])) {
							throw new TableLoadException("Timer.tab id:" + this.id + " begin time is bigger than end time");
						}
					}
				}
			}// end of if == -1
        }// end of for
		
		//TODO: 判断这个timer 时间是否有重合
		for (int i = 0; i < parseStrList.length; i++) {
			//TODO: do duplicate
		}
	}


	/**
	 * 获得描述
	 * @return
	 */
	public String getDesc() {
		return desc;
	}


	
	public void setDesc(String desc) {
		this.desc = desc;
	}


	/**
	 * 获得 循环次数
	 * @return
	 */
	public int getLoopNum() {
		return loopNum;
	}

	/**
	 * 设置循环次数
	 * @param loopNum
	 */
	public void setLoopNum(int loopNum) {
		this.loopNum = loopNum;
	}
	
	/**
	 * 设置循环间隔时间
	 * @param intervalTime
	 */
	public void setIntervalTime(int intervalTime) {
		this.intervalTime = intervalTime;
	}
    
    @Override
    public void mapData(ISerializer s) throws TableLoadException
    {
    	this.id = super.mapIndex(s);
        s.Parse(this, "setBeginTime", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_STRING);
        s.Parse(this, "setEndTime", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_STRING);
        s.SkipField(); // 描述
        s.Parse(this, "setLoopNum", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_INT);
        s.Parse(this, "setIntervalTime", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_INT);
    }
}