package tw.com.mitac.thp.job;

import java.util.ArrayList;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <pre>
 * 重置權限屬性
 * </pre>
 * 
 * 用於開發階段，定期重置Miaa03FileProperty.FIELD_ALL_ALLOW，使其未來運行時將filePropertyName修正為中文
 */
public class Miaa03FilePropertyResetTask extends BasisJob {
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		List<Object> saveList = new ArrayList<Object>();

		java.util.Map<String, Object> setMap = new java.util.HashMap<String, Object>();
		setMap.put("filePropertyName", tw.com.mitac.miaa.Util.FIELD_ALL_ALLOW);
		saveList.add(new tw.com.mitac.hibernate.UpdateStatement(tw.com.mitac.miaa.bean.Miaa03FileProperty.class
				.getSimpleName(), new tw.com.mitac.hibernate.QueryGroup(new tw.com.mitac.hibernate.QueryRule(
				"filePropertyId", tw.com.mitac.miaa.Util.FIELD_ALL_ALLOW)), setMap));

		if (saveList.size() > 0)
			cloudDao.save(sessionFactory, saveList);
	}
}