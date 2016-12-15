package tw.com.mitac.thp.dao;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Repository
public class CoreCylinderReportDAO {
	protected Logger logger = Logger.getLogger(this.getClass());
	private SessionFactory sessionFactory;

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}