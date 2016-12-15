package tw.com.mitac.thp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.hibernate.UpdateStatement;
import tw.com.mitac.miaa.bean.Miaa08RoleUid;
import tw.com.mitac.thp.bean.BhsVendorProfile;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMemberForVendor;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.bean.MtsVendorProfile;
import tw.com.mitac.thp.util.Util;

public class CpsVendorAction extends DetailController<CpsVendor> {
	@Override
	public LinkedHashMap<String, DetailInfo> getDetailInfoMap() {
		LinkedHashMap<String, DetailInfo> detailClassMap = super.getDetailInfoMap();
		detailClassMap.put("", new DetailInfo("", DETAIL_SET, "detail", CpsMemberForVendor.class));
		return detailClassMap;
	}

	@Override
	protected QueryGroup getQueryRestrict() {
		if (CpsEntity.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			if (getUserAccount().getSourceSysid().equals(getCpsEntitySysid())) {
				return QueryGroup.DEFAULT;
			} else {
				return new QueryGroup(new QueryRule("entitySysid", getUserAccount().getSourceSysid()));
			}
		} else if (CpsVendor.class.getSimpleName().equals(getUserAccount().getSourceType())) {
			return new QueryGroup(new QueryRule(PK, "x"));
		} else {
			return new QueryGroup(new QueryRule(PK, "x"));
		}
	}

	@Override
	protected boolean executeSave() {
		boolean boo = super.executeSave();
		if (boo) {
			// 供應商更名時更新bhs/mts的vendorName以保持同步
			logger.info("auto gen lan info");
			saveList.clear();
			for (Class<?> clazz : new Class[] { MtsVendorProfile.class, BhsVendorProfile.class }) {
				List<String> sysidList = (List<String>) cloudDao.findProperty(sf(), clazz, new QueryGroup(
						new QueryRule("vendorSysid", bean.getSysid())), null, false, PK);
				if (sysidList.size() == 0)
					continue;
				resetDataMap(clazz);
				String sysid = sysidList.get(0);

				Map<String, Object> setMap = getUpdatePropertyMap();
				setMap.put("vendorName", bean.getName());
				saveList.add(new UpdateStatement(clazz.getSimpleName(), new QueryGroup(new QueryRule(PK, sysid)),
						setMap));

				try {
					String sourceTable = clazz.getSimpleName();
					for (String lan : languageTypeMap.keySet()) {
						List sysMultiLanguageList = cloudDao.queryTable(sf(), multiLanClass(lan), new QueryGroup(
								new QueryRule("sourceTable", sourceTable), new QueryRule("sourceSysid", sysid),
								new QueryRule("sourceColumn", "vendorName")), new QueryOrder[0], null, null);

						String sysMultiLanguageKey = NAME + "_" + lan;
						String lanValue = beaninfo.get(sysMultiLanguageKey);
						if (lan.equals(getCookieLan()))
							lanValue = (String) PropertyUtils.getProperty(bean, NAME);
						logger.debug("beaninfo.name_" + lan + "=" + lanValue);
						if (StringUtils.isNotBlank(lanValue)) {
							Object sysMultiLanguage = null;
							if (sysMultiLanguageList.size() > 0) {
								sysMultiLanguage = sysMultiLanguageList.get(0);
							} else {
								sysMultiLanguage = ConstructorUtils.invokeConstructor(multiLanClass(lan), null);
								Util.defaultPK(sysMultiLanguage);
								PropertyUtils.setProperty(sysMultiLanguage, "sourceSysid", sysid);
								PropertyUtils.setProperty(sysMultiLanguage, "sourceTable", sourceTable);
								PropertyUtils.setProperty(sysMultiLanguage, "sourceColumn", "vendorName");
								PropertyUtils.setProperty(sysMultiLanguage, "columnDatatype", "STRING");
								PropertyUtils.setProperty(sysMultiLanguage, "columnLength", 255L);
							}
							defaultValue(sysMultiLanguage);
							PropertyUtils.setProperty(sysMultiLanguage, "columnValueString", lanValue);
							saveList.add(sysMultiLanguage);
						} else {
							if (sysMultiLanguageList.size() > 0) {
								sysMultiLanguageList.clear();
								saveList.add(new DeleteStatement(multiLanClassName(lan), new QueryGroup(new QueryRule(
										"sourceTable", sourceTable), new QueryRule("sourceSysid", sysid),
										new QueryRule("sourceColumn", "vendorName"))));
							}
						}
					}// end of languageTypeMap
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cloudDao.save(sf(), saveList);

		}
		return boo;
	}

	protected void jqgridDetailDel(String resourceName, Class clazz) {
		if (DETAIL_SET.equals(resourceName)) {
			Set<CpsMemberForVendor> delSet = jqgridDetailSelectedSet(resourceName, clazz);
			List<String> uuidList = new ArrayList<String>();
			for (CpsMemberForVendor cpsMemberForVendor : delSet) {
				uuidList.add(cpsMemberForVendor.getUuid());
			}
			if (uuidList.size() > 0) {
				cloudDao.save(sf(), new Object[] {
						new DeleteStatement(Miaa08RoleUid.class.getSimpleName(), new QueryGroup(new QueryRule("uid",
								IN, uuidList))),
						new DeleteStatement(CpsMemberForVendor.class.getSimpleName(), new QueryGroup(new QueryRule(
								"uuid", IN, uuidList))) });
			}
		}
		super.jqgridDetailDel(resourceName, clazz);
	}
}