package tw.com.mitac.thp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.thp.bean.BhsMenu;
import tw.com.mitac.thp.util.Util;

public class BhsMenuAction extends BasisCrudAction<BhsMenu> {
	@Override
	public String[] getImgCols() {
		return new String[] { "menuImg" };
	}

	@Override
	public String getTreeParentKey() {
		return "parentBhsMenuSysid";
	}

	@Override
	protected boolean executeSave() {
		// 檢核
		if (bean.getSysid().equals(bean.getParentBhsMenuSysid())) {
			addActionError("上層分類不可挑選本身");
			return false;
		}

		Map<String, BhsMenu> bhsMenus = createDataTable(BhsMenu.class);
		String key = bean.getParentBhsMenuSysid();
		while (StringUtils.isNotBlank(key)) {
			BhsMenu menu = bhsMenus.get(key);
			key = menu.getParentBhsMenuSysid();
			if (bean.getSysid().equals(key)) {
				addActionError("上層分類形成回圈");
				return false;
			}
		}

		Map<String, BhsMenu> bhsMenus2 = new LinkedHashMap<String, BhsMenu>(bhsMenus);
		bhsMenus2.put(bean.getSysid(), bean);
		for (String sysid : bhsMenus2.keySet()) {
			String parentSysid = bhsMenus2.get(sysid).getParentBhsMenuSysid();
			Integer maxLv = 0;
			while (StringUtils.isNotBlank(parentSysid)) {
				parentSysid = bhsMenus2.get(parentSysid).getParentBhsMenuSysid();
				maxLv++;
				if (maxLv >= getTreeMaxLevel()) {
					addActionError("主選單分類超過" + getTreeMaxLevel() + "層");
					return false;
				}
			}
		}

		return super.executeSave();
	}

	public String ajaxTreeTypeBInsert() throws Exception {
		String name = request.getParameter("name");
		String menuType = request.getParameter("menuType");
		String parentKey = request.getParameter("parentKey");
		String dataorder = request.getParameter("dataorder");
		bean = new BhsMenu();
		Util.defaultPK(bean);
		defaultValue(bean);
		bean.setName(name);
		bean.setParentBhsMenuSysid("");
		if (StringUtils.isNotBlank(parentKey))
			bean.setParentBhsMenuSysid(parentKey);
		bean.setMenuType(menuType);
		try {
			bean.setDataOrder(Integer.parseInt(dataorder));
		} catch (NumberFormatException e) {
			bean.setDataOrder(0);
		}
		saveList.add(bean);
		for (String lan : getLanguageTypeMap().keySet()) {
			String nameValue = bean.getName();
			if (!StringUtils.equals(lan, getCookieLan()))
				nameValue = request.getParameter("name_" + lan);
			if (StringUtils.isNotBlank(nameValue)) {
				Object sysMultiLanguage = ConstructorUtils.invokeConstructor(multiLanClass(lan), null);
				Util.defaultPK(sysMultiLanguage);
				defaultValue(sysMultiLanguage);
				PropertyUtils.setProperty(sysMultiLanguage, "sourceSysid", bean.getSysid());
				PropertyUtils.setProperty(sysMultiLanguage, "sourceTable", getPersistentClass().getSimpleName());
				PropertyUtils.setProperty(sysMultiLanguage, "sourceColumn", "name");
				PropertyUtils.setProperty(sysMultiLanguage, "columnDatatype", "STRING");
				PropertyUtils.setProperty(sysMultiLanguage, "columnLength", 255L);
				PropertyUtils.setProperty(sysMultiLanguage, "columnValueString", nameValue);
				saveList.add(sysMultiLanguage);
			}
		}
		resultString = cloudDao.save(sf(), saveList.toArray(), false, "INSERT");
		if (StringUtils.equals(SUCCESS, resultString))
			resetDataMap(getPersistentClass());
		return JSON_RESULT;
	}
}