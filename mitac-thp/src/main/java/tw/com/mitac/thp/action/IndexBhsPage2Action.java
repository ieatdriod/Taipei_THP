package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.BhsArticleType;
import tw.com.mitac.thp.bean.BhsProducts;
import tw.com.mitac.thp.bean.BhsProductsCategoryLink;
import tw.com.mitac.thp.bean.BhsRecommand;
import tw.com.mitac.thp.bean.BhsRecommandItem;
import tw.com.mitac.thp.bean.BhsRecommandLink;
import tw.com.mitac.thp.bean.BhsRecommandLinkItem;
import tw.com.mitac.thp.bean.BhsTechnology;
import tw.com.mitac.thp.bean.CpsVendor;

public class IndexBhsPage2Action extends BasisTenancyAction {
	public String indexBhsPage() {
		// 產業新知
		List<BhsArticleType> bhsArticleTypeList = findBhsArticleTypeList();
		request.setAttribute("bhsArticleTypeList", bhsArticleTypeList);

		String bhsArticleTypeSysid = request.getParameter("bhsArticleTypeSysid");
		if (StringUtils.isBlank(bhsArticleTypeSysid) && bhsArticleTypeList.size() > 0) {
			BhsArticleType firstArticleType = bhsArticleTypeList.get(0);
			bhsArticleTypeSysid = firstArticleType.getSysid();
		}

		if (StringUtils.isNotBlank(bhsArticleTypeSysid)) {
			request.setAttribute("articleTypeShowIndex", bhsArticleTypeSysid);
		}
		// ---------- ---------- ---------- ---------- ----------
		String key1 = "data" + BhsRecommandLink.class.getSimpleName() + "_" // 自動重置資源
				+ "data" + BhsProducts.class.getSimpleName() + "_" // 自動重置資源
				+ "data" + BhsTechnology.class.getSimpleName() + "_" // 自動重置資源
				+ "data" + CpsVendor.class.getSimpleName() + "_" // 自動重置資源
				+ "brl" + "_" + getCookieLan();
		List<BhsRecommand> brl = (List<BhsRecommand>) appMap().get(key1);
		if (brl == null) {
			brl = cloudDao.queryTable(sf(), BhsRecommand.class, new QueryGroup(new QueryRule(IS_ENABLED, true)),
					new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			logger.debug("2016/12/26-NullPointerException-BhsRecommand"+brl.size());
			addMultiLan(brl, sf(), BhsRecommand.class);
			appMap().put(key1, brl);

			Map<String, List> categorySysidMap = null;

			// < BhsRecommandLink.sysid , BhsRecommand >
			Map<String, BhsRecommand> bhsRecommandMap = new HashMap<String, BhsRecommand>();

			List<BhsRecommandLink> brll = cloudDao.queryTable(sf(), BhsRecommandLink.class, new QueryGroup(
					new QueryRule(IS_ENABLED, true)), new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			addMultiLan(brll, sf(), BhsRecommandLink.class);
			for (BhsRecommandLink bhsRecommandLink : brll) {
				for (BhsRecommand bhsRecommand : brl) {
					if (bhsRecommandLink.getRecommandSysid().equals(bhsRecommand.getSysid())) {
						if (bhsRecommand.getDetailSet2() == null)
							bhsRecommand.setDetailSet2(new LinkedHashSet<BhsRecommandLinkItem>());
						try {
							BhsRecommandLinkItem brli = new BhsRecommandLinkItem();
							PropertyUtils.copyProperties(brli, bhsRecommandLink);
							bhsRecommand.getDetailSet2().add(brli);
						} catch (Exception e) {
						}
						bhsRecommandMap.put(bhsRecommandLink.getSysid(), bhsRecommand);
						break;
					}
				}
			}

			// < BhsRecommandLink.sysid , List<List(3)> >
			Map<String, List<List<IndexBhsRecommand>>> bhsRecommandGroupMap = new HashMap<String, List<List<IndexBhsRecommand>>>();
			appMap().put("bhsRecommandGroupMap" + "_" + getCookieLan(), bhsRecommandGroupMap);

			List<BhsRecommandItem> bhsRecommandItemList = cloudDao.queryTable(sf(), BhsRecommandItem.class,
					QueryGroup.DEFAULT, new QueryOrder[] { new QueryOrder(DATA_ORDER) }, null, null);
			for (BhsRecommandItem bhsRecommandItem : bhsRecommandItemList) {
				boolean isL3 = true;
				
				for (BhsRecommand bhsRecommand : brl) {
					if (bhsRecommandItem.getParentSysid().equals(bhsRecommand.getSysid())) {
						if (bhsRecommand.getDetailSet() == null)
							bhsRecommand.setDetailSet(new LinkedHashSet<BhsRecommandItem>());
						bhsRecommand.getDetailSet().add(bhsRecommandItem);
						isL3 = false;
						break;
					}
					
				}

				if (isL3) {
					BhsRecommand bhsRecommand = bhsRecommandMap.get(bhsRecommandItem.getParentSysid());
					if (bhsRecommand == null)
						continue;
					String vendorSysid = "";
					int smailListSize = 3;
					BhsProducts bhsProducts = null;
					BhsTechnology bhsTechnology = null;
					if (StringUtils.equalsIgnoreCase("P", bhsRecommand.getRecommandType())) {
						bhsProducts = getDataBhsProductsTable().get(bhsRecommandItem.getSourceSysid());
						if (bhsProducts == null || !bhsProducts.getIsEnabled())
							continue;
						vendorSysid = bhsProducts.getVendorSysid();
					} else if (StringUtils.equalsIgnoreCase("T", bhsRecommand.getRecommandType())) {
						bhsTechnology = getDataBhsTechnologyTable().get(bhsRecommandItem.getSourceSysid());
						if (bhsTechnology == null || !bhsTechnology.getIsEnabled())
							continue;
						vendorSysid = bhsTechnology.getVendorSysid();
						smailListSize = 2;
					}
					CpsVendor cpsVendor = getDataCpsVendorTable().get(vendorSysid);
					if (cpsVendor == null || !cpsVendor.getIsEnabled())
						continue;

					List<List<IndexBhsRecommand>> bigList = bhsRecommandGroupMap.get(bhsRecommandItem.getParentSysid());
					if (bigList == null) {
						bigList = new ArrayList<List<IndexBhsRecommand>>();
						bhsRecommandGroupMap.put(bhsRecommandItem.getParentSysid(), bigList);

						bigList.add(new ArrayList<IndexBhsRecommand>());
					}

					List<IndexBhsRecommand> smailList = bigList.get(bigList.size() - 1);
					if (smailList.size() == smailListSize) {
						smailList = new ArrayList<IndexBhsRecommand>();
						bigList.add(smailList);
					}

					if (StringUtils.equalsIgnoreCase("P", bhsRecommand.getRecommandType())) {
						IndexBhsRecommand ibr = new IndexBhsRecommand();

						ibr.setUrl(request.getContextPath() + "/pages2/BHS_FW_004?vendorSysid="
								+ bhsProducts.getSysid());
						ibr.setImgPath("/" + getWebDfImg() + "/" + BhsProducts.class.getSimpleName() + "/"
								+ bhsProducts.getSysid() + "/" + bhsProducts.getProductsImageSummary1());
						ibr.setItemName(bhsProducts.getName());
						ibr.setFavoMarkId("FavoMark-BHS-" + bhsProducts.getVendorSysid() + "-"
								+ bhsRecommand.getRecommandType() + "-" + bhsRecommandItem.getSourceSysid() + "_"
								+ bhsRecommandItem.getSysid());

						if (categorySysidMap == null) {
							categorySysidMap = new HashMap<String, List>();

							List<Map> categorySysidAllList = (List<Map>) cloudDao.findProperty(sf(),
									BhsProductsCategoryLink.class, QueryGroup.DEFAULT, null, false, "parentSysid",
									"categorySysid");
							for (Map<String, String> map : categorySysidAllList) {
								String key = map.get("parentSysid");
								String categorySysid = map.get("categorySysid");
								List<String> categorySysidList = categorySysidMap.get(key);
								if (categorySysidList == null) {
									categorySysidList = new ArrayList<String>();
									categorySysidMap.put(key, categorySysidList);
								}
								categorySysidList.add(categorySysid);
							}
						}

						// List<String> categorySysidList =
						// categorySysidMap.get(bhsProducts.getSysid());
						// if (categorySysidList != null)
						// for (String categorySysid :
						// categorySysidList) {
						// BhsProductsCategory bhsProductsCategory =
						// getDataBhsProductsCategoryTable()
						// .get(categorySysid);
						// if (bhsProductsCategory != null)
						// ibr.getItemInfos().add("·" +
						// bhsProductsCategory.getName());
						// }
						if (StringUtils.isNotBlank(bhsProducts.getProductsSummary1())) {
							ibr.getItemInfos().add(
									"<span style='font-size:5px;'>●<span>&nbsp;" + bhsProducts.getProductsSummary1());
						} else {
							ibr.getItemInfos().add("");
						}
						if (StringUtils.isNotBlank(bhsProducts.getProductsSummary2())) {
							ibr.getItemInfos().add(
									"<span style='font-size:5px;'>●<span>&nbsp;" + bhsProducts.getProductsSummary2());
						} else {
							ibr.getItemInfos().add("");
						}
						if (StringUtils.isNotBlank(bhsProducts.getProductsSummary3())) {
							ibr.getItemInfos().add(
									"<span style='font-size:5px;'>●<span>&nbsp;" + bhsProducts.getProductsSummary3());
						} else {
							ibr.getItemInfos().add("");
						}

						// ibr.getItemInfos().add("·" +
						// bhsProducts.getProductsSummary1());
						// ibr.getItemInfos().add("·" +
						// bhsProducts.getProductsSummary2());
						// ibr.getItemInfos().add("·" +
						// bhsProducts.getProductsSummary3());

						smailList.add(ibr);
					} else if (StringUtils.equalsIgnoreCase("T", bhsRecommand.getRecommandType())) {
						IndexBhsRecommand ibr = new IndexBhsRecommand();

						ibr.setUrl(request.getContextPath() + "/pages2/BHS_FW_004?vendorSysid="
								+ bhsTechnology.getSysid());
						ibr.setImgPath("/" + getWebDfImg() + "/" + BhsTechnology.class.getSimpleName() + "/"
								+ bhsTechnology.getSysid() + "/" + bhsTechnology.getTechnologySummaryImg());
						ibr.setItemName(bhsTechnology.getName());
						ibr.setFavoMarkId("FavoMark-BHS-" + bhsTechnology.getVendorSysid() + "-"
								+ bhsRecommand.getRecommandType() + "-" + bhsRecommandItem.getSourceSysid() + "_"
								+ bhsRecommandItem.getSysid());
						ibr.getItemInfos().add(bhsTechnology.getTechnologySummary());

						smailList.add(ibr);
					}
				}
			}

		}
		request.setAttribute("brl", brl);
		request.setAttribute("bhsRecommandGroupMap", appMap().get("bhsRecommandGroupMap" + "_" + getCookieLan()));

		return SUCCESS;
	}

	/**
	 * 產業新知分類
	 */
	protected List<BhsArticleType> findBhsArticleTypeList() {
		List<BhsArticleType> bhsArticleTypeList = new ArrayList<BhsArticleType>(getDataBhsArticleTypeTable().values());
		return bhsArticleTypeList;
	}
}