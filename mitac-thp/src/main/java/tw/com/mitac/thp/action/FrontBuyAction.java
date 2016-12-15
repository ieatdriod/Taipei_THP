package tw.com.mitac.thp.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.json.JSONObject;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryOrderWithTable;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.thp.bean.CpsCountry;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.HpsBillSalesOrder;
import tw.com.mitac.thp.bean.HpsBillSalesOrderItem;
import tw.com.mitac.thp.bean.HpsCoreShoppingCart;
import tw.com.mitac.thp.bean.HpsVendorItem;
import tw.com.mitac.thp.util.BillStatusUtil;
import tw.com.mitac.thp.util.ConstantUtil;
import tw.com.mitac.thp.util.Util;

public class FrontBuyAction extends BasisFrontLoginAction {
	private static final String SALES_ORDER_SESSION = "salesOrderSession";
	private static final String CALC_FINAL_AMOUNT = "calcFinalAmount";
	private static final String ORDER_FREIGHT = "orderFreight";
	private static final String CALC_TOTAL_TRANSACTION_AMOUNT = "calcTotalTransactionAmount";
	private static final String SHOPPING_CART_ITEM_RESOURCE = "shoppingCartItemResource";
	private final String SAVE_MSG = "saveMsg";
	/**
	 * 測試用ip 非此ip無法測試玉山信用卡/銀聯卡線上繳款功能 空值(正式上線):不限制使用
	 */
	public static final String TEST_ESUN_CLIENT_IP = "211.78.245.233";// "211.78.245.233";

	public static final String DOMAIN_NAME;
	static {
		if (StringUtils.isNotBlank(TEST_ESUN_CLIENT_IP))
			DOMAIN_NAME = "http://211.78.245.231";
		else
			DOMAIN_NAME = "http://175.98.165.143";
	}

	protected HpsBillSalesOrder order;
	protected String countrySelect;
	protected String invoiceSelect;

	public String viewCart() {
		Map<String, List> buyCartMap = new LinkedHashMap<String, List>();
		QueryGroup queryGroup = new QueryGroup(new QueryRule("memberSysid", getUserData2().getAccount().getSysid()));
		List<HpsCoreShoppingCart> cartList = cloudDao.queryTable(sf(), HpsCoreShoppingCart.class, queryGroup,
				new QueryOrder[] { new QueryOrder("vendorSysid"),
						new QueryOrderWithTable("itemSysid", ASC, HpsVendorItem.class, PK, ID) }, null, null);
		if (cartList.size() > 0) {
			List<String> itemSysidList = new ArrayList<String>();
			for (HpsCoreShoppingCart hpsCoreShoppingCart : cartList) {
				itemSysidList.add(hpsCoreShoppingCart.getItemSysid());
			}
			List<Map> hpsVendorItemList = (List<Map>) cloudDao.findProperty(sf(), HpsVendorItem.class, queryGroup,
					new QueryOrder[0], false, PK, ID, NAME, "salesPrice", "mainPictureFilePath");
			Map<String, Map> hpsVendorItemMap = new HashMap<String, Map>();
			for (Map<String, Object> map : hpsVendorItemList) {
				hpsVendorItemMap.put((String) map.get(PK), map);
			}

			for (HpsCoreShoppingCart hpsCoreShoppingCart : cartList) {
				List<Map> buyCartList = buyCartMap.get(hpsCoreShoppingCart.getVendorSysid());
				if (buyCartList == null) {
					buyCartList = new ArrayList<Map>();
					buyCartMap.put(hpsCoreShoppingCart.getVendorSysid(), buyCartList);
				}
				Map<String, Object> cartMap = tw.com.mitac.ssh.util.Util.formatToMap(hpsCoreShoppingCart);
				buyCartList.add(cartMap);

				cartMap.put("itemId", hpsVendorItemMap.get(hpsCoreShoppingCart.getItemSysid()).get(ID));
				cartMap.put("itemName", hpsVendorItemMap.get(hpsCoreShoppingCart.getItemSysid()).get(NAME));
				cartMap.put("salesPrice", hpsVendorItemMap.get(hpsCoreShoppingCart.getItemSysid()).get("salesPrice"));
				cartMap.put("mainPictureFilePath",
						hpsVendorItemMap.get(hpsCoreShoppingCart.getItemSysid()).get("mainPictureFilePath"));
			}
		}
		request.setAttribute("buyCartMap", buyCartMap);
		return SUCCESS;
	}

