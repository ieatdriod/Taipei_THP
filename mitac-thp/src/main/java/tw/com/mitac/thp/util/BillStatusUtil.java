package tw.com.mitac.thp.util;

public class BillStatusUtil {
	/** 開立 :wf00 */
	public static final String NEW = "wf00";
	/** 確認 :wf70 */
	public static final String CONFIRM = "wf70";
	/** 退件 :wf71 */
	public static final String UNAPPROVED = "wf71";
	/** 已審核(審核) :wf75 */
	public static final String APPROVED = "wf75";
	/** 已轉單 :wf80 */
	public static final String TURN = "wf80";
	/** 結案 :wf90 */
	public static final String FINISH = "wf90";
	/** 作廢 :wf99 */
	public static final String INVALID = "wf99";

	/** 檢驗中:wf51 **/
	public static final String IS_INSPECTING = "wf51";
	/** 檢驗完成:wf53 **/
	public static final String IS_FINISH = "wf53";
	// /** 檢驗完成確認:wf55 **/
	// public static final String IS_CONFIRM = "wf55";

	// /** 變更開始:wf30 */
	// public static final String ALTER_START = "wf30";
	// /** 變更完成:wf35 */
	// public static final String ALTER_FINISH = "wf35";
	//
	/** 未轉 :wf10 */
	public static final String NOTENOUGH = "wf10";
	/** 已轉 :wf20 */
	public static final String ENOUGH = "wf20";
	
	/** 檢測：通過 */
	public static final String PASS = "PASS";
	/** 檢測：未通過 */
	public static final String UNPASS = "UNPASS";
	
	/** 付款狀態：收單 */
	public static final String PAYSTATUS_S = "wf31";
	/** 付款狀態：已報價 */
	public static final String PAYSTATUS_Q = "wf32";
	/** 付款狀態：已付款 */
	public static final String PAYSTATUS_P = "wf33";
	/** 付款狀態：已結案 */
	public static final String PAYSTATUS_F = "wf37";
	/** 付款狀態：已取消 */
	public static final String PAYSTATUS_Z = "wf35";
	/** 付款狀態：已拒絕 */
	public static final String PAYSTATUS_R = "wf36";
	/** 付款狀態：待退款 */
	public static final String PAYSTATUS_B = "wf34";
	/** 開立 :mts00 */
	public static final String MTS_NEW = "mts00";
	// 流程1
	/** 待接單 :mts10 */
	public static final String MTS_WAIT_ORDER = "mts10";
	/** 待轉單 :mts20 */
	public static final String MTS_TURN_ORDER = "mts20";
	/** 已接單 :mts30 */
	public static final String MTS_ORDER = "mts30";
	/** 確認訂單 :mts40 */
	public static final String MTS_CONFIRM = "mts40";
	// 流程2
	/** 待審核 :mts50 */
	public static final String MTS_WAIT_APPROVED = "mts50";
	/** 待確認 :mts60 */
	public static final String MTS_WAIT_CONFIRM = "mts60";
	/** 訂單確認 :mts70 */
	public static final String MTS_CONFIRM_ORDER = "mts70";
	/** 結案 :mts90 */
	public static final String MTS_FINISH = "mts90";
	/** 取消訂單 :mts99 */
	public static final String MTS_CANCEL_ORDER = "mts99";
	
