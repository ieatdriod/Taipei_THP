package tw.com.mitac.thp.tree;

import java.util.ArrayList;
import java.util.List;

public class BeanTreeNode<MO> {
	protected MO bean;
	protected List<BeanTreeNode> sub = new ArrayList<BeanTreeNode>();

	public final MO getBean() {
		return bean;
	}

	public final void setBean(MO bean) {
		this.bean = bean;
	}

	public final List<BeanTreeNode> getSub() {
		return sub;
	}

	public final void setSub(List<BeanTreeNode> sub) {
		this.sub = sub;
	}
}