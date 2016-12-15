package tw.com.mitac.thp.action;

import java.util.ArrayList;
import java.util.List;

public class IndexBhsRecommand {
	protected String url;
	protected String imgPath;
	protected String itemName;
	protected String favoMarkId;
	protected List<String> itemInfos = new ArrayList<String>();

	public final String getUrl() {
		return url;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	public final String getImgPath() {
		return imgPath;
	}

	public final void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public final String getItemName() {
		return itemName;
	}

	public final void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public final List<String> getItemInfos() {
		return itemInfos;
	}

	public final void setItemInfos(List<String> itemInfos) {
		this.itemInfos = itemInfos;
	}

	public final String getFavoMarkId() {
		return favoMarkId;
	}

	public final void setFavoMarkId(String favoMarkId) {
		this.favoMarkId = favoMarkId;
	}
}