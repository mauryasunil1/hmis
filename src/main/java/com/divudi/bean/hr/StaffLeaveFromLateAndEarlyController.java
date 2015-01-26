/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.hr;

import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.UtilityController;
import com.divudi.data.hr.LeaveType;
import com.divudi.data.hr.ReportKeyWord;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.FinalVariables;
import com.divudi.ejb.HumanResourceBean;
import com.divudi.entity.Form;
import com.divudi.entity.Staff;
import com.divudi.entity.hr.HrForm;
import com.divudi.entity.hr.LeaveForm;
import com.divudi.entity.hr.LeaveFormSystem;
import com.divudi.entity.hr.StaffLeave;
import com.divudi.entity.hr.StaffLeaveEntitle;
import com.divudi.entity.hr.StaffLeaveSystem;
import com.divudi.entity.hr.StaffShift;
import com.divudi.facade.LeaveFormFacade;
import com.divudi.facade.StaffLeaveEntitleFacade;
import com.divudi.facade.StaffLeaveFacade;
import com.divudi.facade.StaffShiftFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.TemporalType;

/**
 *
 * @author Sniper 619
 */
@Named
@SessionScoped
public class StaffLeaveFromLateAndEarlyController implements Serializable {

    LeaveForm currentLeaveForm;
    @EJB
    StaffLeaveFacade staffLeaveFacade;
    @EJB
    LeaveFormFacade leaveFormFacade;
    @Inject
    SessionController sessionController;

    @EJB
    CommonFunctions commonFunctions;
    List<LeaveForm> leaveForms;
    Staff approvedStaff;
    Staff staff;
    Date fromDate;
    Date toDate;
    @Enumerated(EnumType.STRING)
    LeaveType leaveType;
    double leaveEntitle;
    double leaved;
    @EJB
    FinalVariables finalVariables;
    ReportKeyWord reportKeyWord;

    public ReportKeyWord getReportKeyWord() {
        if (reportKeyWord == null) {
            reportKeyWord = new ReportKeyWord();
        }
        return reportKeyWord;
    }

    public void setReportKeyWord(ReportKeyWord reportKeyWord) {
        this.reportKeyWord = reportKeyWord;
    }

    public StaffShiftFacade getStaffShiftFacade() {
        return staffShiftFacade;
    }

    public void setStaffShiftFacade(StaffShiftFacade staffShiftFacade) {
        this.staffShiftFacade = staffShiftFacade;
    }

    public double getLeaveEntitle() {
        return leaveEntitle;
    }

    public void setLeaveEntitle(double leaveEntitle) {
        this.leaveEntitle = leaveEntitle;
    }

    public double getLeaved() {
        return leaved;
    }

    public void setLeaved(double leaved) {
        this.leaved = leaved;
    }

    public PhDateController getPhDateController() {
        return phDateController;
    }

    public void setPhDateController(PhDateController phDateController) {
        this.phDateController = phDateController;
    }

    public HumanResourceBean getHumanResourceBean() {
        return humanResourceBean;
    }

    public void setHumanResourceBean(HumanResourceBean humanResourceBean) {
        this.humanResourceBean = humanResourceBean;
    }

    @EJB
    StaffLeaveEntitleFacade staffLeaveEntitleFacade;
    @EJB
    StaffShiftFacade staffShiftFacade;
    StaffShift[] staffShiftsArray;

    public StaffShift[] getStaffShiftsArray() {
        return staffShiftsArray;
    }

    public void setStaffShiftsArray(StaffShift[] staffShiftsArray) {
        this.staffShiftsArray = staffShiftsArray;
    }

    public void fetchStaffShiftLateInErlyOut() {
        String sql = "";
        HashMap hm = new HashMap();
        sql = "select ss from StaffShift ss "
                + " where ss.retired=false "
                + " and ss.consideredForLateEarlyAttendance=false"
                //                + " and ss.consideredForSalary=false "
                + "  and ss.staff=:stf ";
        hm.put("stf", getCurrentLeaveForm().getStaff());

        sql += " and (ss.lateInVarified!=0"
                + " or ss.earlyOutVarified!=0)";

        if (getReportKeyWord().getFrom() != 0 && getReportKeyWord().getTo() != 0) {
            sql += " and ((ss.lateInVarified>= :frmTime  and ss.lateInVarified<= :toTime) "
                    + " or (ss.earlyOutVarified>= :frmTime and ss.earlyOutVarified<= :toTime )) ";
            hm.put("frmTime", getReportKeyWord().getFrom() * 60);
            hm.put("toTime", getReportKeyWord().getTo() * 60);
        } else if (getReportKeyWord().getFrom() != 0 && getReportKeyWord().getTo() == 0) {
            sql += " and ((ss.lateInVarified>= :frmTime) "
                    + " or (ss.earlyOutVarified>= :frmTime)) ";
            hm.put("frmTime", getReportKeyWord().getFrom() * 60);
        } else if (getReportKeyWord().getFrom() == 0 && getReportKeyWord().getTo() != 0) {
            sql += " and ((ss.lateInVarified<= :toTime) "
                    + " or (ss.earlyOutVarified<= :toTime )) ";
            hm.put("toTime", getReportKeyWord().getTo() * 60);
        }

        staffShifts = staffShiftFacade.findBySQL(sql, hm, 10);

    }

