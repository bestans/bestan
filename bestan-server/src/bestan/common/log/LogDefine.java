package bestan.common.log;

public class LogDefine {

	public LogDefine() {
	}

	public enum LOG_TYPE{
		LOG_TYPE_LOGIN,
		LOG_TYPE_MONEY_CONSUME,
		LOG_TYPE_MONEY_PRODUCE,
		LOG_TYPE_MERGE,
		LOG_TYPE_EQUIP_STRENGTHEN,
		LOG_TYPE_EQUIP_UNIQUE_STRENGTHEN,
		LOG_TYPE_EQUIP_IMPROVE,
		LOG_TYPE_HERO_IMPROVE_STAR,
		LOG_TYPE_BULLET_IMPROVE_STAR,
		LOG_TYPE_BULLET_STRENGTHEN,
		LOG_TYPE_BULLET_MASTER,
		LOG_TYPE_EQUIP_UNIQUE_REROLL,
		LOG_TYPE_EQUIP_UNIQUE_REROLL_CONFIRM,
		LOG_TYPE_RAID,
		LOG_TYPE_ACTIVITY,
		LOG_TYPE_PVP,
		LOG_TYPE_ENERGY_CONSUME,
	}
}