package tw.com.mitac.thp.bean;
// Generated 2016/11/8 �U�� 01:53:31 by Hibernate Tools 4.3.1.Final

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * BhsArticle generated by hbm2java
 */
@Entity
@Table(name = "bhs_article", catalog = "thp")
public class BhsArticle implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String articleCategory;
	private String forumName;
	private Integer forumCost;
	private Boolean isEnabled;
	private String bhsArticleType;
	private String bhsArticleTypeSysid;
	private String articleTitle;
	private String articleSummary;
	private String articleFull;
	private String articleFullTxt;
	private String articleImageSummary;
	private String articleImage;
	private Date articleDate;
	private Integer dataOrder;
	private String articleWritter;

	public BhsArticle() {
	}

	public BhsArticle(String sysid, String creator, String creationDate, String operator, String operationDate,
			Boolean isEnabled, String bhsArticleType, String articleTitle, String articleSummary) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.isEnabled = isEnabled;
		this.bhsArticleType = bhsArticleType;
		this.articleTitle = articleTitle;
		this.articleSummary = articleSummary;
	}

	public BhsArticle(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String articleCategory, String forumName, Integer forumCost, Boolean isEnabled,
			String bhsArticleType, String bhsArticleTypeSysid, String articleTitle, String articleSummary,
			String articleFull, String articleFullTxt, String articleImageSummary, String articleImage,
			Date articleDate, Integer dataOrder, String articleWritter) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.articleCategory = articleCategory;
		this.forumName = forumName;
		this.forumCost = forumCost;
		this.isEnabled = isEnabled;
		this.bhsArticleType = bhsArticleType;
		this.bhsArticleTypeSysid = bhsArticleTypeSysid;
		this.articleTitle = articleTitle;
		this.articleSummary = articleSummary;
		this.articleFull = articleFull;
		this.articleFullTxt = articleFullTxt;
		this.articleImageSummary = articleImageSummary;
		this.articleImage = articleImage;
		this.articleDate = articleDate;
		this.dataOrder = dataOrder;
		this.articleWritter = articleWritter;
	}

	@Id

	@Column(name = "SYSID", unique = true, nullable = false, length = 45)
	public String getSysid() {
		return this.sysid;
	}

	public void setSysid(String sysid) {
		this.sysid = sysid;
	}

	@Column(name = "CREATOR", nullable = false)
	public String getCreator() {
		return this.creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Column(name = "CREATION_DATE", nullable = false)
	public String getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	@Column(name = "OPERATOR", nullable = false)
	public String getOperator() {
		return this.operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	@Column(name = "OPERATION_DATE", nullable = false)
	public String getOperationDate() {
		return this.operationDate;
	}

	public void setOperationDate(String operationDate) {
		this.operationDate = operationDate;
	}

	@Column(name = "REMARK", length = 65535)
	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "ARTICLE_CATEGORY", length = 2)
	public String getArticleCategory() {
		return this.articleCategory;
	}

	public void setArticleCategory(String articleCategory) {
		this.articleCategory = articleCategory;
	}

	@Column(name = "FORUM_NAME")
	public String getForumName() {
		return this.forumName;
	}

	public void setForumName(String forumName) {
		this.forumName = forumName;
	}

	@Column(name = "FORUM_COST")
	public Integer getForumCost() {
		return this.forumCost;
	}

	public void setForumCost(Integer forumCost) {
		this.forumCost = forumCost;
	}

	@Column(name = "IS_ENABLED", nullable = false)
	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Column(name = "BHS_ARTICLE_TYPE", nullable = false, length = 1)
	public String getBhsArticleType() {
		return this.bhsArticleType;
	}

	public void setBhsArticleType(String bhsArticleType) {
		this.bhsArticleType = bhsArticleType;
	}

	@Column(name = "BHS_ARTICLE_TYPE_SYSID", length = 45)
	public String getBhsArticleTypeSysid() {
		return this.bhsArticleTypeSysid;
	}

	public void setBhsArticleTypeSysid(String bhsArticleTypeSysid) {
		this.bhsArticleTypeSysid = bhsArticleTypeSysid;
	}

	@Column(name = "ARTICLE_TITLE", nullable = false)
	public String getArticleTitle() {
		return this.articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	@Column(name = "ARTICLE_SUMMARY", nullable = false, length = 500)
	public String getArticleSummary() {
		return this.articleSummary;
	}

	public void setArticleSummary(String articleSummary) {
		this.articleSummary = articleSummary;
	}

	@Column(name = "ARTICLE_FULL", length = 65535)
	public String getArticleFull() {
		return this.articleFull;
	}

	public void setArticleFull(String articleFull) {
		this.articleFull = articleFull;
	}

	@Column(name = "ARTICLE_FULL_TXT", length = 65535)
	public String getArticleFullTxt() {
		return this.articleFullTxt;
	}

	public void setArticleFullTxt(String articleFullTxt) {
		this.articleFullTxt = articleFullTxt;
	}

	@Column(name = "ARTICLE_IMAGE_SUMMARY")
	public String getArticleImageSummary() {
		return this.articleImageSummary;
	}

	public void setArticleImageSummary(String articleImageSummary) {
		this.articleImageSummary = articleImageSummary;
	}

	@Column(name = "ARTICLE_IMAGE")
	public String getArticleImage() {
		return this.articleImage;
	}

	public void setArticleImage(String articleImage) {
		this.articleImage = articleImage;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "ARTICLE_DATE", length = 10)
	public Date getArticleDate() {
		return this.articleDate;
	}

	public void setArticleDate(Date articleDate) {
		this.articleDate = articleDate;
	}

	@Column(name = "DATA_ORDER")
	public Integer getDataOrder() {
		return this.dataOrder;
	}

	public void setDataOrder(Integer dataOrder) {
		this.dataOrder = dataOrder;
	}

	@Column(name = "ARTICLE_WRITTER")
	public String getArticleWritter() {
		return this.articleWritter;
	}

	public void setArticleWritter(String articleWritter) {
		this.articleWritter = articleWritter;
	}

}