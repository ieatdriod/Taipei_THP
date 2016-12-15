package tw.com.mitac.thp.action;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import tw.com.mitac.thp.bean.MtsDoctor;
import com.opensymphony.xwork2.ActionContext;

/** MTS_FW_002_醫療團隊介紹 */
public class ForDoctorEdsIndexMtsVendorAction extends IndexMtsVendorAction {
	protected MtsDoctor bean;

	public MtsDoctor getBean() {
		return bean;
	}

	public void setBean(MtsDoctor bean) {
		this.bean = bean;
	}

	public String demo() {
		// logger.debug("bean:" +
		// org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(bean,
		// org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE));

		// 查後台語系
		cookieLan = StringUtils.defaultString(cookiesMap.get("language"));

		// 設定前台語系
		if (StringUtils.isNotBlank(cookieLan)) {
			// logger.debug("cookieLan:" + cookieLan);
			String[] arr = cookieLan.split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			ActionContext.getContext().setLocale(locale);
		}

		return mtsDoctorDemo(bean, false);
	}
}