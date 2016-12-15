package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;

import tw.com.mitac.thp.bean.CpsEpaper;

public class EpaperAction extends BasisTenancyAction {
	public String execute() {
		String q = request.getParameter("q");
		CpsEpaper cpsEpaper = cloudDao.get(sf(), CpsEpaper.class, q);
		if (cpsEpaper != null) {
			List<CpsEpaper> l = new ArrayList<CpsEpaper>();
			addMultiLan(l, sf(), CpsEpaper.class);

			request.setAttribute("title", cpsEpaper.getTitle());
			request.setAttribute("body", cpsEpaper.getContent());
		}
		return SUCCESS;
	}
}