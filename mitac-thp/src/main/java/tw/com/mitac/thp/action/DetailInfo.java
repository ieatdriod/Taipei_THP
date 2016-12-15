package tw.com.mitac.thp.action;

public class DetailInfo {
	protected String detailKey;
	protected String detailResource;
	protected String detailI18nKey;
	protected Class<?> detailClass;

	public DetailInfo(String detailKey, String detailResource, String detailI18nKey, Class<?> detailClass) {
		this.detailKey = detailKey;
		this.detailResource = detailResource;
		this.detailI18nKey = detailI18nKey;
		this.detailClass = detailClass;
	}

	public final String getDetailKey() {
		return detailKey;
	}

	public final String getDetailResource() {
		return detailResource;
	}

	public final String getDetailI18nKey() {
		return detailI18nKey;
	}

	public final Class<?> getDetailClass() {
		return detailClass;
	}
}