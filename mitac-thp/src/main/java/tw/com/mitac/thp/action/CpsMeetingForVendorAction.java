package tw.com.mitac.thp.action;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONObject;

import com.sun.net.httpserver.Authenticator.Success;

import tw.com.mitac.hibernate.QueryGroup;
import tw.com.mitac.hibernate.QueryOrder;
import tw.com.mitac.hibernate.QueryRule;
import tw.com.mitac.thp.bean.CpsEntity;
import tw.com.mitac.thp.bean.CpsMeeting;
import tw.com.mitac.thp.bean.CpsMeetingAttendance;
import tw.com.mitac.thp.bean.CpsMeetingCfg;
import tw.com.mitac.thp.bean.CpsMember;
import tw.com.mitac.thp.bean.CpsVendor;
import tw.com.mitac.thp.meeting.base.Response;
import tw.com.mitac.thp.meeting.meeting.CrudZoomMeeting;
import tw.com.mitac.thp.meeting.meeting.ParamMeetingData;
import tw.com.mitac.thp.meeting.meeting.ReturnDeleteData;
import tw.com.mitac.thp.meeting.meeting.ReturnMeetingData;
import tw.com.mitac.thp.meeting.meeting.ReturnUpdateData;
import tw.com.mitac.thp.util.MeetingApi;

public class CpsMeetingForVendorAction extends CpsMeetingAction {



}