    public List<StaffShift> fetchStaffShiftLateIn(Staff staff, double from, double to) {
        String sql = "";
        HashMap hm = new HashMap();
        sql = "select ss from StaffShift ss "
                + " where ss.retired=false "
                + " and ss.considerForLateIn=false "
                + " and ss.leaveType is not null "
                + "  and ss.staff=:stf ";
        hm.put("stf", staff);

        sql += " and ss.lateInLogged!=0";

        sql += " and ss.lateInLogged>= :frmTime  "
                + " and ss.lateInLogged<= :toTime";
        hm.put("frmTime", from);
        hm.put("toTime", to);

        return staffShiftFacade.findBySQL(sql, hm);
    }

    public List<StaffShift> fetchStaffShiftLateIn(Staff staff, double from) {
        String sql = "";
        HashMap hm = new HashMap();
        sql = "select ss from StaffShift ss "
                + " where ss.retired=false "
                + " and ss.considerForLateIn=false "
                + " and ss.leaveType is not null "
                + "  and ss.staff=:stf ";
        hm.put("stf", staff);

        sql += " and ss.lateInLogged!=0";

        sql += " and ss.lateInLogged>= :frmTime  ";
//                + " and ss.lateInLogged<= :toTime";
        hm.put("frmTime", from);
//        hm.put("toTime", to);

        return staffShiftFacade.findBySQL(sql, hm);
    }

    public List<StaffShift> fetchStaffShiftEarlyOut(Staff staff, double from, double to) {
        String sql = "";
        HashMap hm = new HashMap();
        sql = "select ss from StaffShift ss "
                + " where ss.retired=false "
                + " and ss.considerForEarlyOut=false"
                + " and ss.leaveType is not null "
                + "  and ss.staff=:stf ";
        hm.put("stf", staff);

        sql += " and ss.earlyOutLogged!=0";
        sql += " and ss.earlyOutLogged>= :frmTime  "
                + " and ss.earlyOutLogged<= :toTime";
        hm.put("frmTime", from);
        hm.put("toTime", to);

        return staffShiftFacade.findBySQL(sql, hm);
    }

    public FinalVariables getFinalVariables() {
        return finalVariables;
    }

    public void setFinalVariables(FinalVariables finalVariables) {
        this.finalVariables = finalVariables;
    }

    public StaffLeaveEntitleFacade getStaffLeaveEntitleFacade() {
        return staffLeaveEntitleFacade;
    }

    public void setStaffLeaveEntitleFacade(StaffLeaveEntitleFacade staffLeaveEntitleFacade) {
        this.staffLeaveEntitleFacade = staffLeaveEntitleFacade;
    }

    List<StaffShift> staffShifts;

    public List<StaffShift> getStaffShifts() {
        return staffShifts;
    }

    public void setStaffShifts(List<StaffShift> staffShifts) {
        this.staffShifts = staffShifts;
    }

    public StaffLeaveEntitle fetchLeaveEntitle(Staff staff, LeaveType leaveType) {
        List<LeaveType> list = leaveType.getLeaveTypes();

        String sql = "select  ss "
                + " from StaffLeaveEntitle ss"
                + " where ss.retired=false"
                + " and ss.staff=:stf"
                + " and ss.leaveType in :ltp ";
        HashMap hm = new HashMap();
        hm.put("stf", staff);
        hm.put("ltp", list);

        return staffLeaveEntitleFacade.findFirstBySQL(sql, hm);
    }

