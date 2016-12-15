package tw.com.mitac.thp.login;

import org.hibernate.SessionFactory;

import tw.com.mitac.tenancy.bean.MtMultiTenancy;

public class TenancyData implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	protected MtMultiTenancy tenancy;
	protected SessionFactory tenancySessionFactory;

	public TenancyData(MtMultiTenancy tenancy, SessionFactory tenancySessionFactory) {
		this.tenancy = tenancy;
		this.tenancySessionFactory = tenancySessionFactory;
	}

	public final MtMultiTenancy getTenancy() {
		return tenancy;
	}

	public final SessionFactory getTenancySessionFactory() {
		return tenancySessionFactory;
	}
}