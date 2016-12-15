package tw.com.mitac.thp.util;

public class ConstantUtil {
	/**
	 * HpsPromoteBonusHistory.bonusStatus EFFECT:已生效
	 */
	public static final String BONUS_STATUS_EFFECT = "EFFECT";

	/**
	 * HpsPromoteBonus.typeOfUse ALL:全館
	 */
	public static final String TYPE_OF_USE_ALL = "ALL";
	/**
	 * HpsPromoteBonus.typeOfUse TYPE:特定類別
	 */
	public static final String TYPE_OF_USE_TYPE = "TYPE";
	/**
	 * HpsPromoteBonus.typeOfUse ITEM:特定商品
	 */
	public static final String TYPE_OF_USE_ITEM = "ITEM";

	/**
	 * HpsPromotionItem.applyType TYPE:類別
	 */
	public static final String APPLY_TYPE_TYPE = "TYPE";
	/**
	 * HpsPromotionItem.applyType ITEM:商品
	 */
	public static final String APPLY_TYPE_ITEM = "ITEM";

	/** CoreEmployee.empStatus */
	public static final String EMP_STATUS_ONJOB = "ONJOB";
	public static final String EMP_STATUS_QUITJOB = "QUITJOB";
	public static final String EMP_STATUS_NONE = "NONE";
	/**
	 * HpsBillSalesOrder.payStatus 付款狀態:未付款
	 */
	public static final String PAY_STATUS_NONE = "NONE";
	/**
	 * HpsBillSalesOrder.payStatus 付款狀態:交易中(等待回應) 若無馬上回應則可能逾時
	 */
	public static final String PAY_STATUS_WAIT = "WAIT";
	/**
	 * HpsBillSalesOrder.payStatus 付款狀態:已付款
	 */
	public static final String PAY_STATUS_PAID = "PAID";

	/**
	 * SalesOrder.payType 付款方式:信用卡
	 */
	public static final String PAY_TYPE_SALE42 = "sale42";
	/**
	 * SalesOrder.payType 付款方式:銀聯卡
	 */
	public static final String PAY_TYPE_SALE61 = "sale61";
	/**
	 * SalesOrder.payType 付款方式:atm 含web atm
	 */
	public static final String PAY_TYPE_ATM = "atm";
	/**
	 * SalesOrder.payType 付款方式:便利超商代收
	 */
	public static final String PAY_TYPE_CS = "cs";

	/**
	 * HpsBillSalesOrder.invoiceType 發票類型:二聯式
	 */
	public static final String INVOICE_TYPE_TWO = "TWO";
	/**
	 * HpsBillSalesOrder.invoiceType 發票類型:二聯式
	 */
	public static final String INVOICE_TYPE_THREE = "THREE";

	/**
	 * SalesOrder/SalesOrderItem.deliveryStatus 產品出貨狀態:NONE:未出貨
	 */
	public static final String DELIVERY_STATUS_NONE = "NONE";
	/**
	 * SalesOrder/SalesOrderItem.deliveryStatus 產品出貨狀態:PART_DONE:部份出貨
	 */
	public static final String DELIVERY_STATUS_PART_DONE = "PART_DONE";
	/**
	 * SalesOrder/SalesOrderItem.deliveryStatus 產品出貨狀態:DONE:已出貨
	 */
	public static final String DELIVERY_STATUS_DONE = "DONE";

	/**
	 * SalesOrderItem.salesType 銷售類別:NORMAL:一般
	 */
	public static final String SALES_TYPE_NORMAL = "NORMAL";
	/**
	 * SalesOrderItem.salesType 銷售類別:PLUS:加價購
	 */
	public static final String SALES_TYPE_PLUS = "PLUS";
	/**
	 * SalesOrderItem.salesType 銷售類別:GIFT:贈品
	 */
	public static final String SALES_TYPE_GIFT = "GIFT";
	/**
	 * snedMail 訂單成立
	 */
	public static final String Mail_ORDER_READY = "ORDER_READY";
	/**
	 * snedMail 訂單取消
	 */
	public static final String Mail_ORDER_CANCEL = "ORDER_CANCEL";
	/**
	 * snedMail 付款成功
	 */
	public static final String Mail_PAY_SUCCESS = "PAY_SUCCESS";
	/**
	 * snedMail 商品退貨
	 */
	public static final String Mail_ITEM_RETURN = "ITEM_RETURN";
}