    public void calLeaveCount() {
        if (getCurrentLeaveForm().getStaff() == null) {
            UtilityController.addErrorMessage("Please Select Staff");
            return;
        }

        LeaveType leaveTypeLocal = getCurrentLeaveForm().getLeaveType();

        if (leaveTypeLocal == null) {
            UtilityController.addErrorMessage("Please Select Leave Type");
            return;
        }

        StaffLeaveEntitle staffLeaveEntitle = fetchLeaveEntitle(getCurrentLeaveForm().getStaff(), getCurrentLeaveForm().getLeaveType());

        if (!leaveTypeLocal.isExceptionalLeave() && staffLeaveEntitle == null) {
            UtilityController.addErrorMessage("Please Set Leave Enttile count for this Staff in Administration");
            return;
        }

        if (staffLeaveEntitle != null) {
            leaveEntitle = staffLeaveEntitle.getCount();
        }

        leaved = humanResourceBean.calStaffLeave(getCurrentLeaveForm().getStaff(), leaveTypeLocal,
                getCommonFunctions().getFirstDayOfYear(new Date()),
                getCommonFunctions().getLastDayOfYear(new Date()));

    }

    public StaffLeaveFacade getStaffLeaveFacade() {
        return staffLeaveFacade;
    }

    public void setStaffLeaveFacade(StaffLeaveFacade staffLeaveFacade) {
        this.staffLeaveFacade = staffLeaveFacade;
    }

    public StaffLeaveFromLateAndEarlyController() {
    }

    public boolean errorCheck() {

        if (currentLeaveForm.getStaff() == null) {
            JsfUtil.addErrorMessage("Please Enter Staff");
            return true;
        }
        if (getCurrentLeaveForm().getLeaveType() == null) {
            JsfUtil.addErrorMessage("Please Enter Leave Type");
            return true;
        }

        if (!getCurrentLeaveForm().getLeaveType().isExceptionalLeave()
                && (leaveEntitle - leaved) <= 0) {
            JsfUtil.addErrorMessage("Staff Leave Entitle is Overflow");
            return true;
        }

        if (currentLeaveForm.getComments() == null || "".equals(currentLeaveForm.getComments())) {
            JsfUtil.addErrorMessage("Please Add Comment");
            return true;
        }

        if (staffShiftsArray == null) {
            JsfUtil.addErrorMessage("Please Select StaffShift that added for leave");
            return true;
        }

        int existShiftToAddLeave = 0;
        for (StaffShift ss : staffShiftsArray) {
            if (!ss.isConsideredForSalary()) {
                existShiftToAddLeave++;
            }
        }

        if (existShiftToAddLeave == 0) {
            JsfUtil.addErrorMessage("There is no Shift to Add Leave for this month");
            return true;
        }

        return false;
    }

    public boolean checkingForLieLeave() {

        LeaveType ltp = currentLeaveForm.getLeaveType();
        StaffShift stf = currentLeaveForm.getStaffShift();

        if ((ltp == LeaveType.Lieu
                || ltp == LeaveType.LieuHalf)) {
            if (stf == null) {
                JsfUtil.addErrorMessage("Please Select Shift That Lie Entitled");
                return true;
            }
//            Long datRang = commonFunctions.getDayCount(getCurrentLeaveForm().getFromDate(), getCurrentLeaveForm().getToDate());

//            if (datRang != 1) {
//                JsfUtil.addErrorMessage("Date range should be 1");
//                return true;
//            }
            if (ltp == LeaveType.Lieu && stf.getLieuQtyUtilized() != 0) {
                JsfUtil.addErrorMessage("You cant get Liue leave from this Shift");
                return true;
            }

            if (ltp == LeaveType.LieuHalf && stf.getLieuQtyUtilized() == 1) {
                JsfUtil.addErrorMessage("You cant get Liue leave from this Shift");
                return true;
            }
        }

        return false;
    }

    @Inject
    PhDateController phDateController;

    @EJB
    HumanResourceBean humanResourceBean;

    private List<StaffShift> fetchStaffShift(Date date, Staff staff) {
        String sql = "select s from StaffShift s "
                + " where s.retired=false "
                + " and s.shiftDate=:dt "
                + " and s.staff=:st ";
        HashMap hm = new HashMap();
        hm.put("dt", date);
        hm.put("st", staff);

        return staffShiftFacade.findBySQL(sql, hm, TemporalType.DATE);
    }

    public void addLeaveDataToStaffShift(StaffShift ss, LeaveType leaveType) {
        if (ss == null) {
            return;
        }
        ss.resetLeaveData(getCurrentLeaveForm().getLeaveType());
        ss.calLeaveTime();
        ss.setLeaveForm(currentLeaveForm);
        ss.setLeaveType(getCurrentLeaveForm().getLeaveType());
//        ss.setConsideredForLateEarlyAttendance(true);
        staffShiftFacade.edit(ss);

    }

