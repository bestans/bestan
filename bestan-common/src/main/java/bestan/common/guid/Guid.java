package bestan.common.guid;

/**
 * guid class
 * 
 * GUID: long 
 * [16]            [8]                 [8]                   [32]
 * [SERV_ID]      [TYPE_ID]           [SUB_TYPE_ID]         [UNIQUE_ID]
 * [服务器ZoneID]  [OBJECT_TYPE_ID]     [1级子类型]           [唯一id]
 */
public final class Guid {

	/**
	 * the value, 0~0xFFFFFFFFFFFFFFFF
	 */
	private long value;

	/**
	 * max GUID value 0xFFFFFFFFFFFFFFFF
	 */
	private static final long GUID_MAX_VALUE = 0xFFFFFFFFFFFFFFFFL;
	
	/**
	 * max UNIQUE ID value 0xFFFFFFFFFFFFFFFF
	 */
	public static final long GUID_MAX_UNIQUE_ID = 0x00000000FFFFFFFFL;
	
	/**
	 * guid high offsize
	 */
	private static final char GUID_HIGH_SHIFT = 32;
	
	/**
	 * zone ID 16位
	 */
	private static final long GUID_ZONE_ID = 0xFFFF000000000000L;

	/**
	 * 
	 */
	private static final long GUID_CLEAR_ZONE_ID = 0x0000FFFFFFFFFFFFL;
	
	/**
	 * 
	 */
	private static final char GUID_ZONE_ID_SHIFT = 48;
	
	/**
	 * type id 8 位
	 */
	private static final long GUID_TYPE_ID = 0x0000FF0000000000L;
	
	/**
	 * 
	 */
	private static final long GUID_CLEAR_TYPE_ID = 0xFFFF00FFFFFFFFFFL;
	
	/**
	 * 
	 */
	private static final char GUID_TYPE_ID_SHIFT = 40;
	
	/**
	 * sub type 5 位
	 */
	private static final long GUID_SUB_TYPE_ID = 0x000000FF00000000L;
	
	/**
	 * 
	 */
	private static final long GUID_CLEAER_SUB_TYPE_ID = 0xFFFFFF00FFFFFFFFL;
	
	/**
	 * 
	 */
	private static final char GUID_SUB_TYPE_ID_SHIFT = 32;
	
	/**
	 * 
	 */
	private static final long GUID_CLEAR_UNIQUE_ID = 0xFFFFFFFF00000000L;
	
	/**
	 * Constructor
	 */
	public Guid(){
		this.value = GUID_MAX_VALUE;
	}
	
	/**
	 * Constructor
	 *
	 * @param value
	 *            guild value
	 */
	public Guid(long value){
		this.value = value;
	}
	
	/**
	 * Constructor
	 *
	 * @param high
	 *            object high
	 * @param low
	 *            object low
	 */
	public Guid(int high, int low){
		this.value = (((high & GUID_MAX_VALUE) << GUID_HIGH_SHIFT) & GUID_CLEAR_UNIQUE_ID) | (low & GUID_MAX_UNIQUE_ID);
	}
	
	public void cleanUp(){
		this.value = GUID_MAX_VALUE;
	}
	
	public boolean isValid(){
		return this.value != GUID_MAX_VALUE;
	}
	
	public void setValue(long invalue){
		this.value = invalue;
	}
	
	public long getValue(){
		return this.value;
	}
		
	public void setZoneId(short zoneId){
		this.value = (this.value & GUID_CLEAR_ZONE_ID) | (((zoneId & GUID_MAX_VALUE) << GUID_ZONE_ID_SHIFT) & GUID_ZONE_ID); 
	}
	
	public short getZoneId(){
		return getZoneId(value);
	}
	
	public static short getZoneId(final long val) {
		return (short)((val & GUID_ZONE_ID) >> GUID_ZONE_ID_SHIFT);
	}
	
	public void setType(short type){
		this.value = (this.value & GUID_CLEAR_TYPE_ID) | (((type & GUID_MAX_VALUE) << GUID_TYPE_ID_SHIFT) & GUID_TYPE_ID); 
	}
	
	public short getType(){
		return (short)((this.value & GUID_TYPE_ID) >> GUID_TYPE_ID_SHIFT);
	}
	
	public static short getType(final long val) {
		return (short)((val & GUID_TYPE_ID) >> GUID_TYPE_ID_SHIFT);
	}
	
	public void setSubType(short subtype){
		this.value = (this.value & GUID_CLEAER_SUB_TYPE_ID) | (((subtype & GUID_MAX_VALUE) << GUID_SUB_TYPE_ID_SHIFT) & GUID_SUB_TYPE_ID); 
	}
	
	public short getSubType(){
		return (short)((this.value & GUID_SUB_TYPE_ID) >> GUID_SUB_TYPE_ID_SHIFT);
	}
	
	public static short getSubType(final long val) {
		return (short)((val & GUID_SUB_TYPE_ID) >> GUID_SUB_TYPE_ID_SHIFT);
	}
	
	public int getHighId(){
		return (int)((this.value >> GUID_HIGH_SHIFT) & GUID_MAX_UNIQUE_ID);
	}
	
	public int getId(){
		return (int)(this.value & GUID_MAX_UNIQUE_ID);
	}
	
	public static int getId(final long val){
		return (int)(val & GUID_MAX_UNIQUE_ID);
	}
	
	public void setId(int id){
		this.value = (this.value & GUID_CLEAR_UNIQUE_ID) | id;
	}

	@Override
	public boolean equals(Object obj) {
        if (obj instanceof Guid) {
            return value == ((Guid)obj).getValue();
        }
        return false;
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}
	
	private static long makeType(int type) {
		return (((type & GUID_MAX_VALUE) << GUID_TYPE_ID_SHIFT) & GUID_TYPE_ID);
	}
	
	private static long makeZoneId(int zoneId) {
		return (((zoneId & GUID_MAX_VALUE) << GUID_ZONE_ID_SHIFT) & GUID_ZONE_ID);
	}
	
	public static Guid newGUID(int zoneId, int type, int uniqueId) {
		return new Guid(makeZoneId(zoneId) | makeType(type) | (uniqueId & GUID_MAX_UNIQUE_ID));
	}
}
