package tw.com.mitac.thp.util;

public interface ProjectArea {
	/** pk */
	public static final String PK = "sysid";

	/** dataLog:creator */
	public static final String CR = "creator";
	/** dataLog:operator/editor/updater */
	public static final String OP = "operator";
	public static final Class<?> timestampClass = String.class;
	/** dataLog:creationDate */
	public static final String CD = "creationDate";
	/** dataLog:operationDate */
	public static final String OD = "operationDate";

	public static final String[] DATA_LOG_MEMBER = { OD, OP, CD, CR };

	/** remark */
	public static final String REMARK = "remark";
	public static final String IS_ENABLED = "isEnabled";
	/** id */
	public static final String ID = "dataId";
	/** name */
	public static final String NAME = "name";
	/** billno */
	public static final String BILLNO = "billno";
	public static final String BILL_SOURCE_TYPE = "sourceType";
	public static final String SOURCE_NO = "sourceNo";
	public static final String SOURCE_SN = "sourceSequenceNo";
	/** dataOrder */
	public static final String DATA_ORDER = "dataOrder";
	public static final String SOURCE_ID = "sourceId";
	
	/** foreignKey */
	public static final String FK = "parentSysid";
	/** detail sequenceno */
	public static final String SN = "sequenceNo";
	/** bill status */
	public static final String BILL_STATUS = "billStatus";
	public static final String SITE_SYSID = "siteSysid";
	public static final String EXCHANGE_RATE = "exchangeRate";
	public static final String ISSUE_DATE = "issueDate";
	public static final String ISSUE_DEPT_SYSID = "issueDeptSysid";
	public static final String ISSUE_EMP_SYSID = "issueEmpSysid";
	public static final String CLOSE_DATE = "closeDate";
	public static final String AUDIT_EMP_SYSID = "auditEmpSysid";
	public static final String AUDIT_DATE = "auditDate";

	public static final String DETAIL_SET = "detailSet";
	public static final String DETAIL_SET2 = "detailSet2";
	public static final String DETAIL_SET3 = "detailSet3";
}