package tw.com.mitac.thp.login;

import java.math.BigDecimal;

import tw.com.mitac.tenancy.dao.TenancyDAO;
import tw.com.mitac.thp.action.BasisTenancyAction;

public class TenancyAction extends BasisTenancyAction {
	public String execute() {
		String dbDialect = getTenancyData().getTenancy().getDbDialect();
		logger.info("dbDialect:" + dbDialect);
		TenancyDAO tenancyDAO = null;
		if (dbDialect.startsWith("org.hibernate.dialect.MySQL"))
			tenancyDAO = new tw.com.mitac.tenancy.dao.mysql.MysqlTenancyDAOImpl();
		else if (dbDialect.startsWith("org.hibernate.dialect.SQLServer"))
			tenancyDAO = new tw.com.mitac.tenancy.dao.mssql.MssqlTenancyDAOImpl();
		BigDecimal kb = tenancyDAO.queryDbSize(getTenancyData().getTenancySessionFactory(), getTenancyData()
				.getTenancy().getDbCatalog());
		BigDecimal mb = kb.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP);
		request.setAttribute("dbSize", mb.toString() + "MB");
		return SUCCESS;
	}
}