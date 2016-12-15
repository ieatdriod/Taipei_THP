package tw.com.mitac.thp.action;

import java.math.BigDecimal;
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
import tw.com.mitac.thp.bean.CpsSiteMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsItems;
import tw.com.mitac.thp.bean.MtsOrders;
import tw.com.mitac.thp.bean.MtsOrdersFeedback;
import tw.com.mitac.thp.bean.MtsOrdersItems;
import tw.com.mitac.thp.bean.MtsOrdersProducts;
import tw.com.mitac.thp.bean.MtsProducts;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FrontMtsCustomerCenterAction extends BasisFrontLoginAction {
	protected MtsOrders order;
	protected List<MtsOrdersProducts> mtsOrdersProductsList;
	protected List<MtsOrdersItems> mtsOrdersItemsList;
	protected List<MtsProducts> mtsProductsList;
	protected List<MtsItems> mtsItemsList;
	protected CpsSiteMember cpsMember;
	protected String fbtable;
	protected String questiontext;

	public MtsOrders getOrder() {
		return order;
	}

	public void setOrder(MtsOrders order) {
		this.order = order;
	}

	public List<MtsOrdersProducts> getMtsOrdersProductsList() {
		return mtsOrdersProductsList;
	}

	public void setMtsOrdersProductsList(List<MtsOrdersProducts> mtsOrdersProductsList) {
		this.mtsOrdersProductsList = mtsOrdersProductsList;
	}

	public List<MtsOrdersItems> getMtsOrdersItemsList() {
		return mtsOrdersItemsList;
	}

	public void setMtsOrdersItemsList(List<MtsOrdersItems> mtsOrdersItemsList) {
		this.mtsOrdersItemsList = mtsOrdersItemsList;
	}

	public List<MtsProducts> getMtsProductsList() {
		return mtsProductsList;
	}

	public void setMtsProductsList(List<MtsProducts> mtsProductsList) {
		this.mtsProductsList = mtsProductsList;
	}

	public List<MtsItems> getMtsItemsList() {
		return mtsItemsList;
	}

	public void setMtsItemsList(List<MtsItems> mtsItemsList) {
		this.mtsItemsList = mtsItemsList;
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
	public String mtsOrdersList() {
		String memberSysid = getUserData2().getAccount().getSysid();

		List<MtsOrders> ordersList = cloudDao.queryTable(sf(), MtsOrders.class, new QueryGroup(new QueryRule(
				"memberSysid", memberSysid)), new QueryOrder[] { new QueryOrder(PK, DESC) }, null, null);
		fbtable = "";
		if (ordersList.size() == 0) {
			// fbtable = "<table><tr><td><h3>無訂單資料</h3></td></tr>";
			fbtable = "<table><tr><td><h3>No Data</h3></td></tr>";
		} else {
			DateFormat sdfDT = new SimpleDateFormat("yyyy/MM/dd"); // HH:mm:ss
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(0);
			df.setGroupingUsed(false);
			// fbtable +=
			// "<table style=\"text-align:center;\" border=\"2\"><tr>" +
			// "<td class=\"col-sm-1\"></td>"
			// + "<td class=\"col-sm-3\">訂單編號</td>" +
			// "<td class=\"col-sm-1\">訂單狀態</td>"
			// + "<td class=\"col-sm-1\">醫療團隊</td>" +
			// "<td class=\"col-sm-1\">開立日期</td>"
			// + "<td class=\"col-sm-3\">訂單類別</td>" +
			// "<td class=\"col-sm-2\">金額</td></tr>";
			fbtable += "<table style=\"text-align:center;\" border=\"2\"><tr>" + "<td class=\"col-sm-1\"></td>"
					+ "<td class=\"col-sm-3\">Order No.</td>" + "<td class=\"col-sm-1\">Order Status</td>"
					+ "<td class=\"col-sm-1\">Medical Team</td>" + "<td class=\"col-sm-1\">Order Date</td>"
					+ "<td class=\"col-sm-3\">Order Category</td>" + "<td class=\"col-sm-2\">Cost</td></tr>";
			for (MtsOrders orders : ordersList) {
				CpsVendor vendor = cloudDao.get(sf(), CpsVendor.class, orders.getVendorSysid());
				if (vendor == null) {
					vendor = new CpsVendor();
					vendor.setName("*");
				}
				// String btn =
				// "<button class=\"btn\" id=\"submit\" type=\"submit\" formaction=\""+request.getContextPath()+"/mts/showMtsOrders?sysid="
				// + orders.getSysid() + "\"" +
				// "style=\"background-color: #00b7ee;color:#FFFFFF;\">瀏覽</button>";
				String btn = "<button class=\"btn\" id=\"submit\" type=\"submit\" formaction=\""
						+ request.getContextPath() + "/mts/showMtsOrders?sysid=" + orders.getSysid() + "\""
						+ "style=\"background-color: #00b7ee;color:#FFFFFF;\">Enter</button>";
				// String btn2 = "";
				String url = "showMtsOrdersAppointment2";
				// String str = "驗證";
				String str = "Validate";
				if ("SO".equals(orders.getOrdersType())) {
					url = "showMtsConsultant";
					// str = "付款";
					str = "Payment";
				}
				if (BillStatusUtil.MTS_NEW.equals(orders.getBillStatus()))
					btn += "<button class=\"btn\" id=\"submit\" type=\"submit\" formaction=\""
							+ request.getContextPath() + "/mts/" + url + "?sysid=" + orders.getSysid() + "\""
							+ "style=\"background-color: #d9534f;color:#FFFFFF;\">" + str + "</button>";
				if (BillStatusUtil.MTS_WAIT_CONFIRM.equals(orders.getBillStatus())) {
					// 確認訂單
					btn += "<button class=\"btn\" id=\"submit\" type=\"submit\" formaction=\""
							+ request.getContextPath() + "/mts/showMtsOrders?sysid=" + orders.getSysid()
							+ "&confirm=true\"" + "style=\"background-color: #00b7ee;color:#FFFFFF;\">Confirm</button>";
					// 訂單異議
					btn += "<button class=\"btn\" id=\"submit\" type=\"submit\" formaction=\""
							+ request.getContextPath() + "/mts/showMtsOrders?sysid=" + orders.getSysid()
							+ "&confirm=false\""
							+ "style=\"background-color: #d9534f;color:#FFFFFF;\">Objection</button>";
				}

				String ordersType = "MA".equals(orders.getOrdersType()) == true ? "Make an appointment"
						: "Second Opinion";

				BigDecimal bd = orders.getTransactionAmount().setScale(2, BigDecimal.ROUND_DOWN);
				String amount = df.format(bd);
				fbtable += "<tr><td>" + btn + "</td><td>" + orders.getBillno() + "</td><td>"
						+ getWfStatusMap().get(orders.getBillStatus()) + "</td><td>" + vendor.getName() + "</td><td>"
						+ sdfDT.format(orders.getIssueDate()) + "</td><td>" + ordersType + "</td><td>" + amount
						+ "</td></tr>";
			}
		}
		fbtable += "</table>";

		return SUCCESS;
	}

	protected Map<String, String> treatmentMap;

	public Map<String, String> getTreatmentMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map = treatmentMap;
		return map;
	}

	/** 訂單 */
	public String mtsOrders() {
		String memberSysid = getUserData2().getAccount().getSysid();

		String sysid = request.getParameter("sysid");
		order = cloudDao.get(sf(), MtsOrders.class, sysid);
		if (order == null)
			return ERROR;
		// 是否為該user的訂單
		if (!order.getMemberSysid().equals(memberSysid))
			return ERROR;

		// 確認訂單 / 訂單異議
		String confirm = request.getParameter("confirm");
		if (StringUtils.isNotBlank(confirm) && BillStatusUtil.MTS_WAIT_CONFIRM.equals(order.getBillStatus())) {
			if ("true".equals(confirm))
				order.setBillStatus(BillStatusUtil.MTS_CONFIRM_ORDER);
			if ("false".equals(confirm))
				order.setBillStatus(BillStatusUtil.MTS_WAIT_APPROVED);
			defaultValue(order);
			order.setOperator(getUserData2().getAccount().getMemberName());
			List saveList = new ArrayList();
			saveList.add(order);
			String daoMsg = "";
			if (StringUtils.isBlank(daoMsg)) {
				daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "UPDATE");
			}
			if (!daoMsg.equals(SUCCESS)) {
				addActionError(daoMsg);
				return ERROR;
			}
		}

		order.setBillStatus(getWfStatusMap().get(order.getBillStatus()));
		CpsVendor vendor = cloudDao.get(sf(), CpsVendor.class, order.getVendorSysid());
		if (vendor == null)
			order.setVendorSysid("*");
		else
			order.setVendorSysid(vendor.getName());

		Map<String, String> map = new LinkedHashMap<String, String>();
		for (MtsOrdersItems moi : order.getDetailSet3()) {
			MtsItems mi = cloudDao.get(sf(), MtsItems.class, moi.getMtsItemsSysid());
			if (StringUtils.isNotBlank(moi.getRemark()))
				map.put("I" + mi.getSysid(), mi.getDataId() + "：" + mi.getName() + "(" + moi.getRemark() + ")");
			else
				map.put(mi.getSysid(), mi.getDataId() + "：" + mi.getName());
		}
		for (MtsOrdersProducts mop : order.getDetailSet2()) {
			MtsProducts mp = cloudDao.get(sf(), MtsProducts.class, mop.getMtsProductsSysid());
			map.put(mp.getSysid(), mp.getName());
		}
		treatmentMap = map;

		fbtable = "";
		for (MtsOrdersFeedback fb : order.getDetailSet()) {
			fbtable += "<tr><td>" + fb.getCreationDate().substring(0, 16) + "</td><td>" + fb.getCreator() + "</td><td>"
					+ fb.getFeedbackDetail() + "</td></tr>";
		}

		sessionSet("tempBean", order);
		return SUCCESS;
	}

	/** 新增回應 */
	public String mtsOrdersNewFeedback() {
		boolean isSuccess = saveFeedback();
		if (!isSuccess)
			return ERROR;
		if (!SUCCESS.equals(mtsOrders()))
			return ERROR;
		return SUCCESS;
	}

	protected boolean saveFeedback() {
		try {
			List saveList = new ArrayList();
			order = (MtsOrders) sessionGet("tempBean");
			if (order == null)
				return false;
			String isfb = request.getParameter("isfb");
			if (StringUtils.isNotBlank(isfb) && isfb.equals("true")) {
				if (StringUtils.isNotBlank(questiontext)) {
					MtsOrdersFeedback fb = new MtsOrdersFeedback();
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