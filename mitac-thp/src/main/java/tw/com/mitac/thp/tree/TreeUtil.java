package tw.com.mitac.thp.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TreeUtil {
	protected static Logger logger = Logger.getLogger(TreeUtil.class);

	protected static boolean treeMatch(Map treeObj, String key, String parentKey) {
		Map<String, Object> tempSpace = (Map<String, Object>) treeObj.get("tempSpace");
		Map<String, Boolean> leafRecords = (Map<String, Boolean>) treeObj.get("leafRecords");
		Map<String, Integer> levelRecords = (Map<String, Integer>) treeObj.get("levelRecords");
		Map<String, BeanTreeNode> beanTreeNodeMap = (Map<String, BeanTreeNode>) treeObj.get("beanTreeNodeMap");
		List<BeanTreeNode> rootList = (List<BeanTreeNode>) treeObj.get("rootList");
		// console.log('treeMatch start where key:'+key+' nameMsg:'+nameMsg);
		if (StringUtils.isBlank(parentKey)) {
			// console.log('tree root point');
			levelRecords.put(key, 0);
		} else {
			if (!Integer.class.isInstance(levelRecords.get(parentKey))) {
				// console.log('treeMatch end where not match');
				return false;
			}

			levelRecords.put(key, levelRecords.get(parentKey) + 1);
			leafRecords.put(parentKey, false);

			if ((int) treeObj.get("maxLevelRecord") < levelRecords.get(key))
				treeObj.put("maxLevelRecord", levelRecords.get(key));
		}
		leafRecords.put(key, true);
		// console.log('treeObj.levelRecords[key]:' +
		// treeObj.levelRecords[key]);

		BeanTreeNode beanTreeNode = new BeanTreeNode();
		beanTreeNodeMap.put(key, beanTreeNode);
		beanTreeNode.setBean(tempSpace.get(key));

		BeanTreeNode parentBeanTreeNode = beanTreeNodeMap.get(parentKey);
		if (parentBeanTreeNode != null)
			parentBeanTreeNode.getSub().add(beanTreeNode);
		else
			rootList.add(beanTreeNode);

		tempSpace.remove(key);
		// console.log('treeMatch end');

		return true;
	}

	public static List<BeanTreeNode> treeMachine(Map treeObj, String treeParentKey, String dataIdKey) throws Exception {
		logger.debug("treeMachine start");
		Collection request = (Collection) treeObj.get("request");
		if (treeObj.get("tempSpace") == null)
			treeObj.put("tempSpace", new LinkedHashMap());
		if (treeObj.get("leafRecords") == null)
			treeObj.put("leafRecords", new HashMap());
		if (treeObj.get("levelRecords") == null)
			treeObj.put("levelRecords", new HashMap());
		if (treeObj.get("maxLevelRecord") == null)
			treeObj.put("maxLevelRecord", 0);
		if (treeObj.get("beanTreeNodeMap") == null)
			treeObj.put("beanTreeNodeMap", new HashMap());
		if (treeObj.get("rootList") == null)
			treeObj.put("rootList", new ArrayList());
		Map<String, Object> tempSpace = (Map<String, Object>) treeObj.get("tempSpace");
		Map<String, Boolean> leafRecords = (Map<String, Boolean>) treeObj.get("leafRecords");
		Map<String, Integer> levelRecords = (Map<String, Integer>) treeObj.get("levelRecords");
		Map<String, BeanTreeNode> beanTreeNodeMap = (Map<String, BeanTreeNode>) treeObj.get("beanTreeNode");
		List<BeanTreeNode> rootList = (List<BeanTreeNode>) treeObj.get("rootList");

		int count = request.size();
		for (Object object : request) {
			String key = (String) PropertyUtils.getProperty(object, dataIdKey);
			tempSpace.put(key, object);
		}

		if (count == 0) {
			logger.debug("no data");
		}

		for (int i = 0; i < 100; i++) {// 避免無窮迴圈
			logger.debug("-- treeMachine counter:" + i + " start --");
			int successCount = 0;
			Set<String> keySet = new LinkedHashSet(tempSpace.keySet());
			for (String key : keySet) {
				// console.log('key:' + key);
				String parentKey = (String) PropertyUtils.getProperty(tempSpace.get(key), treeParentKey);

				boolean isMatch = treeMatch(treeObj, key, parentKey);
				if (isMatch) {
					count--;
					successCount++;
				}
			}
			logger.debug("count:" + count + " successCount:" + successCount);
			if (count <= 0 || successCount == 0) {
				logger.debug("-- treeMachine data break --");
				break;
			}
			logger.debug("-- treeMachine counter:" + i + " end --");
		}
		logger.debug("treeMachine end");
		return rootList;
	}

	public static List<BeanTreeNode> findWithSub(List<BeanTreeNode> nodeList) {
		List<BeanTreeNode> withSubList = new ArrayList<BeanTreeNode>(nodeList);
		for (BeanTreeNode beanTreeNode : nodeList) {
			withSubList.addAll(findWithSub(beanTreeNode.getSub()));
		}
		return withSubList;
	}

	public static BeanTreeNode findNodeByEq(List<BeanTreeNode> nodeList, String eqProperty, Object eqValue) {
		if (nodeList == null || nodeList.size() == 0)
			return null;
		for (BeanTreeNode beanTreeNode : nodeList) {
			Object bean = beanTreeNode.getBean();
			try {
				Object beanValue = PropertyUtils.getProperty(bean, eqProperty);
				if (ObjectUtils.equals(beanValue, eqValue))
					return beanTreeNode;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (BeanTreeNode beanTreeNode : nodeList) {
			BeanTreeNode node = findNodeByEq(beanTreeNode.getSub(), eqProperty, eqValue);
			if (node != null)
				return node;
		}
		return null;
	}
}