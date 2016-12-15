package tw.com.mitac.thp.sys;

import org.apache.commons.lang3.StringUtils;

import tw.com.mitac.hibernate.DeleteStatement;
import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.jqgrid.bean.SysColumnConfig;
import tw.com.mitac.thp.action.BasisLoginAction;

public class SysColumnConfigConsoleAction extends BasisLoginAction {
	@Override
	protected String getMiaaInitUrl() {
		return "sys/sysColumnConfigConsole";
	}

	public String delSccByTbNm() {
		QueryGroup queryGroup = QueryGroup.DEFAULT;

		String tablename = request.getParameter("tablename");
		if (StringUtils.isNotBlank(tablename))
			queryGroup = new tw.com.mitac.hibernate.QueryGroup(new tw.com.mitac.hibernate.QueryRule("tablename",
					tablename));

		resultString = cloudDao.save(sf(), new DeleteStatement(SysColumnConfig.class.getSimpleName(), queryGroup));
		if (SUCCESS.equals(resultString)) {
			appMap().remove("systemColumnConfigCreator");
			session.remove("systemColumnConfigCreator");
			session.remove("userColumnConfigCreator");
			session.remove("columnConfigCreator");
		}

		// try {
		// Class<?> clazz = Class.forName(tw.com.mitac.thp.util.Util.beanPackage
		// + "." + tablename);
		// resetDataMap(clazz);
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }

		return JSON_RESULT;
	}
}