package tw.com.mitac.thp.action;

public class SearchResult {
	protected String sysid;
	protected String imageSummary;
	protected String nameOri;
	protected String summaryOri;
	protected String name;
	protected String summary;
	protected String url;
	protected String imgUrl;

	public final String getSysid() {
		return sysid;
	}

	public final void setSysid(String sysid) {
		this.sysid = sysid;
	}

	public final String getImageSummary() {
		return imageSummary;
	}

	public final void setImageSummary(String imageSummary) {
		this.imageSummary = imageSummary;
	}

	public final String getNameOri() {
		return nameOri;
	}

	public final void setNameOri(String nameOri) {
		this.nameOri = nameOri;
	}

	public final String getSummaryOri() {
		return summaryOri;
	}

	public final void setSummaryOri(String summaryOri) {
		this.summaryOri = summaryOri;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getSummary() {
		return summary;
	}

	public final void setSummary(String summary) {
		this.summary = summary;
	}

	public final String getUrl() {
		return url;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	public final String getImgUrl() {
		return imgUrl;
	}

	public final void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
}