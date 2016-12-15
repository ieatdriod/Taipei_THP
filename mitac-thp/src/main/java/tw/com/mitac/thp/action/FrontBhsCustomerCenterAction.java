package tw.com.mitac.thp.action;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsOrders;
import tw.com.mitac.thp.bean.BhsOrdersFeedback;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FrontBhsCustomerCenterAction extends BasisFrontLoginAction {
	protected BhsOrders order;
	protected List<BhsProducts> bhsProductsList;
	protected CpsSiteMember cpsMember;
	protected String fbtable;
	protected String questiontext;

	public BhsOrders getOrder() {
		return order;
	}

	public void setOrder(BhsOrders order) {
		this.order = order;
	}

	public List<BhsProducts> getBhsProductsList() {
		return bhsProductsList;
	}

	public void setBhsProductsList(List<BhsProducts> bhsProductsList) {
		this.bhsProductsList = bhsProductsList;
	}

	public CpsSiteMember getCpsMember() {
		return cpsMember;
	}

	public void setCpsMember(CpsSiteMember cpsMember) {
		this.cpsMember = cpsMember;
	}

	public String getFbtable() {
		return fbtable;
	}

	public void setFbtable(String fbtable) {
		this.fbtable = fbtable;
	}

	public String getQuestiontext() {
		return questiontext;
	}

	public void setQuestiontext(String questiontext) {
		this.questiontext = questiontext;
	}

	/** 訂單List */
	public String bhsOrdersList() {
		String memberSysid = getUserData2().getAccount().getSysid();

		List<BhsOrders> ordersList = cloudDao.queryTable(sf(), BhsOrders.class, new QueryGroup(new QueryRule(
				"memberSysid", memberSysid)), new QueryOrder[] { new QueryOrder(PK, DESC) }, null, null);
		fbtable = "";
		if (ordersList.size() == 0) {
			fbtable = "<table><tr><td><h3>無訂單資料</h3></td></tr>";
		} else {
			DateFormat sdfDT = new SimpleDateFormat("yyyy/MM/dd"); // HH:mm:ss
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(0);
			df.setGroupingUsed(false);
			fbtable += "<table style=\"text-align:center;\" border=\"2\"><tr>" + "<td class=\"col-sm-1\"></td>"
					+ "<td class=\"col-sm-3\">需求單編號</td>" + "<td class=\"col-sm-1\">需求單狀態</td>"
					+ "<td class=\"col-sm-1\">供應商</td>" + "<td class=\"col-sm-1\">開立日期</td>"
					+ "<td class=\"col-sm-3\">需求單類別</td>" + "<td class=\"col-sm-2\">主要產品</td></tr>";
			for (BhsOrders orders : ordersList) {
				CpsVendor vendor = cloudDao.get(sf(), CpsVendor.class, orders.getVendorSysid());
				if (vendor == null) {
					vendor = new CpsVendor();
					vendor.setName("*");
				}
				String btn = "<button class=\"btn\" id=\"submit\" type=\"submit\" formaction=\""
						+ request.getContextPath() + "/bhs/showBhsOrders?sysid=" + orders.getSysid() + "\""
						+ "style=\"background-color: #00b7ee;color:#FFFFFF;\">瀏覽</button>";
				Map<String, Map> bhsOrdersConstantMap = getConstantMenu(BhsOrders.class);
				Map<String, String> orderTypeMap = bhsOrdersConstantMap.get("orderType");
				fbtable += "<tr><td>" + btn + "</td><td>" + orders.getBillno() + "</td><td>"
						+ getWfStatusMap().get(orders.getBillStatus()) + "</td><td>" + vendor.getName() + "</td><td>"
						+ sdfDT.format(orders.getIssueDate()) + "</td><td>" + orderTypeMap.get(orders.getOrderType())
						+ "</td><td>" + orders.getOrdersItem() + "</td></tr>";
			}
		}
		fbtable += "</table>";

		return SUCCESS;
	}

	protected Map<String, String> orderTypeMap;

	public Map<String, String> getOrderTypeMap() {
		return orderTypeMap;
	}

	protected Map<String, String> businessTypeMap;

	public Map<String, String> getBusinessTypeMap() {
		return businessTypeMap;
	}

	/** 訂單 */
	public String bhsOrders() {
		String memberSysid = getUserData2().getAccount().getSysid();

		String sysid = request.getParameter("sysid");
		order = cloudDao.get(sf(), BhsOrders.class, sysid);
		if (order == null)
			return ERROR;
		// 是否為該user的訂單
		if (!order.getMemberSysid().equals(memberSysid))
			return ERROR;

		// 確認訂單
		// String confirm = request.getParameter("confirm");
		// if ("true".equals(confirm) &&
		// BillStatusUtil.MTS_WAIT_CONFIRM.equals(order.getBillStatus())) {
		// order.setBillStatus(BillStatusUtil.MTS_CONFIRM_ORDER);
		// defaultValue(order);
		// order.setOperator(getUserData2().getAccount().getMemberName());
		// List saveList = new ArrayList();
		// saveList.add(order);
		// String daoMsg = "";
		// if (StringUtils.isBlank(daoMsg)) {
		// daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "UPDATE");
		// }
		// if (!daoMsg.equals(SUCCESS)) {
		// addActionError(daoMsg);
		// return ERROR;
		// }
		// }

		Map<String, Map> bhsOrdersConstantMap = getConstantMenu(BhsOrders.class);
		orderTypeMap = bhsOrdersConstantMap.get("orderType");
		businessTypeMap = new LinkedHashMap<String, String>();
		Map<String, String> businessTypeAllMap = bhsOrdersConstantMap.get("businessType");
		if (StringUtils.isNotBlank(order.getBusinessType())) {
			String[] businessTypeArr = order.getBusinessType().split(", ");
			for (String key : businessTypeArr) {
				String value = businessTypeAllMap.get(key);
				// TODO
				// if ("O".equals(key))
				// value += "(" + "其它" + ")";
				businessTypeMap.put(key, value);
			}
		}

		order.setBillStatus(getWfStatusMap().get(order.getBillStatus()));
		CpsVendor vendor = cloudDao.get(sf(), CpsVendor.class, order.getVendorSysid());
		if (vendor == null)
			order.setVendorSysid("*");
		else
			order.setVendorSysid(vendor.getName());
		fbtable = "";
		for (BhsOrdersFeedback fb : order.getDetailSet()) {
			fbtable += "<tr><td>" + fb.getCreationDate().substring(0, 16) + "</td><td>" + fb.getCreator() + "</td><td>"
					+ fb.getFeedbackDetail() + "</td></tr>";
		}

		sessionSet("tempBean", order);
		return SUCCESS;
	}

	/** 新增回應 */
	public String bhsOrdersNewFeedback() {
		boolean isSuccess = saveFeedback();
		if (!isSuccess)
			return ERROR;
		if (!SUCCESS.equals(bhsOrders()))
			return ERROR;
		return SUCCESS;
	}

	protected boolean saveFeedback() {
		try {
			List saveList = new ArrayList();
			order = (BhsOrders) sessionGet("tempBean");
			if (order == null)
				return false;
			String isfb = request.getParameter("isfb");
			if (StringUtils.isNotBlank(isfb) && isfb.equals("true")) {
				if (StringUtils.isNotBlank(questiontext)) {
					BhsOrdersFeedback fb = new BhsOrdersFeedback();
					Util.defaultPK(fb);
					defaultValue(fb);
					fb.setCreator(getUserData2().getAccount().getMemberName());
					fb.setOperator(getUserData2().getAccount().getMemberName());
					fb.setMemberSysid(getUserData2().getAccount().getSysid());
					fb.setParentSysid(order.getSysid());
					String snCounter = String.valueOf((order.getDetailSet().size() + 1) * 10);
					fb.setSequenceNo(StringUtils.leftPad(String.valueOf(snCounter), 4, "0"));
					fb.setSourceType("M");
					fb.setFeedbackDetail(questiontext.replace("\r\n", " "));
					saveList.add(fb);

					String daoMsg = "";
					if (StringUtils.isBlank(daoMsg)) {
						daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
					}

					if (!daoMsg.equals(SUCCESS)) {
						addActionError(daoMsg);
						return false;
					}

				}
			}

		} catch (Exception e) {
			addActionError(e.getClass() + ":" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		addActionMessage(getText(I18N_SAVE_SUCCESS));

		return true;
	}
}