    public void addLeaveDataToStaffShift(LinkedList<StaffShift> list) {
        if (list == null) {
            return;
        }

        int divide = list.size();

        while (!list.isEmpty()) {
            StaffShift ss = list.pollFirst();
            ss.resetLeaveData(getCurrentLeaveForm().getLeaveType());
//            ss.setLeaveDivident(divide);
//            ss.setConsideredForLateEarlyAttendance(true);
            ss.calLeaveTime();
            ss.setLeaveForm(currentLeaveForm);
            ss.setLeaveType(getCurrentLeaveForm().getLeaveType());

            staffShiftFacade.edit(ss);
        }

    }

    public void removeLeaveDataFromStaffShift(Date date, Staff staff) {
        List<StaffShift> list = fetchStaffShift(date, staff);
        if (list == null) {
            return;
        }

        for (StaffShift ss : list) {
            ss.resetLeaveData(getCurrentLeaveForm().getLeaveType());
            ss.setLeaveType(null);
            staffShiftFacade.edit(ss);
        }
    }

    private void saveStaffLeaves() {
        StaffLeave staffLeave = new StaffLeave();
        staffLeave.setCreatedAt(new Date());
        staffLeave.setCreater(sessionController.getLoggedUser());
        staffLeave.setLeaveType(getCurrentLeaveForm().getLeaveType());
        staffLeave.setRoster(getCurrentLeaveForm().getStaff().getRoster());
        staffLeave.setStaff(getCurrentLeaveForm().getStaff());
        staffLeave.setLeaveDate(staffShiftsArray[0].getShiftDate());
        staffLeave.setForm(getCurrentLeaveForm());
        staffLeave.calLeaveQty();
        staffLeaveFacade.create(staffLeave);

        LinkedList<StaffShift> list = new LinkedList<>();

        for (StaffShift ss : staffShiftsArray) {
            if (!ss.isConsideredForSalary()) {
                list.addLast(ss);
            }
        }

        if (list.isEmpty()) {
            return;
        }

        if (list.size() == 1) {
            addLeaveDataToStaffShift(list.pollFirst(), getCurrentLeaveForm().getLeaveType());
        } else {
            addLeaveDataToStaffShift(list);
        }

    }

    public HrForm saveLeaveForm(StaffShift staffShift, LeaveType leaveType, Date fromDate, Date toDate) {
        LeaveFormSystem leaveForm = new LeaveFormSystem();
        leaveForm.setCreatedAt(new Date());
        leaveForm.setCreater(sessionController.getLoggedUser());
        leaveForm.setStaff(staffShift.getStaff());
        leaveForm.setStaffShift(staffShift);
        leaveForm.setFromDate(fromDate);
        leaveForm.setToDate(toDate);
        leaveFormFacade.create(leaveForm);
        return leaveForm;
    }

    public void saveStaffLeaves(StaffShift staffShift, LeaveType leaveType, HrForm form) {
        StaffLeaveSystem staffLeave = new StaffLeaveSystem();
        staffLeave.setCreatedAt(new Date());
        staffLeave.setCreater(sessionController.getLoggedUser());
        staffLeave.setLeaveType(leaveType);
        staffLeave.setRoster(staffShift.getStaff().getRoster());
        staffLeave.setStaff(staffShift.getStaff());
        staffLeave.setLeaveDate(staffShift.getShiftDate());
        staffLeave.setStaffShift(staffShift);
        staffLeave.setForm(form);
        staffLeave.calLeaveQty();
        staffLeaveFacade.create(staffLeave);

    }

    public void addLeaveDataToStaffShift(StaffShift ss, LeaveType leaveType, HrForm form) {

        ss.resetLeaveData(leaveType);
        ss.calLeaveTime();
        ss.setLeaveForm(form);
        ss.setLeaveType(leaveType);
        staffShiftFacade.edit(ss);

    }

    public void saveLeaveform() {
        if (currentLeaveForm.getId() != null) {
            return;
        }

        if (errorCheck()) {
            return;
        }
        currentLeaveForm.setCreater(getSessionController().getLoggedUser());
        currentLeaveForm.setCreatedAt(new Date());

        if (currentLeaveForm.getId() == null) {
            getLeaveFormFacade().create(currentLeaveForm);
        } else {
            getLeaveFormFacade().edit(currentLeaveForm);
        }

        saveStaffLeaves();

        JsfUtil.addSuccessMessage("Sucessfully Saved");
        clear();
    }

