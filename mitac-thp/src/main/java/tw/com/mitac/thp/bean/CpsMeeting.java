package tw.com.mitac.thp.bean;
// Generated 2016/11/25 �U�� 02:09:34 by Hibernate Tools 4.3.1.Final

import java.sql.Time;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * CpsMeeting generated by hbm2java
 */
@Entity
@Table(name = "cps_meeting", catalog = "thp")
public class CpsMeeting implements java.io.Serializable {

	private String sysid;
	private String creator;
	private String creationDate;
	private String operator;
	private String operationDate;
	private String remark;
	private String entitySysid;
	private String meetingType;
	private String meetingRoomId;
	private Date meetingDate;
	/**介接要求 Time*/
	private Time meetingStartTime;
	private String meetingSubject;
	private String sourceId;
	private String meetingSession;
	private Boolean meetingStatus;
	private String videoConferenceId;
	private String videoConferenceAddress;
	private String meetingPpt;
	private String startUrl;
	
	private java.util.Set<CpsMeetingAttendance> detailSet;

	public java.util.Set<CpsMeetingAttendance> getDetailSet() {
		return detailSet;
	}

	public void setDetailSet(java.util.Set<CpsMeetingAttendance> detailSet) {
		this.detailSet = detailSet;
	}

	public CpsMeeting() {
	}

	public CpsMeeting(String sysid, String creator, String creationDate, String operator, String operationDate,
			Date meetingDate, Time meetingStartTime, String meetingSubject, String sourceId, String meetingSession,
			Boolean meetingStatus) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.meetingDate = meetingDate;
		this.meetingStartTime = meetingStartTime;
		this.meetingSubject = meetingSubject;
		this.sourceId = sourceId;
		this.meetingSession = meetingSession;
		this.meetingStatus = meetingStatus;
	}

	public CpsMeeting(String sysid, String creator, String creationDate, String operator, String operationDate,
			String remark, String entitySysid, String meetingType, String meetingRoomId, Date meetingDate,
			Time meetingStartTime, String meetingSubject, String sourceId, String meetingSession, Boolean meetingStatus,
			String videoConferenceId, String videoConferenceAddress, String meetingPpt, String startUrl) {
		this.sysid = sysid;
		this.creator = creator;
		this.creationDate = creationDate;
		this.operator = operator;
		this.operationDate = operationDate;
		this.remark = remark;
		this.entitySysid = entitySysid;
		this.meetingType = meetingType;
		this.meetingRoomId = meetingRoomId;
		this.meetingDate = meetingDate;
		this.meetingStartTime = meetingStartTime;
		this.meetingSubject = meetingSubject;
		this.sourceId = sourceId;
		this.meetingSession = meetingSession;
		this.meetingStatus = meetingStatus;
		this.videoConferenceId = videoConferenceId;
		this.videoConferenceAddress = videoConferenceAddress;
		this.meetingPpt = meetingPpt;
		this.startUrl = startUrl;
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

	@Column(name = "ENTITY_SYSID", length = 45)
	public String getEntitySysid() {
		return this.entitySysid;
	}

	public void setEntitySysid(String entitySysid) {
		this.entitySysid = entitySysid;
	}

	@Column(name = "MEETING_TYPE", length = 1)
	public String getMeetingType() {
		return this.meetingType;
	}

	public void setMeetingType(String meetingType) {
		this.meetingType = meetingType;
	}

	@Column(name = "MEETING_ROOM_ID")
	public String getMeetingRoomId() {
		return this.meetingRoomId;
	}

	public void setMeetingRoomId(String meetingRoomId) {
		this.meetingRoomId = meetingRoomId;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "MEETING_DATE", nullable = false, length = 10)
	public Date getMeetingDate() {
		return this.meetingDate;
	}

	public void setMeetingDate(Date meetingDate) {
		this.meetingDate = meetingDate;
	}

	@Temporal(TemporalType.TIME)
	@Column(name = "MEETING_START_TIME", nullable = false, length = 8)
	public Time getMeetingStartTime() {
		return this.meetingStartTime;
	}

	public void setMeetingStartTime(Time meetingStartTime) {
		this.meetingStartTime = meetingStartTime;
	}

	@Column(name = "MEETING_SUBJECT", nullable = false)
	public String getMeetingSubject() {
		return this.meetingSubject;
	}

	public void setMeetingSubject(String meetingSubject) {
		this.meetingSubject = meetingSubject;
	}

	@Column(name = "SOURCE_ID", nullable = false, length = 45)
	public String getSourceId() {
		return this.sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	@Column(name = "MEETING_SESSION", nullable = false)
	public String getMeetingSession() {
		return this.meetingSession;
	}

	public void setMeetingSession(String meetingSession) {
		this.meetingSession = meetingSession;
	}

	@Column(name = "MEETING_STATUS", nullable = false)
	public Boolean getMeetingStatus() {
		return this.meetingStatus;
	}

	public void setMeetingStatus(Boolean meetingStatus) {
		this.meetingStatus = meetingStatus;
	}

	@Column(name = "VIDEO_CONFERENCE_ID")
	public String getVideoConferenceId() {
		return this.videoConferenceId;
	}

	public void setVideoConferenceId(String videoConferenceId) {
		this.videoConferenceId = videoConferenceId;
	}

	@Column(name = "VIDEO_CONFERENCE_ADDRESS")
	public String getVideoConferenceAddress() {
		return this.videoConferenceAddress;
	}

	public void setVideoConferenceAddress(String videoConferenceAddress) {
		this.videoConferenceAddress = videoConferenceAddress;
	}

	@Column(name = "MEETING_PPT")
	public String getMeetingPpt() {
		return this.meetingPpt;
	}

	public void setMeetingPpt(String meetingPpt) {
		this.meetingPpt = meetingPpt;
	}

	@Column(name = "START_URL", length = 1000)
	public String getStartUrl() {
		return this.startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

}
