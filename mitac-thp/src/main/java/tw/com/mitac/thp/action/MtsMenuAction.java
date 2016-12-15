package tw.com.mitac.thp.action;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.thp.bean.MtsMenu;
import tw.com.mitac.thp.util.Util;

public class MtsMenuAction extends DetailController<MtsMenu> {
	@Override
	public String getTreeParentKey() {
		return "parentMtsMenuSysid";
	}

	@Override
	public String[] getImgCols() {
		return new String[] { "menuImg" };
	}

	public String ajaxTreeTypeBInsert() throws Exception {
		String name = request.getParameter("name");
		String menuType = request.getParameter("menuType");
		String parentKey = request.getParameter("parentKey");
		String dataorder = request.getParameter("dataorder");
		bean = new MtsMenu();
		Util.defaultPK(bean);
		defaultValue(bean);
		bean.setName(name);
		bean.setParentMtsMenuSysid("");
		if (StringUtils.isNotBlank(parentKey))
			bean.setParentMtsMenuSysid(parentKey);
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