    public void createleaveTable() {
        String sql;
        Map m = new HashMap();

        sql = " select l from LeaveForm l "
                + " where "
                + " l.createdAt between :fd and :td ";

        if (staff != null) {
            sql += " and l.staff=:st ";
            m.put("st", staff);
        }

        if (approvedStaff != null) {
            sql += " and l.approvedStaff=:app ";
            m.put("app", approvedStaff);
        }

        m.put("fd", fromDate);
        m.put("td", toDate);

        leaveForms = getLeaveFormFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

    }

    public void createleaveTableApprovedDate() {
        String sql;
        Map m = new HashMap();

        sql = " select l from LeaveForm l where "
                + " l.approvedAt between :fd and :td ";

        if (staff != null) {
            sql += " and l.staff=:st ";
            m.put("st", staff);
        }

        if (approvedStaff != null) {
            sql += " and l.approvedStaff=:app ";
            m.put("app", approvedStaff);
        }

        m.put("fd", fromDate);
        m.put("td", toDate);

        leaveForms = getLeaveFormFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

    }

    private List<StaffShift> fetchFormStaffShift(Form form) {
        String sql = "Select l from StaffShift l where l.leaveFrom=:frm ";
        HashMap nm = new HashMap();
        nm.put("frm", form);
        return staffShiftFacade.findBySQL(sql, nm);
    }

    public void deleteStaffLeave(LeaveForm form) {
        String sql = "Select l from StaffLeave l where l.form=:frm ";
        HashMap nm = new HashMap();
        nm.put("frm", form);
        List<StaffLeave> list = staffLeaveFacade.findBySQL(sql, nm);

        for (StaffLeave stf : list) {
            stf.setRetired(true);
            stf.setRetiredAt(new Date());
            stf.setRetirer(sessionController.getLoggedUser());
            staffLeaveFacade.edit(stf);
        }
        List<StaffShift> stfShf = fetchFormStaffShift(form);
        for (StaffShift s : stfShf) {
            s.resetLeaveData(form.getLeaveType());
//            s.setConsideredForLateEarlyAttendance(false);
//            s.setLeaveDivident(0);
            s.setLeaveType(null);
            staffShiftFacade.edit(s);
        }

    }

    public void deleteLeaveForm() {
        if (currentLeaveForm != null) {
            deleteStaffLeave(currentLeaveForm);
            currentLeaveForm.setRetired(true);
            currentLeaveForm.setRetirer(getSessionController().getLoggedUser());
            currentLeaveForm.setRetiredAt(new Date());
            getLeaveFormFacade().edit(currentLeaveForm);
            JsfUtil.addSuccessMessage("Sucessfuly Deleted.");
            clear();
        } else {
            JsfUtil.addErrorMessage("Nothing to Delete.");
        }
    }

    public void viewLeaveForm(LeaveForm leaveForm) {
        currentLeaveForm = leaveForm;
        List<StaffShift> list = fetchFormStaffShift(leaveForm);
        if (list.isEmpty()) {
            return;
        }
        int i = 0;
        staffShiftsArray = null;
        for (StaffShift s : list) {
            staffShiftsArray[i++] = s;
        }
    }

    public void clear() {
        currentLeaveForm = null;
        leaveEntitle = 0;
        leaved = 0;
        reportKeyWord = null;
        staffShiftsArray = null;
        staffShifts = null;

    }

    public LeaveForm getCurrentLeaveForm() {
        if (currentLeaveForm == null) {
            currentLeaveForm = new LeaveForm();

        }
        return currentLeaveForm;
    }

    public void setCurrentLeaveForm(LeaveForm currentLeaveForm) {
        this.currentLeaveForm = currentLeaveForm;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public LeaveFormFacade getLeaveFormFacade() {
        return leaveFormFacade;
    }

    public void setLeaveFormFacade(LeaveFormFacade leaveFormFacade) {
        this.leaveFormFacade = leaveFormFacade;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public Staff getApprovedStaff() {
        return approvedStaff;
    }

    public void setApprovedStaff(Staff approvedStaff) {
        this.approvedStaff = approvedStaff;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = commonFunctions.getStartOfMonth(new Date());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = commonFunctions.getEndOfMonth(new Date());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public List<LeaveForm> getLeaveForms() {
        return leaveForms;
    }

    public void setLeaveForms(List<LeaveForm> leaveForms) {
        this.leaveForms = leaveForms;
    }

}