	public String viewOrder() {
		String buyVendorSysid = request.getParameter("buyVendorSysid");
		if (StringUtils.isBlank(buyVendorSysid))
			return INPUT;
		CpsVendor vendor = getDataCpsVendorTable().get(buyVendorSysid);
		if (vendor == null)
			return INPUT;

		session.put("buyVendorSysid", buyVendorSysid);
		viewCart();

		CpsCountry memberCountry = getAllCountry().get(getUserData2().getAccount().getCountrySysid());
		if (memberCountry != null)
			countrySelect = getUserData2().getAccount().getCountrySysid() + "#" + memberCountry.getIsForeign();

		return SUCCESS;
	}

	/**
	 * @see 05_store-shipping.jsp
	 */
	public String checkoutConfirm() {
		boolean isSkipEsun = StringUtils.isNotBlank(TEST_ESUN_CLIENT_IP);
		if (isSkipEsun) {
			String ipAddress = request.getRemoteAddr();
			if (TEST_ESUN_CLIENT_IP.equals(ipAddress))
				isSkipEsun = false;
		}

		if (StringUtils.isBlank(countrySelect)) {
			addActionError("未選擇國家");
			return INPUT;
		}
		if (StringUtils.isBlank(invoiceSelect)) {
			addActionError("未選擇發票類型");
			return INPUT;
		}
		if (StringUtils.isBlank(order.getVendorSysid())) {
			addActionError("未設定廠商");
			return INPUT;
		}

		List saveList = new ArrayList();

		Util.defaultPK(order);
		defaultValue(order);
		defaultBillno(order);
		order.setBillStatus(BillStatusUtil.NEW);
		order.setIssueDate(systemDatetime);
		order.setBuyerMemberSysid(getUserData2().getAccount().getSysid());

		order.setPayStatus(ConstantUtil.PAY_STATUS_NONE);
		if (ConstantUtil.PAY_TYPE_ATM.equals(order.getPayType()))
			order.setPayStatus(ConstantUtil.PAY_STATUS_NONE);
		else
			order.setPayStatus(ConstantUtil.PAY_STATUS_PAID);

		// FIXME 銷售訂單新流程
		// order.setShippingStatus("");//

		String[] arr = countrySelect.split("#");
		order.setReceivingCountrySysid(arr[0]);
		if ("TWO_DONATE".equals(invoiceSelect)) {
			order.setInvoiceType(ConstantUtil.INVOICE_TYPE_TWO);
			order.setIsDonateInvoice(true);
		} else {
			order.setInvoiceType(invoiceSelect);
			order.setIsDonateInvoice(false);
		}

		BigDecimal transactionAmount = BigDecimal.ZERO;
		Set<HpsBillSalesOrderItem> detailSet = new LinkedHashSet<HpsBillSalesOrderItem>();
		order.setDetailSet(detailSet);

		viewCart();
		Map<String, List> buyCartMap = (Map<String, List>) request.getAttribute("buyCartMap");
		List<Map> buyCartList = buyCartMap.get(order.getVendorSysid());
		int snCounter = 1;
		for (Map<String, Object> cartMap : buyCartList) {
			BigDecimal salesPrice = (BigDecimal) cartMap.get("salesPrice");
			Long quantity = new Long(cartMap.get("quantity").toString());
			BigDecimal amount = salesPrice.multiply(new BigDecimal(quantity));
			transactionAmount = transactionAmount.add(amount);
			String vendorItemSysid = (String) cartMap.get("itemSysid");
			String vendorItemName = (String) cartMap.get("itemName");

			HpsBillSalesOrderItem detail = new HpsBillSalesOrderItem();
			detailSet.add(detail);
			Util.defaultPK(detail);
			defaultValue(detail);
			detail.setParentSysid(order.getSysid());
			detail.setSequenceNo(StringUtils.leftPad(String.valueOf(snCounter), 4, "0"));
			detail.setVendorItemSysid(vendorItemSysid);
			detail.setVendorItemName(vendorItemName);
			detail.setItemSpecificationSysid("");
			detail.setQuantity(quantity);
			detail.setUnitPrice(salesPrice);
			detail.setTransactionAmount(amount);
			detail.setIsPlus(false);
			detail.setIsGift(false);
			detail.setTransportType("");
			detail.setRelateSequenceNo("");// 加價購 贈品
			detail.setAllotRate(BigDecimal.ZERO);// TODO 拆給生策會的比率
		}

		order.setTransactionAmount(transactionAmount);
		ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.MULTI_LINE_STYLE);
		logger.debug("order:" + ReflectionToStringBuilder.toString(order));
		saveList.add(order);