	//
	// /** 待出庫 :wf50 */
	// public static final String PRE_INVENTORYOUT = "wf50";
	// /** 待入庫 :wf60 */
	// public static final String PRE_INVENTORYIN = "wf60";
	// /** 已出庫 :wf55 */
	// public static final String INVENTORYOUT_FINISH = "wf55";
	// /** 已入庫 :wf65 */
	// public static final String INVENTORYIN_FINISH = "wf65";
	//
	// /** 待檢驗:wf25 **/
	// public static final String QC_WAIT = "wf25";
	// /** 檢驗完成:wf26 **/
	// public static final String QC_FINISH = "wf26";
	// /** 驗退:wf27 **/
	// public static final String QC_RETREAT = "wf27";
	// /** 檢驗中:wf28 **/
	// public static final String QC_INSPECTING = "wf28";
	// /** 不需檢驗:wf29 **/
	// public static final String QC_NONE = "wf29";
	//
	// /** 維修管理_收件 :wf00 */
	// public static final String RF_RECEIVE = "wf00";
	// /** 維修管理_派工 :wf30 */
	// public static final String RF_ASSIGN = "wf30";
	// /** 維修管理_退修 :wf70 */
	// public static final String RF_NOREPAIR = "wf70";
	// /** 維修管理_完工 :wf80 */
	// public static final String RF_FINISH = "wf80";
	// /** 維修管理_再修 :wf85 */
	// public static final String RF_AGAIN = "wf85";
	// /** 維修管理_結案 :wf90 */
	// public static final String RF_END = "wf90";
	// /** 維修管理_作廢 :wf99 */
	// public static final String RF_INVALID = "wf99";
	//
	// /** 製造_領料中 :wf40 */
	// public static final String IN_PICK = "wf40";
	// /** 製造_已領料 :wf41 */
	// public static final String HAD_PICK = "wf41";
	// /** 製造_回報中 :wf42 */
	// public static final String IN_REPORT = "wf42";
	// /** 製造_已回報 :wf43 */
	// public static final String HAD_REPORT = "wf43";
	//
	// /** POS_審核中(待審核) :wf70 */
	// public static final String IN_AUDIT = "wf70";
	// /** POS_已審核(已審核) :wf75 */
	// public static final String HAD_AUDIT = "wf75";
	// /** POS_已退件(審核退件) :wf71 */
	// public static final String HAD_REJECT = "wf71";
	//
	// /** 財務_付款完畢:wf76 */
	// public static final String FIN_PAYMENT = "wf76";
	// /** 財務_收款完畢:wf77 */
	// public static final String FIN_COLLECTION = "wf77";
	// /** 財務_部分付款:wf78 */
	// public static final String FIN_PART_PAYMENT = "wf78";
	// /** 財務_部分收款:wf79 */
	// public static final String FIN_PART_COLLECTION = "wf79";
	// /** 財務_退款:wf81 */
	// public static final String FIN_REFUND = "wf81";
	// /** 財務_部分退款:wf82 */
	// public static final String FIN_PART_REFUND = "wf82";
	//
	// /** 未結案 */
	// public static final String[] PENDING_STATUS_GROUP = new String[] { NEW,
	// CONFIRM, TURN, ALTER_START,
	// NOTENOUGH,
	// PRE_INVENTORYOUT, PRE_INVENTORYIN, QC_WAIT, QC_INSPECTING, IN_PICK,
	// HAD_PICK, IN_REPORT,
	// FIN_PAYMENT, FIN_COLLECTION, FIN_PART_PAYMENT, FIN_PART_COLLECTION,
	// FIN_PART_REFUND};
	// /** 已結案 */
	// public static final String[] CLOSED_STATUS_GROUP = new String[] {
	// UNAPPROVED, APPROVED, FINISH,
	// ALTER_FINISH,
	// ALTER_FINISH, INVENTORYOUT_FINISH, INVENTORYIN_FINISH, QC_FINISH,
	// QC_RETREAT, QC_NONE, HAD_REPORT,
	// FIN_REFUND};
	// /** 作廢 */
	// public static final String[] INVALID_STATUS_GROUP = new String[] {
	// INVALID };
	// public static final String PENDING_STATUS;
	// public static final String CLOSED_STATUS;
	// public static final String INVALID_STATUS;
	// static {
	// String statusStr = "";
	// for (String string : PENDING_STATUS_GROUP)
	// statusStr += "," + string;
	// if (StringUtils.isNotBlank(statusStr))
	// statusStr = statusStr.substring(1);
	// PENDING_STATUS = statusStr;
	//
	// statusStr = "";
	// for (String string : CLOSED_STATUS_GROUP)
	// statusStr += "," + string;
	// if (StringUtils.isNotBlank(statusStr))
	// statusStr = statusStr.substring(1);
	// CLOSED_STATUS = statusStr;
	//
	// statusStr = "";
	// for (String string : INVALID_STATUS_GROUP)
	// statusStr += "," + string;
	// if (StringUtils.isNotBlank(statusStr))
	// statusStr = statusStr.substring(1);
	// INVALID_STATUS = statusStr;
	// }
}