		saveList.add(new DeleteStatement(HpsCoreShoppingCart.class.getSimpleName(), new QueryGroup(new QueryRule(
				"memberSysid", getUserData2().getAccount().getSysid()), new QueryRule("vendorSysid", order
				.getVendorSysid()))));
		String daoMsg = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
		logger.info("daoMsg:" + daoMsg);
		if (SUCCESS.equals(daoMsg)) {
			session.remove(Util.SHOPPING_CART_MAP);
			session.remove("showShoppingCartMap");

			addActionMessage("本單據" + getText("bean.payStatus." + order.getPayStatus()));
		}
		return SUCCESS;
	}

	public String addItem() {
		String inputItemSysid = request.getParameter("itemSysid");
		String inputVendorSysid = request.getParameter("vendorSysid");
		String quStr = request.getParameter("quantity");
		return addItem(inputItemSysid, inputVendorSysid, quStr);
	}

	/**
	 * 將產品加入購物車 若產品已在購物車則不做異動
	 * 
	 * @return
	 */
	public String addItem(String inputItemSysid, String inputVendorSysid, String quStr) {// 可能有一般商品，加進購物車
		String uid = "ADMIN";
		logger.debug("測試inputCart開始");
		resultMap = new HashMap<Object, Object>();
		resultMap.put("isSuccess", true);
		resultMap.put("msg", "商品已加入購物車");

		if (getDataCpsVendorTable().get(inputVendorSysid) == null) {
			resultMap.put("isSuccess", false);
			resultMap.put("msg", "無效的廠商");
		}
		// else if (getDataHpsCoreItemTypeTable().get(inputItemSysid) == null) {
		// resultMap.put("isSuccess", false);HpsVendorItem
		// resultMap.put("msg", "無效的商品");
		// }
		if ((Boolean) resultMap.get("isSuccess")) {
			boolean isLogin = (getUserData2() != null);
			String memberSysid = "";
			if (isLogin) {
				memberSysid = getUserData2().getAccount().getSysid();
				uid = getUserData2().getUid();
			}
			String cartKey = "";

			cartKey += inputItemSysid;
			Long quantity = 1L;
			try {
				quantity = Long.parseLong(quStr);
				if (StringUtils.isNotBlank(cartKey)) {
					logger.debug("測試CartKey:" + cartKey);
					// HpsCoreShoppingCart shoppingCart =
					// shoppingCartMap.get(cartKey);// 是否已存在購物車
					HpsCoreShoppingCart shoppingCart = null;
					QueryGroup queryGroup = new QueryGroup(new QueryRule("memberSysid", memberSysid), new QueryRule(
							"vendorSysid", inputVendorSysid), new QueryRule("itemSysid", inputItemSysid));
					List<HpsCoreShoppingCart> l = cloudDao.queryTable(sf(), HpsCoreShoppingCart.class, queryGroup,
							new QueryOrder[0], null, null);
					if (l.size() > 0)
						shoppingCart = l.get(0);
					if (shoppingCart != null) {
						// result = getText("msg.exist");//第一種作法:不新增重複
						Long needQu = shoppingCart.getQuantity() + quantity;
						// if (!item.getIsSet() &&
						// item.getInventoryQuantity().compareTo(new
						// BigDecimal(needQu)) < 0) {
						// resultString = getText("msg.notEnough");
						// return JSON_RESULT;
						// } else if (item.getMaxBuyQuantity().compareTo(needQu)
						// <
						// 0) {
						// resultString = "您所輸入的\"" + item.getName() +
						// "\"商品數量，已超過該商品單次可購買之最大限額(" + item.getMaxBuyQuantity()
						// + ")，請至\"查看購物車中進行調整\"";
						// return JSON_RESULT;
						// } else {
						shoppingCart.setQuantity(needQu); // 第二種做法:累加數量
						// dataList =
						// buildRelationalShoppingData(shoppingCartMap,
						// item, needQu, shoppingCart);
						// }
					} else {
						// if (item.getInventoryQuantity().compareTo(new
						// BigDecimal(quantity)) >= 0 || item.getIsSet()) {
						shoppingCart = new HpsCoreShoppingCart();
						Util.defaultPK(shoppingCart);

						defaultValue(shoppingCart);
						shoppingCart.setItemSysid(inputItemSysid);
						shoppingCart.setQuantity(quantity);
						shoppingCart.setVendorSysid(inputVendorSysid);
						// dataList =
						// buildRelationalShoppingData(shoppingCartMap,
						// item, quantity, shoppingCart);
						// } else {
						// resultString = getText("msg.notEnough");
						// }
					}
					if (isLogin) {
						shoppingCart.setMemberSysid(memberSysid);
						String daoMsg = cloudDao.save(sf(), new Object[] { shoppingCart }, false, null);
						boolean isSuccess = SUCCESS.equals(daoMsg);
						resultMap.put("isSuccess", isSuccess);
						String msg = daoMsg;
						if (isSuccess) {
							if (l.size() == 0) {
								if (shoppingCart.getQuantity() == 1)
									msg = "商品已加入購物車";
								else
									msg = shoppingCart.getQuantity() + "件商品已加入購物車";
							} else {
								msg = "已有購物車此商品，此為第" + shoppingCart.getQuantity() + "件";
							}
						}
						resultMap.put("msg", msg);
					}
					String mainCartSysid = String.copyValueOf(shoppingCart.getSysid().toCharArray());

				} else {
					resultMap.put("isSuccess", false);
					resultMap.put("msg", "[ERROR] without itemSysid");
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				resultMap.put("isSuccess", false);
				resultMap.put("msg", getText("errMsg.inputWrong"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.debug("測試inputCart結束");
		return JSON_RESULT;
	}

	protected String showCartList() {
		Map<String, Map<String, Object>> shoppingCartMap = new LinkedHashMap<String, Map<String, Object>>();
		Map<String, HpsCoreShoppingCart> originalShoppingCartMap = (Map<String, HpsCoreShoppingCart>) session
				.get(Util.SHOPPING_CART_MAP);

		if (originalShoppingCartMap == null || originalShoppingCartMap.size() == 0)
			addActionError(getText("msg.noItem"));
		if (originalShoppingCartMap != null && originalShoppingCartMap.size() > 0)
			for (Entry<String, HpsCoreShoppingCart> entry : originalShoppingCartMap.entrySet()) {
				String quStr = request.getParameter("quantity_" + entry.getValue().getSysid());
				Long newQu = entry.getValue().getQuantity();
				if (StringUtils.isNotBlank(quStr))
					newQu = Long.parseLong(quStr);// 根據頁面數量刷新
				Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
				// HpsVendorItem item =
				// createDataTable(HpsVendorItem.class).get(entry.getValue().getItemSysid());
				HpsVendorItem item = cloudDao.get(sf(), HpsVendorItem.class, entry.getValue().getItemSysid());
				CpsVendor vendor = getDataCpsVendorTable().get(item.getVendorSysid());
				item.setSalesPrice(item.getSalesPrice().setScale(0, BigDecimal.ROUND_HALF_UP));
				item.setSimpleDescription("");//
				item.setDesc("");//
				Long inputQu = entry.getValue().getQuantity();
				if (!inputQu.equals(newQu))
					inputQu = newQu;
				entry.getValue().setQuantity(inputQu);// 從request取得的QU跟現有的QU作比較，以request取得的為主
				BigDecimal price = item.getSalesPrice();

				BigDecimal salesPrice = price.setScale(2, BigDecimal.ROUND_HALF_UP);

				dataMap.put("itemBean", item);
				dataMap.put("vendorBean", vendor);
				dataMap.put("quantity", inputQu);
				dataMap.put("shoppingCart", entry.getValue());
				dataMap.put("salesPrice", salesPrice);
				dataMap.put("totalAmount",
						salesPrice.multiply(new BigDecimal(inputQu)).setScale(2, BigDecimal.ROUND_HALF_UP));
				shoppingCartMap.put(item.getSysid(), dataMap);
			}
		session.put("showShoppingCartMap", shoppingCartMap);
		return SUCCESS;
	}

	public String getCartList() {
		logger.debug("測試 getCartList start");
		Map targetMap = (Map) session.get("showShoppingCartMap");
		if (targetMap == null) {
			// XXX 偷懶
			QueryGroup queryGroup = new QueryGroup(new QueryRule("memberSysid", getUserData2().getAccount().getSysid()));
			List<HpsCoreShoppingCart> cartList = cloudDao.queryTable(sf(), HpsCoreShoppingCart.class, queryGroup,
					new QueryOrder[] { new QueryOrder("vendorSysid"),
							new QueryOrderWithTable("itemSysid", ASC, HpsVendorItem.class, PK, ID) }, null, null);
			cloudDao.save(sf(), new DeleteStatement(HpsCoreShoppingCart.class.getSimpleName(), queryGroup));
			for (HpsCoreShoppingCart hpsCoreShoppingCart : cartList) {
				addItem(hpsCoreShoppingCart.getItemSysid(), hpsCoreShoppingCart.getVendorSysid(), hpsCoreShoppingCart
						.getQuantity().toString());
			}
			targetMap = (Map) session.get("showShoppingCartMap");
		}
		JSONObject obj = new JSONObject(targetMap);
		resultString = obj.toString();
		return JSON_RESULT;
	}

	/**
	 * 修改購物車內產品購買數量
	 * 
	 * @return
	 */
	public String editQuantity2() {
		try {
			String quantityStr = request.getParameter("quantity");
			String inputItemSysid = request.getParameter("itemSysid");
			String inputVendorSysid = request.getParameter("vendorSysid");
			String memberSysid = getUserData2().getAccount().getSysid();

			if (getDataCpsVendorTable().get(inputVendorSysid) == null) {
				resultString = "無效的廠商";
			} else {
				QueryGroup queryGroup = new QueryGroup(new QueryRule("memberSysid", memberSysid), new QueryRule(
						"vendorSysid", inputVendorSysid), new QueryRule("itemSysid", inputItemSysid));
				Map<String, Object> setMap = getUpdatePropertyMap();
				setMap.put("quantity", Long.parseLong(quantityStr));
				resultString = cloudDao.save(sf(), new UpdateStatement(HpsCoreShoppingCart.class.getSimpleName(),
						queryGroup, setMap));
			}
		} catch (NumberFormatException e) {
			resultString = "請輸入整數";
		}
		return JSON_RESULT;
	}

	/**
	 * 將商品自購物車移除
	 *
	 * @return
	 */
	public String deleteItem2() {
		String inputItemSysid = request.getParameter("itemSysid");
		String inputVendorSysid = request.getParameter("vendorSysid");
		String memberSysid = getUserData2().getAccount().getSysid();
		QueryGroup queryGroup = new QueryGroup(new QueryRule("memberSysid", memberSysid), new QueryRule("vendorSysid",
				inputVendorSysid), new QueryRule("itemSysid", inputItemSysid));
		resultString = cloudDao.save(sf(), new DeleteStatement(HpsCoreShoppingCart.class.getSimpleName(), queryGroup));
		return JSON_RESULT;
	}

	public final HpsBillSalesOrder getOrder() {
		return order;
	}

	public final void setOrder(HpsBillSalesOrder order) {
		this.order = order;
	}

	public final String getCountrySelect() {
		return countrySelect;
	}

	public final void setCountrySelect(String countrySelect) {
		this.countrySelect = countrySelect;
	}

	public final String getInvoiceSelect() {
		return invoiceSelect;
	}

	public final void setInvoiceSelect(String invoiceSelect) {
		this.invoiceSelect = invoiceSelect;
	}
}