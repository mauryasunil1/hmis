/*
 * Open Hospital Management Information System
 *
 * Dr M H B Ariyaratne
 * Acting Consultant (Health Informatics)
 * (94) 71 5812399
 * (94) 71 5812399
 */
package com.divudi.bean.clinical;

import com.divudi.bean.common.BillController;
import com.divudi.bean.common.CommonController;
import com.divudi.bean.common.CommonFunctionsController;
import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.UtilityController;
import com.divudi.bean.pharmacy.PharmacySaleController;
import com.divudi.data.BillType;
import com.divudi.data.SymanticType;
import com.divudi.data.clinical.ClinicalFindingValueType;
import com.divudi.data.clinical.ItemUsageType;
import com.divudi.data.clinical.PrescriptionTemplateType;
import com.divudi.data.inward.PatientEncounterType;
import com.divudi.data.lab.InvestigationResultForGraph;
import com.divudi.entity.Bill;
import com.divudi.entity.Department;
import com.divudi.entity.Doctor;
import com.divudi.entity.Institution;
import com.divudi.entity.Patient;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.Person;
import com.divudi.entity.clinical.ClinicalFindingItem;
import com.divudi.entity.clinical.ClinicalFindingValue;
import com.divudi.entity.clinical.DocumentTemplate;
import com.divudi.entity.clinical.ItemUsage;
import com.divudi.entity.clinical.Prescription;
import com.divudi.entity.clinical.PrescriptionTemplate;
import com.divudi.entity.lab.Investigation;
import com.divudi.entity.lab.InvestigationItem;
import com.divudi.entity.lab.PatientInvestigation;
import com.divudi.entity.lab.PatientReportItemValue;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.facade.BillFacade;
import com.divudi.facade.ClinicalFindingItemFacade;
import com.divudi.facade.ClinicalFindingValueFacade;
import com.divudi.facade.ItemUsageFacade;
import com.divudi.facade.PatientEncounterFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PatientInvestigationFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.PrescriptionFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import org.hl7.fhir.r5.model.Encounter;
import org.primefaces.model.DefaultStreamedContent;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics) Acting
 * Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class PatientEncounterController implements Serializable {

    /**
     * EJBs
     */
    @EJB
    private PatientEncounterFacade ejbFacade;
    @EJB
    ClinicalFindingItemFacade clinicalFindingItemFacade;
    @EJB
    private ClinicalFindingValueFacade clinicalFindingValueFacade;
    @EJB
    PersonFacade personFacade;
    @EJB
    PatientFacade patientFacade;
    @EJB
    BillFacade billFacade;
    @EJB
    PatientInvestigationFacade piFacade;
    @EJB
    private ItemUsageFacade itemUsageFacade;
    @EJB
    private PrescriptionFacade prescriptionFacade;
    /**
     * Controllers
     */
    @Inject
    private CommonFunctionsController commonFunctions;
    @Inject
    SessionController sessionController;
    @Inject
    PharmacySaleController pharmacySaleController;
    @Inject
    BillController billController;
    @Inject
    CommonController commonController;
    @Inject
    DocumentTeamplateController documentTeamplateController;

    /**
     * Properties
     */
    List<String> completeStrings = null;
    private static final long serialVersionUID = 1L;
    //
    private List<PatientEncounter> selectedItems;
    private PatientEncounter current;
    private List<PatientEncounter> items = null;
    List<PatientEncounter> encounters;

    @Inject
    private FavouriteController favouriteController;

    private Patient patient;

    List<DocumentTemplate> userDocumentTemplates;

    private ClinicalFindingValue patientAllergy;
    private ClinicalFindingValue patientMedicine;
    private ClinicalFindingValue patientImage;
    private ClinicalFindingValue patientDiagnosis;
    private ClinicalFindingValue patientDiagnosticImage;
    private ClinicalFindingValue removingClinicalFindingValue;

    private List<ClinicalFindingValue> patientClinicalFindingValues;
    private List<ClinicalFindingValue> patientAllergies;
    private List<ClinicalFindingValue> patientMedicines;
    private List<ClinicalFindingValue> patientImages;
    private List<ClinicalFindingValue> patientDiagnoses;
    private List<ClinicalFindingValue> patientDiagnosticImages;

    private ClinicalFindingValue encounterMedicine;
    private ClinicalFindingValue encounterDiagnosticImage;
    private ClinicalFindingValue encounterDiagnosis;
    private ClinicalFindingValue encounterImage;
    private ClinicalFindingValue encounterInvestigation;
    private ClinicalFindingValue encounterMedicalFitnessCertificate;
    private ClinicalFindingValue encounterMedicalCertificate;
    private ClinicalFindingValue encounterReferral;
    private ClinicalFindingValue encounterPrescreption;

    private List<ClinicalFindingValue> encounterMedicines;
    private List<ClinicalFindingValue> encounterDiagnosticImages;
    private List<ClinicalFindingValue> encounterDiagnoses;
    private List<ClinicalFindingValue> encounterImages;
    private List<ClinicalFindingValue> encounterInvestigations;
    private List<ClinicalFindingValue> encounterMedicalFitnessCertificates;
    private List<ClinicalFindingValue> encounterMedicalCertificates;
    private List<ClinicalFindingValue> encounterReferrals;
    private List<ClinicalFindingValue> encounterPrescreptions;

    private List<ItemUsage> currentEncounterMedicines;
    private List<ItemUsage> currentEncounterDiagnosis;
    private List<Bill> patientBills;
    private List<Bill> opdBills;
    private List<Bill> opdVisits;
    private List<Bill> pharmacyBills;
    private List<Bill> channelBills;
    private List<PatientInvestigation> investigations;
    private String selectText = "";

    private ClinicalFindingItem diagnosis;
    private String diagnosisComments;
    private Investigation investigation;

    private ClinicalFindingValue removingCfv;

    private PatientEncounter encounterToDisplay;
    private PatientEncounter startedEncounter;

    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private Department department;
    private Doctor doctor;

    private String chartNameSeries;
    private String chartDataSeries1;
    private String chartDataSeries2;
    private String chartName;
    private String values1Name;
    private String values2Name;

    private String chartString;

    private InvestigationItem graphInvestigationItem;

    public StreamedContent getImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            return DefaultStreamedContent.builder().build();
        } else {
            // So, browser is requesting the image. Get ID value from actual request param.
            String id = context.getExternalContext().getRequestParameterMap().get("id");
            System.out.println("id = " + id);
            ClinicalFindingValue image = clinicalFindingValueFacade.find(Long.valueOf(id)); // Assuming 'service' is your EJB session bean.
            System.out.println("image = " + image);
            String imageType = image.getImageType();
            if (imageType == null || imageType.trim().equals("")) {
                imageType = "image/png";
            }
            return DefaultStreamedContent.builder()
                    .contentType(imageType)
                    .stream(() -> new ByteArrayInputStream(image.getImageValue()))
                    .build();
        }
    }

    public void updateEncounterImages() {
        System.out.println("encounterImages = " + encounterImages.size());
    }

    public void listInstitutionEncounters() {
        System.out.println("listInstitutionEncounters = ");
        String jpql = "select e "
                + " from PatientEncounter e "
                + " where e.retired=:ret "
                + " and e.institution=:ins "
                + " and e.encounterDate between :fd and :td "
                + "order by e.id";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("ins", sessionController.getInstitution());
        m.put("fd", fromDate);
        m.put("td", toDate);
        items = getFacade().findByJpql(jpql, m);
    }

    public List<String> completeClinicalComments(String qry) {
        if (current == null || current.getComments() == null) {
            completeRx(qry);
            return completeStrings;
        }
        String c = current.getComments();
        int intHx = c.lastIndexOf(getSessionController().getUserPreference().getAbbreviationForHistory());
        int intEx = c.lastIndexOf(getSessionController().getUserPreference().getAbbreviationForExamination());
        int intIx = c.lastIndexOf(getSessionController().getUserPreference().getAbbreviationForInvestigations());
        int intRx = c.lastIndexOf(getSessionController().getUserPreference().getAbbreviationForTreatments());
        int intMx = c.lastIndexOf(getSessionController().getUserPreference().getAbbreviationForManagement());

        //   ////// // System.out.println("intHx = " + intHx);
        //   ////// // System.out.println("intEx = " + intEx);
        //   ////// // System.out.println("intIx = " + intIx);
        //   ////// // System.out.println("intRx = " + intRx);
        //   ////// // System.out.println("intMx = " + intMx);
        ClinicalField lastField = ClinicalField.History;
        int lastValue = intHx;

        if (intEx > lastValue) {
            lastField = ClinicalField.Examination;
            lastValue = intEx;
        }

        if (intIx > lastValue) {
            lastField = ClinicalField.Investigations;
            lastValue = intIx;
        }

        if (intRx > lastValue) {
            lastField = ClinicalField.Treatments;
            lastValue = intRx;
        }

        if (intMx > lastValue) {
            lastField = ClinicalField.Management;
            lastValue = intMx;
        }

        switch (lastField) {
            case History:
                completeHx(qry);
                break;
            case Examination:
                completeEx(qry);
                break;
            case Investigations:
                completeIx(qry);
                break;
            case Treatments:
                completeRx(qry);
                break;
            default:
                completeStrings = completeItem(qry);
        }

        return completeStrings;
    }

    public List<String> completeItem(String qry) {
        //   ////// // System.out.println("complete item");
        if (qry == null) {
            qry = "";
        }
        String sql;
        sql = "select c.name from Item c where c.retired=false and "
                + "upper(c.name) like :q "
                + "order by c.name";
        Map tmpMap = new HashMap();
        tmpMap.put("q", qry.toUpperCase() + "%");
        return getFacade().findString(sql, tmpMap);
    }

    public String createInvestigationChart() {

        String s = "";
        int i = 0;
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd");
        String val = "";

        List<PatientReportItemValue> privs = fillPatientReportItemValue(current.getPatient(), graphInvestigationItem);
        List<InvestigationResultForGraph> grs = new ArrayList<>();

        for (PatientReportItemValue v : privs) {
            boolean dateFound = false;
            Double dblVal = null;
            try {
                if (v.getDoubleValue() != null) {
                    dblVal = v.getDoubleValue();
                } else if (v.getStrValue() != null) {
                    dblVal = Double.parseDouble(v.getStrValue());
                }
                if (dblVal != null) {

                    i++;

                    dateFound = true;

                    s += "'" + format.format(v.getPatientReport().getPatientInvestigation().getSampledAt()) + "'";
                    if (i != getEncounters().size()) {
                        s += ", ";
                    }

                    val += dblVal + "";
                    if (i != getEncounters().size()) {
                        val += ", ";
                    }
                }

            } catch (Exception e) {

            }
            for (InvestigationResultForGraph g : grs) {
                if (g.getDates().equals(v.getPatientReport().getApproveAt())) {

                }
            }

        }

        setChartNameSeries(s);
        setChartDataSeries1(val);
        setValues1Name(graphInvestigationItem.getName());
        setChartName(graphInvestigationItem.getName() + " Chart");
        setChartString(getSingleLineChartString());
        return "/emr/chart";
    }

    public List<PatientReportItemValue> fillPatientReportItemValue(Patient patient, InvestigationItem ii) {
        Map m = new HashMap();
        m.put("p", patient);
        m.put("it", ii);
        String sql;
        sql = "Select v "
                + " from PatientReportItemValue v "
                + " where "
                + " v.patientReport.patientInvestigation.patient=:p "
                + " and v.investigationItem=:it "
                + " order by v.patientReport.patientInvestigation.id";
        return getPiFacade().findByJpql(sql, m);
    }

    public void completeHx(String qry) {
        //   ////// // System.out.println("complete hx");
        if (qry == null) {
            qry = "";
        }
        String sql;
        sql = "select c.name from Item c where c.retired=false and "
                + "type(c)= :cls and "
                + "c.symanticType=:st and "
                + "upper(c.name) like :q "
                + "order by c.name";
        Map tmpMap = new HashMap();
        tmpMap.put("cls", ClinicalFindingItem.class);
        tmpMap.put("st", SymanticType.Symptom);
        tmpMap.put("q", qry.toUpperCase() + "%");
        completeStrings = getFacade().findString(sql, tmpMap, TemporalType.TIMESTAMP);
    }

    public void completeEx(String qry) {

        //   ////// // System.out.println("complete ex");
        if (qry == null) {
            qry = "";
        }
        String sql;
        sql = "select c.name from Item c where c.retired=false and "
                + "type(c)= :cls and "
                + "c.symanticType=:st and "
                + "upper(c.name) like :q "
                + "order by c.name";
        Map tmpMap = new HashMap();
        tmpMap.put("cls", ClinicalFindingItem.class);
        tmpMap.put("st", SymanticType.Sign);
        tmpMap.put("q", qry.toUpperCase() + "%");
        completeStrings = getFacade().findString(sql, tmpMap, TemporalType.TIMESTAMP);
    }

    public void completeIx(String qry) {
        //   ////// // System.out.println("complete Ix");
        if (qry == null) {
            qry = "";
        }
        String sql;
        sql = "select c.name from Investigation c where c.retired=false and "
                + "upper(c.name) like :q "
                + "order by c.name";
        Map tmpMap = new HashMap();
        tmpMap.put("q", qry.toUpperCase() + "%");
        completeStrings = getFacade().findString(sql, tmpMap, TemporalType.TIMESTAMP);
    }

    public void completeRx(String qry) {
        //   ////// // System.out.println("complete rx");
        //   ////// // System.out.println("qry = " + qry);
        if (qry == null) {
            qry = "";
        }
        String sql;
        sql = "select c.name from Item c where c.retired=false and "
                + "(type(c)= :amp or type(c)= :vmp or "
                + "type(c)= :vtm or "
                + "(type(c)= :ce and c.symanticType=:st)) "
                + "and upper(c.name) like :q "
                + "order by c.name";
        //////// // System.out.println(sql);
        Map tmpMap = new HashMap();
        tmpMap.put("amp", Amp.class);
        tmpMap.put("vmp", Vmp.class);
        tmpMap.put("vtm", Vmp.class);
        tmpMap.put("ce", ClinicalFindingItem.class);
        tmpMap.put("st", SymanticType.Pharmacologic_Substance);
        tmpMap.put("q", qry.toUpperCase() + "%");
        completeStrings = getFacade().findString(sql, tmpMap);
    }

    public String navigateToListInstitutionEncounters() {
        items = null;
        return "/emr/reports/visits";
    }

    public String listAllEncounters() {
        Date startTime = new Date();

        String jpql;
        Map m = new HashMap();
        jpql = "select pe from PatientEncounter pe where pe.retired=false and pe.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);
        if (institution != null) {
            jpql = jpql + " and pe.department.institution=:ins ";
            m.put("ins", institution);

        } else if (department != null) {
            jpql = jpql + " and pe.department=:dep ";
            m.put("dep", department);
        }
        if (doctor != null) {
            jpql = jpql + " and pe.opdDoctor=:doc ";
            m.put("doc", doctor);
        }
        ////// // System.out.println("1. m = " + m);
        ////// // System.out.println("2. sql = " + jpql);
        items = getFacade().findBySQL(jpql, m, TemporalType.TIMESTAMP);
        ////// // System.out.println("3. items = " + items);

        commonController.printReportDetails(fromDate, toDate, startTime, "EHR/Reports/All visits/(/faces/clinical/clinical_reports_all_opd_visits.xhtml)");
        return "/clinical/clinical_reports_all_opd_visits?faces-redirect=true";
    }

    public void listPeriodEncounters() {
        String jpql;
        Map m = new HashMap();
        jpql = "select pe from PatientEncounter pe where pe.retired=false and pe.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);
        if (institution != null) {
            jpql = jpql + " and pe.department.institution=:ins ";
            m.put("ins", institution);

        } else if (department != null) {
            jpql = jpql + " and pe.department=:dep ";
            m.put("dep", department);
        }
        if (doctor != null) {
            jpql = jpql + " and pe.opdDoctor=:doc ";
            m.put("doc", doctor);
        }
        //   ////// // System.out.println("m = " + m);
        //   ////// // System.out.println("sql = " + jpql);
        items = getFacade().findByJpql(jpql, m);

    }

    public void addDx() {
        if (diagnosis == null) {
            UtilityController.addErrorMessage("Please select a diagnosis");
            return;
        }
        if (current == null) {
            UtilityController.addErrorMessage("Please select a visit");
            return;
        }
        ClinicalFindingValue dx = new ClinicalFindingValue();
        dx.setItemValue(diagnosis);
        dx.setClinicalFindingItem(diagnosis);
        dx.setEncounter(current);
        dx.setPerson(current.getPatient().getPerson());
        dx.setStringValue(diagnosis.getName());
        dx.setLobValue(diagnosisComments);
        current.getClinicalFindingValues().add(dx);
        getFacade().edit(current);
        diagnosis = null;
//        diagnosis = new ClinicalFindingItem();
        diagnosisComments = "";
        UtilityController.addSuccessMessage("Diagnosis added");
        setCurrent(getFacade().find(current.getId()));
    }

    public void addDxAndRx() {
        if (diagnosis == null) {
            UtilityController.addErrorMessage("Please select a diagnosis");
            return;
        }
        if (current == null) {
            UtilityController.addErrorMessage("Please select a visit");
            return;
        }
        ClinicalFindingValue dx = new ClinicalFindingValue();
        dx.setItemValue(diagnosis);
        dx.setClinicalFindingItem(diagnosis);
        dx.setEncounter(current);
        dx.setPerson(current.getPatient().getPerson());
        dx.setStringValue(diagnosis.getName());
        dx.setLobValue(diagnosisComments);
        current.getClinicalFindingValues().add(dx);
        getFacade().edit(current);

        List<PrescriptionTemplate> dxitems;

        if (getCurrent().getWeight() != null && getCurrent().getWeight() > 0.1) {
            dxitems = favouriteController.listFavouriteItems(diagnosis, PrescriptionTemplateType.FavouriteDiagnosis, current.getWeight());
        } else if (getCurrent().getPatient() != null && getCurrent().getPatient().getAgeInDays() != null) {
            Long ageInDays = getCurrent().getPatient().getAgeInDays();
            dxitems = favouriteController.listFavouriteItems(diagnosis, PrescriptionTemplateType.FavouriteDiagnosis, null, ageInDays);
        } else {
            return;
        }
        if (dxitems == null) {
            return;
        }
        if (dxitems.isEmpty()) {
            return;
        }
        for (PrescriptionTemplate iu : dxitems) {
            if (iu.getItem() == null) {
                continue;
            }

            List<PrescriptionTemplate> availableFavouriteMedicines = null;
            PrescriptionTemplate addingMedicine;

            if (getCurrent().getWeight() != null && getCurrent().getWeight() > 0.1) {
                availableFavouriteMedicines = favouriteController.listFavouriteItems(iu.getItem(), PrescriptionTemplateType.FavouriteMedicine, current.getWeight());
            } else if (getCurrent().getPatient() != null && getCurrent().getPatient().getAgeInDays() != null) {
                Long ageInDays = getCurrent().getPatient().getAgeInDays();
//                availableFavouriteMedicines = favouriteController.listFavouriteItems(iu.getItem(), PrescriptionTemplate.FavouriteMedicine, null, ageInDays);
            }

            System.out.println("availableFavouriteMedicines = " + availableFavouriteMedicines);
            if (availableFavouriteMedicines == null) {
                continue;
            }

            System.out.println("availableFavouriteMedicines.isEmpty() = " + availableFavouriteMedicines.isEmpty());
            if (availableFavouriteMedicines.isEmpty()) {
                continue;
            }

            if (availableFavouriteMedicines.size() > 1) {
                //TODO: Need to select the best out of the available
                addingMedicine = availableFavouriteMedicines.get(0);
            } else {
                addingMedicine = availableFavouriteMedicines.get(0);

            }
            Prescription p = new Prescription();
            p.setItem(addingMedicine.getItem());
            p.setCategory(addingMedicine.getCategory());
            p.setDepartment(sessionController.getDepartment());
            p.setInstitution(sessionController.getInstitution());
            p.setDose(addingMedicine.getDose());
            p.setDoseUnit(addingMedicine.getDoseUnit());
            p.setDuration(addingMedicine.getDuration());
            p.setDurationUnit(addingMedicine.getDurationUnit());
            p.setEncounter(getCurrent());
            p.setFrequencyUnit(addingMedicine.getFrequencyUnit());
            p.setIssue(addingMedicine.getIssue());
            p.setIssueUnit(addingMedicine.getIssueUnit());
            //to do
            p.setOrderNo((double) getCurrent().getClinicalFindingValues().size() + 1);
            p.setPatient(getCurrent().getPatient());
            p.setWebUser(sessionController.getLoggedUser());

            p.setCreatedAt(new Date());
            p.setCreater(sessionController.getLoggedUser());
            try {

            } catch (Exception e) {

            }

            encounterMedicine.setPrescription(p);
            encounterMedicine.setClinicalFindingValueType(ClinicalFindingValueType.VisitMedicine);
            clinicalFindingValueFacade.create(encounterMedicine);
            //TO Do

        }

        save(getCurrent());

        diagnosis = null;
        diagnosisComments = "";
        UtilityController.addSuccessMessage("Diagnosis and Medicines added");
//        setCurrent(getFacade().find(current.getId()));
    }

    public void save(PatientEncounter encounter) {
        if (encounter.getId() != null) {
            getFacade().edit(encounter);
        } else {
            getFacade().create(encounter);
        }
    }

    public void addRxWithoutDx() {
//        if (diagnosis == null) {
//            UtilityController.addErrorMessage("Please select a diagnosis");
//            return;
//        }
//        if (current == null) {
//            UtilityController.addErrorMessage("Please select a visit");
//            return;
//        }
//        List<PrescriptionTemplate> dxitems;
//
//        if (getCurrent().getWeight() != null && getCurrent().getWeight() > 0.1) {
//            To Do
////            dxitems = favouriteController.listFavouriteItems(diagnosis, ItemUsageType.FavouriteDiagnosis, current.getWeight());
//        } else if (getCurrent().getPatient() != null && getCurrent().getPatient().getAgeInDays() != null) {
//            System.out.println("by age");
//            Long ageInDays = getCurrent().getPatient().getAgeInDays();
//            System.out.println("ageInDays = " + ageInDays);
//           To Do
//            //dxitems = favouriteController.listFavouriteItems(diagnosis, ItemUsageType.FavouriteDiagnosis, null, ageInDays);
//        } else {
//            return;
//        }
//        if (dxitems == null) {
//            return;
//        }
//        if (dxitems.isEmpty()) {
//            return;
//        }
//        for (PrescriptionTemplate iu : dxitems) {
//            if (iu.getItem() == null) {
//                continue;
//            }
//
//            List<ItemUsage> availableFavouriteMedicines = null;
//            ItemUsage addingMedicine;
//
//            if (getCurrent().getWeight() != null && getCurrent().getWeight() > 0.1) {
////                availableFavouriteMedicines = favouriteController.listFavouriteItems(iu.getItem(), ItemUsageType.FavouriteMedicine, current.getWeight());
//            } else if (getCurrent().getPatient() != null && getCurrent().getPatient().getAgeInDays() != null) {
//                System.out.println("by age");
//                Long ageInDays = getCurrent().getPatient().getAgeInDays();
//                System.out.println("ageInDays = " + ageInDays);
////                availableFavouriteMedicines = favouriteController.listFavouriteItems(iu.getItem(), ItemUsageType.FavouriteMedicine, null, ageInDays);
//            }
//
//            System.out.println("availableFavouriteMedicines = " + availableFavouriteMedicines);
//            if (availableFavouriteMedicines == null) {
//                continue;
//            }
//
//            System.out.println("availableFavouriteMedicines.isEmpty() = " + availableFavouriteMedicines.isEmpty());
//            if (availableFavouriteMedicines.isEmpty()) {
//                continue;
//            }
//
//            System.out.println("availableFavouriteMedicines.size() = " + availableFavouriteMedicines.size());
//            if (availableFavouriteMedicines.size() > 1) {
//                //TODO: Need to select the best out of the available
//                addingMedicine = availableFavouriteMedicines.get(0);
//            } else {
//                addingMedicine = availableFavouriteMedicines.get(0);
//
//            }
//            Prescription p = new Prescription();
//            System.out.println("addingMedicine = " + addingMedicine);
//            p.setItem(addingMedicine.getItem());
//            p.setCategory(addingMedicine.getCategory());
//            p.setDepartment(sessionController.getDepartment());
//            p.setInstitution(sessionController.getInstitution());
//            p.setDose(addingMedicine.getDose());
//            p.setDoseUnit(addingMedicine.getDoseUnit());
//            p.setDuration(addingMedicine.getDuration());
//            p.setDurationUnit(addingMedicine.getDurationUnit());
//            p.setEncounter(getCurrent());
//            p.setFrequencyUnit(addingMedicine.getFrequencyUnit());
//            p.setIssue(addingMedicine.getIssue());
//            p.setIssueUnit(addingMedicine.getIssueUnit());
//            p.setOrderNo((double) getCurrent().getPrescriptions().size() + 1);
//            p.setPatient(getCurrent().getPatient());
//            p.setWebUser(sessionController.getLoggedUser());
//
//            p.setCreatedAt(new Date());
//            p.setCreater(sessionController.getLoggedUser());
//            try {
//
//            } catch (Exception e) {
//
//            }
//            System.out.println("getCurrent().getPrescriptions() = " + getCurrent().getPrescriptions());
//            getCurrent().getPrescriptions().add(p);
//            System.out.println("p = " + p);
//            System.out.println("getCurrent().getPrescriptions() = " + getCurrent().getPrescriptions());
//
//        }
//
//        save(getCurrent());
//
//        diagnosis = null;
//        diagnosisComments = "";
//        UtilityController.addSuccessMessage("Medicines for Diagnosis added.");
////        setCurrent(getFacade().find(current.getId()));
    }

    public List<PatientEncounter> getEncounters() {
        return encounters;
    }

    public List<PatientEncounter> fillCurrentPatientEncounters(PatientEncounter pe) {
        Map m = new HashMap();
        m.put("p", pe.getPatient());
        m.put("pe", pe);
        String sql;
        sql = "Select e from PatientEncounter e where e.patient=:p and e!=:pe order by e.id desc";
        return getFacade().findByJpql(sql, m);
    }

    public List<ClinicalFindingValue> fillPatientAllergies(Patient patient) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.PatientAllergy);
        return fillCurrentPatientClinicalFindingValues(patient, clinicalFindingValueTypes);
    }

    public List<ClinicalFindingValue> fillPatientDiagnoses(Patient patient) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.PatientDiagnosis);
        return fillCurrentPatientClinicalFindingValues(patient, clinicalFindingValueTypes);
    }

    public List<ClinicalFindingValue> fillPatientMedicines(Patient patient) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.PatientMedicine);
        return fillCurrentPatientClinicalFindingValues(patient, clinicalFindingValueTypes);
    }

    public List<ClinicalFindingValue> fillEncounterMedicines(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitMedicine);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    public List<ClinicalFindingValue> fillPatientImages(Patient patient) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.PatientImage);
        return fillCurrentPatientClinicalFindingValues(patient, clinicalFindingValueTypes);
    }

    public List<ClinicalFindingValue> fillPatientDiadnosticImages(Patient patient) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.PatientDiagnosticImage);
        return fillCurrentPatientClinicalFindingValues(patient, clinicalFindingValueTypes);
    }

    public List<ClinicalFindingValue> fillCurrentPatientClinicalFindingValues(Patient patient) {
        return fillCurrentPatientClinicalFindingValues(patient, null);
    }

    public List<ClinicalFindingValue> fillCurrentPatientClinicalFindingValues(Patient patient, List<ClinicalFindingValueType> clinicalFindingValueTypes) {
        Map m = new HashMap();
        m.put("p", patient);

        m.put("ret", false);
        String jpql;
        ClinicalFindingValue e = new ClinicalFindingValue();
        e.getPatient();
        jpql = "Select e "
                + " from ClinicalFindingValue e "
                + " where e.patient=:p "
                + " and e.retired=:ret ";

        if (clinicalFindingValueTypes != null) {
            m.put("ts", clinicalFindingValueTypes);
            jpql += " and e.clinicalFindingValueType in :ts ";
        }

        jpql += " order by e.orderNo";
        return clinicalFindingValueFacade.findByJpql(jpql, m);
    }

    public List<ClinicalFindingValue> fillCurrentEncounterFindingValues(PatientEncounter encounter, List<ClinicalFindingValueType> clinicalFindingValueTypes) {
        Map m = new HashMap();
        m.put("e", encounter);
        m.put("ts", clinicalFindingValueTypes);
        m.put("ret", false);
        String sql;
        ClinicalFindingValue e = new ClinicalFindingValue();
        e.getPatient();
        sql = "Select e "
                + " from ClinicalFindingValue e "
                + " where e.encounter=:e "
                + " and e.retired=:ret "
                + " and e.clinicalFindingValueType in :ts "
                + " order by e.orderNo";
        return clinicalFindingValueFacade.findByJpql(sql, m);
    }

    public String navigateToOldOpdVisitFromSearch() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing");
            return "";
        }
        if (current.getPatientEncounterType() == null) {
            JsfUtil.addErrorMessage("No Encounter Type");
            return "";
        }
        if (current.getPatient() == null) {
            JsfUtil.addErrorMessage("No Patient");
            return "";
        }
        PatientEncounter opdVisit = current;
        opdVisit.setPatientEncounterType(PatientEncounterType.OpdVisit);
        setStartedEncounter(opdVisit);
        fillCurrentPatientLists(current.getPatient());
        fillCurrentEncounterLists(opdVisit);
        return "/emr/opd_visit";
    }

    public void fillCurrentPatientLists(Patient patient) {
        encounters = fillPatientEncounters(patient);

        patientBills = fillPatientBills(patient);
        opdBills = new ArrayList<>();
        pharmacyBills = new ArrayList<>();
        channelBills = new ArrayList<>();

        for (Bill b : patientBills) {
            if (b.getBillType() == null) {
                continue;
            }
            switch (b.getBillType()) {
                case OpdBill:
                    opdBills.add(b);
                    break;
                case PharmacySale:
                    pharmacyBills.add(b);
                    break;
                case ChannelPaid:
                case Channel:
                case ChannelAgent:
                case ChannelCash:
                case ChannelOnCall:
                case ChannelStaff:
                    channelBills.add(b);
                    break;

            }
        }

        investigations = fillPatientInvestigations(patient);

        patientClinicalFindingValues = fillCurrentPatientClinicalFindingValues(patient);

        patientAllergies = new ArrayList<>();
        patientDiagnoses = new ArrayList<>();
        patientImages = new ArrayList<>();
        patientDiagnosticImages = new ArrayList<>();
        patientMedicines = new ArrayList<>();

        if (patientClinicalFindingValues == null) {
            return;
        }

        for (ClinicalFindingValue tcfv : patientClinicalFindingValues) {
            if (tcfv.getClinicalFindingValueType() == null) {
                continue;
            }
            switch (tcfv.getClinicalFindingValueType()) {
                case PatientAllergy:
                    patientAllergies.add(tcfv);
                    break;
                case PatientDiagnosis:
                    patientDiagnoses.add(tcfv);
                    break;
                case PatientDiagnosticImage:
                    patientDiagnosticImages.add(tcfv);
                    break;
                case PatientImage:
                    patientImages.add(tcfv);
                    break;
                case PatientMedicine:
                    patientMedicines.add(tcfv);
                    break;
            }
        }
    }

    public void fillCurrentEncounterLists(PatientEncounter encounter) {
        encounterMedicines = fillEncounterMedicines(encounter);
        encounterInvestigations = fillEncounterInvestigations(encounter);
        encounterDiagnoses = fillEncounterDiagnoses(encounter);
        encounterImages = fillEncounterImages(encounter);
        encounterDiagnosticImages = fillEncounterDiadnosticImages(encounter);
        encounterMedicalFitnessCertificates = fillEncounterMedicalFitnessCertificates(encounter);
        encounterMedicalCertificates = fillEncounterMedicalCertificates(encounter);
        encounterReferrals = fillEncounterReferrals(encounter);
        encounterPrescreptions = fillEncounterPrescreptions(encounter);
    }

    public String generateDocumentFromTemplate(DocumentTemplate t, PatientEncounter e) {

        String input = t.getContents();
        String output = "";

        String name = e.getPatient().getPerson().getNameWithTitle();
        String age = e.getPatient().getPerson().getAge() != null ? e.getPatient().getPerson().getAge() : "";
        String sex = e.getPatient().getPerson().getSex().name() != null ? e.getPatient().getPerson().getSex().name() : "";
        String address = e.getPatient().getPerson().getAddress() != null ? e.getPatient().getPerson().getAddress() : "";
        String phone = e.getPatient().getPerson().getPhone() != null ? e.getPatient().getPerson().getPhone() : "";

        for (ClinicalFindingValue cf : getPatientDiagnoses()) {
            cf.getItemValue().getName();
            cf.getItemValue().getComments();
        }

        String medicinesAsString = "Rx" + "<br/>";

        for (ClinicalFindingValue cf : getEncounterMedicines()) {
            if (cf != null && cf.getPrescription() != null) {
                String rxName = cf.getPrescription().getItem() != null ? cf.getPrescription().getItem().getName() : "";
                String dose = cf.getPrescription().getDose() != null ? String.format("%.0f", cf.getPrescription().getDose()) : "";
                String doseUnit = cf.getPrescription().getDoseUnit() != null ? cf.getPrescription().getDoseUnit().getName() : "";
                String frequencyUnit = cf.getPrescription().getFrequencyUnit() != null ? cf.getPrescription().getFrequencyUnit().getName() : "";
                String duration = cf.getPrescription().getDuration() != null ? String.format("%.0f", cf.getPrescription().getDuration()) : "";
                String durationUnit = cf.getPrescription().getDurationUnit() != null ? cf.getPrescription().getDurationUnit().getName() : "";

                medicinesAsString += rxName + " " + dose + " " + doseUnit + " " + frequencyUnit + " " + duration + " " + durationUnit + "<br/>";
            }
        }

        String medicinesOutdoorAsString = "Rx" + "<br/>";
        for (ClinicalFindingValue cf : getEncounterMedicines()) {
            if (cf != null && cf.getPrescription() != null && !Boolean.TRUE.equals(cf.getPrescription().isIndoor())) {
                String rxName = cf.getPrescription().getItem() != null ? cf.getPrescription().getItem().getName() : "";
                String dose = cf.getPrescription().getDose() != null ? String.format("%.0f", cf.getPrescription().getDose()) : "";
                String doseUnit = cf.getPrescription().getDoseUnit() != null ? cf.getPrescription().getDoseUnit().getName() : "";
                String frequencyUnit = cf.getPrescription().getFrequencyUnit() != null ? cf.getPrescription().getFrequencyUnit().getName() : "";
                String duration = cf.getPrescription().getDuration() != null ? String.format("%.0f", cf.getPrescription().getDuration()) : "";
                String durationUnit = cf.getPrescription().getDurationUnit() != null ? cf.getPrescription().getDurationUnit().getName() : "";

                medicinesOutdoorAsString += rxName + " " + dose + " " + doseUnit + " " + frequencyUnit + " " + duration + " " + durationUnit + "<br/>";
            }
        }

        String medicinesIndoorAsString = "Rx" + "<br/>";
        for (ClinicalFindingValue cf : getEncounterMedicines()) {
            if (cf != null && cf.getPrescription() != null && Boolean.TRUE.equals(cf.getPrescription().isIndoor())) {
                String rxName = cf.getPrescription().getItem() != null ? cf.getPrescription().getItem().getName() : "";
                String dose = cf.getPrescription().getDose() != null ? String.format("%.0f", cf.getPrescription().getDose()) : "";
                String doseUnit = cf.getPrescription().getDoseUnit() != null ? cf.getPrescription().getDoseUnit().getName() : "";
                String frequencyUnit = cf.getPrescription().getFrequencyUnit() != null ? cf.getPrescription().getFrequencyUnit().getName() : "";
                String duration = cf.getPrescription().getDuration() != null ? String.format("%.0f", cf.getPrescription().getDuration()) : "";
                String durationUnit = cf.getPrescription().getDurationUnit() != null ? cf.getPrescription().getDurationUnit().getName() : "";

                medicinesIndoorAsString += rxName + " " + dose + " " + doseUnit + " " + frequencyUnit + " " + duration + " " + durationUnit + "<br/>";
            }
        }

        String ixAsString = "Ix" + "<br/>";
        for (ClinicalFindingValue ix : getEncounterInvestigations()) {
            ixAsString += ix.getItemValue().getName();
        }

        output = input.replace("{name}", name)
                .replace("{age}", age)
                .replace("{sex}", sex)
                .replace("{address}", address)
                .replace("{phone}", phone)
                .replace("{medicines}", medicinesAsString)
                .replace("{outdoor}", medicinesOutdoorAsString)
                .replace("{indoor}", medicinesIndoorAsString)
                .replace("{ix}", ixAsString);

        return output;

    }

    public void generateDocumentsFromDocumentTemplates(PatientEncounter encounter) {
        List<DocumentTemplate> dts;
        if (userDocumentTemplates == null) {
            userDocumentTemplates = documentTeamplateController.fillAllItems(sessionController.getLoggedUser());
        }
        if (userDocumentTemplates == null) {
            return;
        }
        dts = userDocumentTemplates;

        for (DocumentTemplate t : dts) {
            if (t.isDefaultTemplate()) {
                switch (t.getType()) {
                    case FitnessCertificate:

                        break;
                    case MedicalCertificate:

                        break;
                    case Prescription:
                        ClinicalFindingValue cfv = new ClinicalFindingValue();
                        cfv.setEncounter(encounter);
                        cfv.setDocumentTemplate(t);
                        cfv.setStringValue(generateDocumentFromTemplate(t, encounter));
                        getEncounterPrescreptions().add(cfv);
                        setEncounterPrescreption(cfv);
                        break;
                    case Referral:
                        break;
                    default:
                        continue;
                }
            }
        }

    }

    public void removePatientAllergy() {
        System.out.println("removePatientAllergy");
        if (getRemovingClinicalFindingValue() == null) {
            JsfUtil.addErrorMessage("Select Allergy");
            return;
        }
        getRemovingClinicalFindingValue().setRetired(true);
        clinicalFindingValueFacade.edit(getRemovingClinicalFindingValue());
        getPatientAllergies().remove(getRemovingClinicalFindingValue());
        setRemovingClinicalFindingValue(null);
    }

    public void removePatientMedicine() {
        if (getRemovingClinicalFindingValue() == null) {
            JsfUtil.addErrorMessage("Select Allergy");
            return;
        }
        getRemovingClinicalFindingValue().setRetired(true);
        clinicalFindingValueFacade.edit(getRemovingClinicalFindingValue());
        getPatientMedicines().remove(getRemovingClinicalFindingValue());
        setRemovingClinicalFindingValue(null);
    }

    public void removePatientDiagnosis() {
        if (getRemovingClinicalFindingValue() == null) {
            JsfUtil.addErrorMessage("Select Allergy");
            return;
        }
        getRemovingClinicalFindingValue().setRetired(true);
        clinicalFindingValueFacade.edit(getRemovingClinicalFindingValue());
        getPatientDiagnoses().remove(getRemovingClinicalFindingValue());
        setRemovingClinicalFindingValue(null);
    }

    public void removeEncounterMedicine() {
        if (getRemovingClinicalFindingValue() == null) {
            JsfUtil.addErrorMessage("Select");
            return;
        }
        getRemovingClinicalFindingValue().setRetired(true);
        clinicalFindingValueFacade.edit(getRemovingClinicalFindingValue());
        getEncounterMedicines().remove(getRemovingClinicalFindingValue());
        setRemovingClinicalFindingValue(null);
    }

    public void removeEncounterImage() {
        if (getRemovingClinicalFindingValue() == null) {
            JsfUtil.addErrorMessage("Select");
            return;
        }
        getRemovingClinicalFindingValue().setRetired(true);
        clinicalFindingValueFacade.edit(getRemovingClinicalFindingValue());
        getEncounterImages().remove(getRemovingClinicalFindingValue());
        setRemovingClinicalFindingValue(null);
    }

    public void addPatientAllergy() {
        if (getPatientAllergy().getItemValue() == null) {
            JsfUtil.addErrorMessage("Select Allergy");
            return;
        }
        getPatientAllergy().setPatient(patient);
        getPatientAllergy().setClinicalFindingValueType(ClinicalFindingValueType.PatientAllergy);
        clinicalFindingValueFacade.create(getPatientAllergy());
        getPatientAllergies().add(getPatientAllergy());
        setPatientAllergy(null);
        JsfUtil.addSuccessMessage("Added");
    }

    public void addPatientDiagnosis() {
        if (getPatientDiagnosis().getItemValue() == null) {
            JsfUtil.addErrorMessage("Select Allergy");
            return;
        }
        getPatientDiagnosis().setPatient(patient);
        getPatientDiagnosis().setClinicalFindingValueType(ClinicalFindingValueType.PatientDiagnosis);
        clinicalFindingValueFacade.create(getPatientDiagnosis());
        getPatientDiagnoses().add(getPatientDiagnosis());
        setPatientDiagnosis(null);
        JsfUtil.addSuccessMessage("Added");
    }

    public void addPatientMedicine() {
        System.out.println("addPatientMedicine");
        if (getPatientMedicine().getPrescription().getItem() == null) {
            JsfUtil.addErrorMessage("Select Medicine");
            return;
        }
        getPatientMedicine().setPatient(patient);
        getPatientMedicine().setClinicalFindingValueType(ClinicalFindingValueType.PatientMedicine);
        prescriptionFacade.create(getPatientMedicine().getPrescription());
        clinicalFindingValueFacade.create(getPatientMedicine());
        getPatientMedicines().add(getPatientMedicine());
        setPatientMedicine(null);
        JsfUtil.addSuccessMessage("Added");
    }

    public void addEncounterMedicine() {
        if (getEncounterMedicine().getPrescription().getItem() == null) {
            JsfUtil.addErrorMessage("Select Medicine");
            return;
        }
        getEncounterMedicine().setEncounter(current);
        getEncounterMedicine().setClinicalFindingValueType(ClinicalFindingValueType.VisitMedicine);
        if (getEncounterMedicine().getPrescription().getId() == null) {
            prescriptionFacade.create(getEncounterMedicine().getPrescription());
        } else {
            prescriptionFacade.edit(getEncounterMedicine().getPrescription());
        }
        if (getEncounterMedicine().getId() == null) {
            clinicalFindingValueFacade.create(getEncounterMedicine());
        } else {
            clinicalFindingValueFacade.edit(getEncounterMedicine());
        }
        getEncounterMedicines().add(getEncounterMedicine());
        updateDocuments();
        setEncounterMedicine(null);
        JsfUtil.addSuccessMessage("Added");
    }

    private void updateDocuments() {
        if (userDocumentTemplates == null) {
            return;
        }
        if (getEncounterPrescreptions() == null) {
            return;
        }
        for (DocumentTemplate dt : userDocumentTemplates) {
            if (dt.isAutoGenerate()) {
                for (ClinicalFindingValue cfv : getEncounterPrescreptions()) {
                    if (cfv.getDocumentTemplate().equals(dt)) {
                        cfv.setStringValue(generateDocumentFromTemplate(dt, current));
                    }
                }
            }
        }
    }

    public List<ItemUsage> fillCurrentEncounterMedicines() {
        Map m = new HashMap();
        m.put("pe", getCurrent());
        m.put("t", ItemUsageType.EncounterItems);
        String sql;
        sql = "Select e "
                + " from ItemUsage e "
                + " where e.patientEncounter=:pe "
                + " and e.type=:t "
                + " order by e.id desc";
        return itemUsageFacade.findByJpql(sql, m);
    }

    public List<ItemUsage> fillCurrentEncounterDiagnosis() {
        Map m = new HashMap();
        m.put("pe", getCurrent());
        m.put("t", ItemUsageType.EncounterDiagnosis);
        String sql;
        sql = "Select e "
                + " from ItemUsage e "
                + " where e.patientEncounter=:pe "
                + " and e.type=:t "
                + " order by e.id desc";
        return itemUsageFacade.findByJpql(sql, m);
    }

    public List<Bill> fillPatientBills(Patient patient) {
        return fillPatientBills(patient, null, null);
    }

    public List<Bill> fillPatientBills(Patient patient, List<BillType> bts, Integer count) {
        Map m = new HashMap();
        m.put("p", patient);
        m.put("ret", false);
        String jpql;
        Bill b = new Bill();
        b.getBillType();
        jpql = "Select e "
                + " from Bill e "
                + " where e.patient=:p "
                + " and e.retired=:ret ";
        if (bts == null) {
            jpql += " and e.billType in :bts";
            m.put("bts", bts);
        }
        jpql += " order by e.id desc";
        if (count != null) {
            return getBillFacade().findBySQL(jpql, m, count);
        } else {
            return getBillFacade().findByJpql(jpql, m);
        }
    }

    public List<Bill> fillPatientPharmacyBills(Patient patient) {
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacySale);
        return fillPatientBills(patient, bts, 10);
    }

    public List<Bill> fillPatientOpdBills(Patient patient) {
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.OpdBill);
        return fillPatientBills(patient, bts, 10);
    }

    public List<Bill> fillPatientChannellingBills(Patient patient) {
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.Channel);
        return fillPatientBills(patient, bts, 10);
    }

    public List<Bill> fillPatientChannelBills(Patient patient) {
        Map m = new HashMap();
        m.put("p", patient);
        BillType[] bts = {BillType.ChannelCash, BillType.ChannelAgent, BillType.ChannelStaff, BillType.ChannelOnCall};
        List<BillType> billTypes = Arrays.asList(bts);
        m.put("bts", billTypes);
        String sql;
        sql = "Select b from Bill b where b.patient=:p and b.billType in :bts order by b.id desc";
        return getBillFacade().findByJpql(sql, m);
    }

    public List<PatientInvestigation> fillPatientInvestigations(Patient patient) {
        Map m = new HashMap();
        m.put("p", patient);
        m.put("ret", false);
        String sql;
        sql = "Select e "
                + " from PatientInvestigation e "
                + " where e.patient=:p "
                + " and e.retired=:ret "
                + "order by e.id desc";
        return getPiFacade().findByJpql(sql, m);
    }

    public List<PatientEncounter> fillPatientEncounters(Patient patient, Integer count) {
        Map m = new HashMap();
        m.put("p", patient);
        String sql;
        sql = "Select e from PatientEncounter e where e.patient=:p order by e.id desc";
        if (count != null) {
            return getFacade().findBySQL(sql, m, count);
        } else {
            return getFacade().findByJpql(sql, m);
        }
    }

    public List<PatientEncounter> fillPatientEncounters(Patient patient) {
        return fillPatientEncounters(patient, null);
    }

    public void removeCfv() {
        if (current == null) {
            UtilityController.addErrorMessage("No Patient Encounter");
            return;
        }
        if (removingCfv == null) {
            UtilityController.addErrorMessage("No Finding selected to remove");
            return;
        }
        current.getClinicalFindingValues().remove(removingCfv);
        saveSelected();
        UtilityController.addSuccessMessage("Removed");
    }

    public ClinicalFindingItem getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(ClinicalFindingItem diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Investigation getInvestigation() {
        return investigation;
    }

    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
    }

//    public List<PatientEncounter> getSelectedItems() {
//        selectedItems = getFacade().findByJpql("select c from PatientEncounter c where c.retired=false and i.institutionType = com.divudi.data.PatientEncounterType.Agency and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
//        return selectedItems;
//    }
    public void prepareAdd() {
        setCurrent(new PatientEncounter());
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
    }

    public String createWtChart() {
        setChartNameSeries(getCurrentPatientEncountersDateStrings());
        setChartDataSeries1(getCurrentPatientEncountersWeightStrings());
        setValues1Name("Weight");
        setChartName("Weight Chart");
        setChartString(getSingleLineChartString());
        return "/chart";
    }

    public String getSingleLineChartString() {
        String s = "<br/>"
                + "		var MONTHS = [N1N1N1N1N1N1N1N1];\n"
                + "		var config = {\n"
                + "			type: 'line',\n"
                + "			data: {\n"
                + "				labels: [N1N1N1N1N1N1N1N1],\n"
                + "				datasets: [{\n"
                + "					label: 'My First dataset',\n"
                + "					backgroundColor: window.chartColors.red,\n"
                + "					borderColor: window.chartColors.red,\n"
                + "					data: [\n"
                + "						D1D1D1D1D1D1D1D1 \n"
                + "					],\n"
                + "					fill: false,\n"
                + "				}]\n"
                + "			},\n"
                + "			options: {\n"
                + "				responsive: true,\n"
                + "				title: {\n"
                + "					display: true,\n"
                + "					text: 'Chart.js Line Chart'\n"
                + "				},\n"
                + "				tooltips: {\n"
                + "					mode: 'index',\n"
                + "					intersect: false,\n"
                + "				},\n"
                + "				hover: {\n"
                + "					mode: 'nearest',\n"
                + "					intersect: true\n"
                + "				},\n"
                + "				scales: {\n"
                + "					xAxes: [{\n"
                + "						display: true,\n"
                + "						scaleLabel: {\n"
                + "							display: true,\n"
                + "							labelString: 'Month'\n"
                + "						}\n"
                + "					}],\n"
                + "					yAxes: [{\n"
                + "						display: true,\n"
                + "						scaleLabel: {\n"
                + "							display: true,\n"
                + "							labelString: 'Value'\n"
                + "						}\n"
                + "					}]\n"
                + "				}\n"
                + "			}\n"
                + "		};\n"
                + "<br/>"
                + "		window.onload = function() {\n"
                + "			var ctx = document.getElementById('canvas').getContext('2d');\n"
                + "			window.myLine = new Chart(ctx, config);\n"
                + "		};\n"
                + "<br/>"
                + "		document.getElementById('randomizeData').addEventListener('click', function() {\n"
                + "			config.data.datasets.forEach(function(dataset) {\n"
                + "				dataset.data = dataset.data.map(function() {\n"
                + "					return randomScalingFactor();\n"
                + "				});\n"
                + "<br/>"
                + "			});\n"
                + "<br/>"
                + "			window.myLine.update();\n"
                + "		});\n"
                + "<br/>"
                + "		var colorNames = Object.keys(window.chartColors);\n"
                + "		document.getElementById('addDataset').addEventListener('click', function() {\n"
                + "			var colorName = colorNames[config.data.datasets.length % colorNames.length];\n"
                + "			var newColor = window.chartColors[colorName];\n"
                + "			var newDataset = {\n"
                + "				label: 'Dataset ' + config.data.datasets.length,\n"
                + "				backgroundColor: newColor,\n"
                + "				borderColor: newColor,\n"
                + "				data: [],\n"
                + "				fill: false\n"
                + "			};\n"
                + "<br/>"
                + "			for (var index = 0; index < config.data.labels.length; ++index) {\n"
                + "				newDataset.data.push(randomScalingFactor());\n"
                + "			}\n"
                + "<br/>"
                + "			config.data.datasets.push(newDataset);\n"
                + "			window.myLine.update();\n"
                + "		});\n"
                + "	";

        s = s.replace("D1D1D1D1D1D1D1D1", getChartDataSeries1());
        s = s.replace("N1N1N1N1N1N1N1N1", getChartNameSeries());
        s = s.replace("My First dataset", getValues1Name());
        s = s.replace("Chart.js Line Chart", getChartName());
        return s;
    }

    public String getCurrentPatientEncountersDateStrings() {
        String s = "";
        int i = 0;
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd");

        List<PatientEncounter> re = new ArrayList<>();
        re.addAll(getEncounters());
        Collections.reverse(re);

        for (PatientEncounter e : re) {
            i++;
            s += "'" + format.format(e.getCreatedAt()) + "'";
            if (i != getEncounters().size()) {
                s += ", ";
            }
        }
        return s;
    }

    public String getCurrentPatientEncountersSbpStrings() {
        String val = "";
        int i = 0;

        List<PatientEncounter> re = new ArrayList<>();
        re.addAll(getEncounters());
        Collections.reverse(re);

        for (PatientEncounter e : re) {
            i++;
            val += e.getSbp();
            if (i != getEncounters().size()) {
                val += ", ";
            }
        }
        return val;
    }

    public String getCurrentPatientEncountersDbpStrings() {
        String s = "";
        int i = 0;

        List<PatientEncounter> re = new ArrayList<>();
        re.addAll(getEncounters());
        Collections.reverse(re);

        for (PatientEncounter e : re) {
            i++;
            s += e.getDbp();
            if (i != getEncounters().size()) {
                s += ", ";
            }
        }
        return s;
    }

    public String getCurrentPatientEncountersHeightStrings() {
        String s = "";
        int i = 0;

        List<PatientEncounter> re = new ArrayList<>();
        re.addAll(getEncounters());
        Collections.reverse(re);

        for (PatientEncounter e : re) {
            i++;
            s += e.getHeight();
            if (i != getEncounters().size()) {
                s += ", ";
            }
        }
        return s;
    }

    public String getCurrentPatientEncountersWeightStrings() {
        String s = "";
        int i = 0;

        List<PatientEncounter> re = new ArrayList<>();
        re.addAll(getEncounters());
        Collections.reverse(re);

        for (PatientEncounter e : re) {
            i++;
            s += e.getWeight();
            if (i != getEncounters().size()) {
                s += ", ";
            }
        }
        return s;
    }

    public String createBpChart() {
        setChartNameSeries(getCurrentPatientEncountersDateStrings());
        setChartDataSeries1(getCurrentPatientEncountersSbpStrings());
        setChartDataSeries2(getCurrentPatientEncountersDbpStrings());
        setValues1Name("SBP");
        setValues2Name("DBP");
        setChartName("Blood Pressure Chart");
        setChartString(getDoubleLineChartString());
        return "/chart";
    }

    public String getDoubleLineChartString() {
        String s = "<br/>"
                + "		var MONTHS = [N1N1N1N1N1N1N1N1];\n"
                + "		var config = {\n"
                + "			type: 'line',\n"
                + "			data: {\n"
                + "				labels: [N1N1N1N1N1N1N1N1],\n"
                + "				datasets: [{\n"
                + "					label: 'My First dataset',\n"
                + "					backgroundColor: window.chartColors.red,\n"
                + "					borderColor: window.chartColors.red,\n"
                + "					data: [\n"
                + "						D1D1D1D1D1D1D1D1 \n"
                + "					],\n"
                + "					fill: false,\n"
                + "				}, {\n"
                + "					label: 'My Second dataset',\n"
                + "					fill: false,\n"
                + "					backgroundColor: window.chartColors.blue,\n"
                + "					borderColor: window.chartColors.blue,\n"
                + "					data: [\n"
                + "						D2D2D2D2D2D2D2D2\n"
                + "					],\n"
                + "				}]\n"
                + "			},\n"
                + "			options: {\n"
                + "				responsive: true,\n"
                + "				title: {\n"
                + "					display: true,\n"
                + "					text: 'Chart.js Line Chart'\n"
                + "				},\n"
                + "				tooltips: {\n"
                + "					mode: 'index',\n"
                + "					intersect: false,\n"
                + "				},\n"
                + "				hover: {\n"
                + "					mode: 'nearest',\n"
                + "					intersect: true\n"
                + "				},\n"
                + "				scales: {\n"
                + "					xAxes: [{\n"
                + "						display: true,\n"
                + "						scaleLabel: {\n"
                + "							display: true,\n"
                + "							labelString: 'Month'\n"
                + "						}\n"
                + "					}],\n"
                + "					yAxes: [{\n"
                + "						display: true,\n"
                + "						scaleLabel: {\n"
                + "							display: true,\n"
                + "							labelString: 'Value'\n"
                + "						}\n"
                + "					}]\n"
                + "				}\n"
                + "			}\n"
                + "		};\n"
                + "<br/>"
                + "		window.onload = function() {\n"
                + "			var ctx = document.getElementById('canvas').getContext('2d');\n"
                + "			window.myLine = new Chart(ctx, config);\n"
                + "		};\n"
                + "<br/>"
                + "		document.getElementById('randomizeData').addEventListener('click', function() {\n"
                + "			config.data.datasets.forEach(function(dataset) {\n"
                + "				dataset.data = dataset.data.map(function() {\n"
                + "					return randomScalingFactor();\n"
                + "				});\n"
                + "<br/>"
                + "			});\n"
                + "<br/>"
                + "			window.myLine.update();\n"
                + "		});\n"
                + "<br/>"
                + "		var colorNames = Object.keys(window.chartColors);\n"
                + "		document.getElementById('addDataset').addEventListener('click', function() {\n"
                + "			var colorName = colorNames[config.data.datasets.length % colorNames.length];\n"
                + "			var newColor = window.chartColors[colorName];\n"
                + "			var newDataset = {\n"
                + "				label: 'Dataset ' + config.data.datasets.length,\n"
                + "				backgroundColor: newColor,\n"
                + "				borderColor: newColor,\n"
                + "				data: [],\n"
                + "				fill: false\n"
                + "			};\n"
                + "<br/>"
                + "			for (var index = 0; index < config.data.labels.length; ++index) {\n"
                + "				newDataset.data.push(randomScalingFactor());\n"
                + "			}\n"
                + "<br/>"
                + "			config.data.datasets.push(newDataset);\n"
                + "			window.myLine.update();\n"
                + "		});\n"
                + "	";

        s = s.replace("D1D1D1D1D1D1D1D1", getChartDataSeries1());
        s = s.replace("D2D2D2D2D2D2D2D2", getChartDataSeries2());
        s = s.replace("N1N1N1N1N1N1N1N1", getChartNameSeries());
        s = s.replace("My First dataset", getValues1Name());
        s = s.replace("My Second dataset", getValues2Name());
        s = s.replace("Chart.js Line Chart", getChartName());
        return s;
    }

    public void saveSelected() {
        current.setEncounterDate(new Date());
        current.setDepartment(sessionController.getDepartment());
        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Updated Successfully.");
        } else {

            current.setCreatedAt(new Date());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("Saved Successfully");
        }
        UtilityController.addSuccessMessage("Saved");
    }

    public void saveEncounter(PatientEncounter pe) {
        if (pe.getId() != null && pe.getId() > 0) {
            getFacade().edit(pe);
            UtilityController.addSuccessMessage("Updated Successfully.");
        } else {
            getFacade().create(pe);
        }
    }

    public void updateComments() {
        //   ////// // System.out.println("updating comments");
        //   ////// // System.out.println("current.getComments() = " + current.getComments());
        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
        } else {
            current.setCreatedAt(new Date());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
        }
    }

    public void updatePerson() {
        //   ////// // System.out.println("updating person");
        if (current == null) {
            //   ////// // System.out.println("current = " + current);
            return;
        }
        if (current.getPatient() == null) {
            //   ////// // System.out.println("current.getPatient()  = " + current.getPatient());
            return;
        }
        if (current.getPatient().getPerson() == null) {
            //   ////// // System.out.println("current.getPatient().getPerson() = " + current.getPatient().getPerson());
            return;
        }
        getPersonFacade().edit(current.getPatient().getPerson());
        getPatientFacade().edit(current.getPatient());
    }

    public String issueItems() {
        if (current == null) {
            return "";
        }
        getPharmacySaleController().setSearchedPatient(current.getPatient());
        getPharmacySaleController().setPatientSearchTab(1);
        getPharmacySaleController().setOpdEncounterComments(current.getComments());
        getPharmacySaleController().setFromOpdEncounter(true);
        getPharmacySaleController().setPatientTabId("tabSearchPt");
//        getPharmacySaleController().getBill().setPatientEncounter(current);
//        getPharmacySaleController().getBill().setPatient(current.getPatient());
        return "/clinical/clinical_pharmacy_sale";
    }

    public String issueServices() {
        if (current == null) {
            return "";
        }
        getBillController().prepareNewBill();
        getBillController().setSearchedPatient(current.getPatient());
        getBillController().setFromOpdEncounter(true);
        getBillController().setOpdEncounterComments(current.getComments());
        getBillController().setPatientSearchTab(1);
        getBillController().setPatientTabId("tabSearchPt");
        getBillController().setReferredBy(doctor);
        //        getPharmacySaleController().getBill().setPatientEncounter(current);
        //        getPharmacySaleController().getBill().setPatient(current.getPatient());
        return "/opd_bill";
    }

    public PatientEncounter getEncounterToDisplay() {
        return encounterToDisplay;
    }

    public void setEncounterToDisplay(PatientEncounter encounterToDisplay) {
        this.encounterToDisplay = encounterToDisplay;
    }

    public PatientEncounter getStartedEncounter() {
        return startedEncounter;
    }

    public void setStartedEncounter(PatientEncounter startedEncounter) {
        this.startedEncounter = startedEncounter;
    }

    public String prepareToDisplayPastVisit() {
        if (current == null) {
            JsfUtil.addErrorMessage("No visit");
            return "";
        }
        if (encounterToDisplay == null) {
            JsfUtil.addErrorMessage("Select Visit");
            return "";
        }
        setCurrent(encounterToDisplay);
        return "";
    }

    public void backToStartingEncounter() {
        if (startedEncounter == null) {
            JsfUtil.addErrorMessage("No visit");
            return;
        }
        setCurrent(startedEncounter);
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public PatientEncounterFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(PatientEncounterFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public PatientEncounterController() {
    }

    public PatientEncounter getCurrent() {
        if (current == null) {
            current = new PatientEncounter();
            Patient pt = new Patient();
            Person p = new Person();
            pt.setPerson(p);
            current.setPatient(pt);
        }
        return current;
    }

    public void setCurrent(PatientEncounter current) {
        this.current = current;
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private PatientEncounterFacade getFacade() {
        return ejbFacade;
    }

    public List<PatientEncounter> getItems() {
        return items;
    }

    public String getDiagnosisComments() {
        return diagnosisComments;
    }

    public void setDiagnosisComments(String diagnosisComments) {
        this.diagnosisComments = diagnosisComments;
    }

    public ClinicalFindingValue getRemovingCfv() {
        return removingCfv;
    }

    public void setRemovingCfv(ClinicalFindingValue removingCfv) {
        this.removingCfv = removingCfv;
    }

    public PharmacySaleController getPharmacySaleController() {
        return pharmacySaleController;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = new Date();
            fromDate = getCommonFunctions().getStartOfDay(fromDate);
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
            toDate = getCommonFunctions().getEndOfDay(toDate);
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public ClinicalFindingItemFacade getClinicalFindingItemFacade() {
        return clinicalFindingItemFacade;
    }

    public BillController getBillController() {
        return billController;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public PatientFacade getPatientFacade() {
        return patientFacade;
    }

    public CommonFunctionsController getCommonFunctions() {
        return commonFunctions;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public PatientInvestigationFacade getPiFacade() {
        return piFacade;
    }

    public List<Bill> getOpdBills() {
        return opdBills;
    }

    public List<PatientInvestigation> getInvestigations() {
        return investigations;
    }

    public List<String> getCompleteStrings() {
        return completeStrings;
    }

    public void setCompleteStrings(List<String> completeStrings) {
        this.completeStrings = completeStrings;
    }

    public List<Bill> getChannelBills() {
        return channelBills;
    }

    public void setChannelBills(List<Bill> channelBills) {
        this.channelBills = channelBills;
    }

    public InvestigationItem getGraphInvestigationItem() {
        return graphInvestigationItem;
    }

    public void setGraphInvestigationItem(InvestigationItem graphInvestigationItem) {
        this.graphInvestigationItem = graphInvestigationItem;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

    public List<PatientEncounter> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<PatientEncounter> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public List<Bill> getPharmacyBills() {
        return pharmacyBills;
    }

    public void setPharmacyBills(List<Bill> pharmacyBills) {
        this.pharmacyBills = pharmacyBills;
    }

    public List<ItemUsage> getCurrentEncounterMedicines() {
        return currentEncounterMedicines;
    }

    public void setCurrentEncounterMedicines(List<ItemUsage> currentEncounterMedicines) {
        this.currentEncounterMedicines = currentEncounterMedicines;
    }

    public List<ItemUsage> getCurrentEncounterDiagnosis() {
        return currentEncounterDiagnosis;
    }

    public void setCurrentEncounterDiagnosis(List<ItemUsage> currentEncounterDiagnosis) {
        this.currentEncounterDiagnosis = currentEncounterDiagnosis;
    }

    public List<ClinicalFindingValue> getPatientAllergies() {
        return patientAllergies;
    }

    public void setPatientAllergies(List<ClinicalFindingValue> patientAllergies) {
        this.patientAllergies = patientAllergies;
    }

    public List<ClinicalFindingValue> getPatientMedicines() {
        return patientMedicines;
    }

    public void setPatientMedicines(List<ClinicalFindingValue> patientMedicines) {
        this.patientMedicines = patientMedicines;
    }

    public List<ClinicalFindingValue> getPatientImages() {
        return patientImages;
    }

    public void setPatientImages(List<ClinicalFindingValue> patientImages) {
        this.patientImages = patientImages;
    }

    public List<ClinicalFindingValue> getPatientDiagnoses() {
        return patientDiagnoses;
    }

    public void setPatientDiagnoses(List<ClinicalFindingValue> patientDiagnoses) {
        this.patientDiagnoses = patientDiagnoses;
    }

    public List<ClinicalFindingValue> getPatientDiagnosticImages() {
        return patientDiagnosticImages;
    }

    public void setPatientDiagnosticImages(List<ClinicalFindingValue> patientDiagnosticImages) {
        this.patientDiagnosticImages = patientDiagnosticImages;
    }

    public ClinicalFindingValue getPatientAllergy() {
        if (patientAllergy == null) {
            patientAllergy = new ClinicalFindingValue();
            patientAllergy.setClinicalFindingValueType(ClinicalFindingValueType.PatientAllergy);
        }
        return patientAllergy;
    }

    public void setPatientAllergy(ClinicalFindingValue patientAllergy) {
        this.patientAllergy = patientAllergy;
    }

    public ClinicalFindingValue getPatientMedicine() {
        if (patientMedicine == null) {
            patientMedicine = new ClinicalFindingValue();
            patientMedicine.setClinicalFindingValueType(ClinicalFindingValueType.PatientMedicine);
            Prescription p = new Prescription();
            patientMedicine.setPrescription(p);
        }
        return patientMedicine;
    }

    public void setPatientMedicine(ClinicalFindingValue patientMedicine) {
        this.patientMedicine = patientMedicine;
    }

    public ClinicalFindingValue getPatientImage() {
        if (patientImage == null) {
            patientImage = new ClinicalFindingValue();
            patientImage.setClinicalFindingValueType(ClinicalFindingValueType.PatientImage);
        }
        return patientImage;
    }

    public void setPatientImage(ClinicalFindingValue patientImage) {
        this.patientImage = patientImage;
    }

    public ClinicalFindingValue getPatientDiagnosis() {
        if (patientDiagnosis == null) {
            patientDiagnosis = new ClinicalFindingValue();
            patientDiagnosis.setClinicalFindingValueType(ClinicalFindingValueType.PatientDiagnosis);
        }
        return patientDiagnosis;
    }

    public void setPatientDiagnosis(ClinicalFindingValue patientDiagnosis) {
        this.patientDiagnosis = patientDiagnosis;
    }

    public ClinicalFindingValue getPatientDiagnosticImage() {
        if (patientDiagnosticImage == null) {
            patientDiagnosticImage = new ClinicalFindingValue();
            patientDiagnosticImage.setClinicalFindingValueType(ClinicalFindingValueType.PatientDiagnosticImage);
        }
        return patientDiagnosticImage;
    }

    public void setPatientDiagnosticImage(ClinicalFindingValue patientDiagnosticImage) {
        this.patientDiagnosticImage = patientDiagnosticImage;
    }

    public ClinicalFindingValue getRemovingClinicalFindingValue() {
        return removingClinicalFindingValue;
    }

    public void setRemovingClinicalFindingValue(ClinicalFindingValue removingClinicalFindingValue) {
        this.removingClinicalFindingValue = removingClinicalFindingValue;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public ClinicalFindingValueFacade getClinicalFindingValueFacade() {
        return clinicalFindingValueFacade;
    }

    public void setClinicalFindingValueFacade(ClinicalFindingValueFacade clinicalFindingValueFacade) {
        this.clinicalFindingValueFacade = clinicalFindingValueFacade;
    }

    public ItemUsageFacade getItemUsageFacade() {
        return itemUsageFacade;
    }

    public void setItemUsageFacade(ItemUsageFacade itemUsageFacade) {
        this.itemUsageFacade = itemUsageFacade;
    }

    public PrescriptionFacade getPrescriptionFacade() {
        return prescriptionFacade;
    }

    public void setPrescriptionFacade(PrescriptionFacade prescriptionFacade) {
        this.prescriptionFacade = prescriptionFacade;
    }

    public List<Bill> getOpdVisits() {
        return opdVisits;
    }

    public void setOpdVisits(List<Bill> opdVisits) {
        this.opdVisits = opdVisits;
    }

    public String getChartNameSeries() {
        return chartNameSeries;
    }

    public void setChartNameSeries(String chartNameSeries) {
        this.chartNameSeries = chartNameSeries;
    }

    public String getChartDataSeries1() {
        return chartDataSeries1;
    }

    public void setChartDataSeries1(String chartDataSeries1) {
        this.chartDataSeries1 = chartDataSeries1;
    }

    public String getChartDataSeries2() {
        return chartDataSeries2;
    }

    public void setChartDataSeries2(String chartDataSeries2) {
        this.chartDataSeries2 = chartDataSeries2;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public String getValues1Name() {
        return values1Name;
    }

    public void setValues1Name(String values1Name) {
        this.values1Name = values1Name;
    }

    public String getValues2Name() {
        return values2Name;
    }

    public void setValues2Name(String values2Name) {
        this.values2Name = values2Name;
    }

    public String getChartString() {
        return chartString;
    }

    public void setChartString(String chartString) {
        this.chartString = chartString;
    }

    public FavouriteController getFavouriteController() {
        return favouriteController;
    }

    public void setFavouriteController(FavouriteController favouriteController) {
        this.favouriteController = favouriteController;
    }

    public ClinicalFindingValue getEncounterMedicine() {
        if (encounterMedicine == null) {
            encounterMedicine = new ClinicalFindingValue();
            encounterMedicine.setClinicalFindingValueType(ClinicalFindingValueType.VisitMedicine);
            Prescription p = new Prescription();
            encounterMedicine.setPrescription(p);
        }
        return encounterMedicine;
    }

    public void setEncounterMedicine(ClinicalFindingValue encounterMedicine) {
        this.encounterMedicine = encounterMedicine;
    }

    public List<ClinicalFindingValue> getEncounterMedicines() {
        return encounterMedicines;
    }

    public void setEncounterMedicines(List<ClinicalFindingValue> encounterMedicines) {
        this.encounterMedicines = encounterMedicines;
    }

    public List<ClinicalFindingValue> getEncounterDiagnosticImages() {
        return encounterDiagnosticImages;
    }

    public void setEncounterDiagnosticImages(List<ClinicalFindingValue> encounterDiagnosticImages) {
        this.encounterDiagnosticImages = encounterDiagnosticImages;
    }

    public List<ClinicalFindingValue> getEncounterDiagnoses() {
        return encounterDiagnoses;
    }

    public void setEncounterDiagnoses(List<ClinicalFindingValue> encounterDiagnoses) {
        this.encounterDiagnoses = encounterDiagnoses;
    }

    public List<ClinicalFindingValue> getEncounterInvestigations() {
        return encounterInvestigations;
    }

    public void setEncounterInvestigations(List<ClinicalFindingValue> encounterInvestigations) {
        this.encounterInvestigations = encounterInvestigations;
    }

    public List<ClinicalFindingValue> getEncounterImages() {
        return encounterImages;
    }

    public void setEncounterImages(List<ClinicalFindingValue> encounterImages) {
        this.encounterImages = encounterImages;
    }

    private List<ClinicalFindingValue> fillEncounterInvestigations(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitMedicine);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    private List<ClinicalFindingValue> fillEncounterDiagnoses(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitDiagnosis);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    private List<ClinicalFindingValue> fillEncounterImages(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitImage);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    private List<ClinicalFindingValue> fillEncounterDiadnosticImages(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitDiagnosticImage);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    private List<ClinicalFindingValue> fillEncounterMedicalFitnessCertificates(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitMedicalFitnessCertificate);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    private List<ClinicalFindingValue> fillEncounterMedicalCertificates(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitMedicalLeaveCertificate);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    private List<ClinicalFindingValue> fillEncounterReferrals(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitReferral);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    private List<ClinicalFindingValue> fillEncounterPrescreptions(PatientEncounter encounter) {
        List<ClinicalFindingValueType> clinicalFindingValueTypes = new ArrayList<>();
        clinicalFindingValueTypes.add(ClinicalFindingValueType.VisitPrescription);
        return fillCurrentEncounterFindingValues(encounter, clinicalFindingValueTypes);
    }

    public ClinicalFindingValue getEncounterDiagnosticImage() {
        return encounterDiagnosticImage;
    }

    public void setEncounterDiagnosticImage(ClinicalFindingValue encounterDiagnosticImage) {
        this.encounterDiagnosticImage = encounterDiagnosticImage;
    }

    public ClinicalFindingValue getEncounterDiagnosis() {
        return encounterDiagnosis;
    }

    public void setEncounterDiagnosis(ClinicalFindingValue encounterDiagnosis) {
        this.encounterDiagnosis = encounterDiagnosis;
    }

    public ClinicalFindingValue getEncounterImage() {
        if (encounterImage == null) {
            encounterImage = new ClinicalFindingValue();
            encounterImage.setClinicalFindingValueType(ClinicalFindingValueType.VisitImage);
            if (current != null) {
                encounterImage.setEncounter(current);
                if (current.getPatient() != null) {
                    encounterImage.setPatient(current.getPatient());
                }
            }
        }
        return encounterImage;
    }

    public void setEncounterImage(ClinicalFindingValue encounterImage) {
        this.encounterImage = encounterImage;
    }

    public ClinicalFindingValue getEncounterInvestigation() {
        return encounterInvestigation;
    }

    public void setEncounterInvestigation(ClinicalFindingValue encounterInvestigation) {
        this.encounterInvestigation = encounterInvestigation;
    }

    public ClinicalFindingValue getEncounterMedicalFitnessCertificate() {
        return encounterMedicalFitnessCertificate;
    }

    public void setEncounterMedicalFitnessCertificate(ClinicalFindingValue encounterMedicalFitnessCertificate) {
        this.encounterMedicalFitnessCertificate = encounterMedicalFitnessCertificate;
    }

    public ClinicalFindingValue getEncounterMedicalCertificate() {
        return encounterMedicalCertificate;
    }

    public void setEncounterMedicalCertificate(ClinicalFindingValue encounterMedicalCertificate) {
        this.encounterMedicalCertificate = encounterMedicalCertificate;
    }

    public ClinicalFindingValue getEncounterReferral() {
        return encounterReferral;
    }

    public void setEncounterReferral(ClinicalFindingValue encounterReferral) {
        this.encounterReferral = encounterReferral;
    }

    public ClinicalFindingValue getEncounterPrescreption() {
        return encounterPrescreption;
    }

    public void setEncounterPrescreption(ClinicalFindingValue encounterPrescreption) {
        this.encounterPrescreption = encounterPrescreption;
    }

    public List<ClinicalFindingValue> getEncounterMedicalFitnessCertificates() {
        return encounterMedicalFitnessCertificates;
    }

    public void setEncounterMedicalFitnessCertificates(List<ClinicalFindingValue> encounterMedicalFitnessCertificates) {
        this.encounterMedicalFitnessCertificates = encounterMedicalFitnessCertificates;
    }

    public List<ClinicalFindingValue> getEncounterMedicalCertificates() {
        return encounterMedicalCertificates;
    }

    public void setEncounterMedicalCertificates(List<ClinicalFindingValue> encounterMedicalCertificates) {
        this.encounterMedicalCertificates = encounterMedicalCertificates;
    }

    public List<ClinicalFindingValue> getEncounterReferrals() {
        return encounterReferrals;
    }

    public void setEncounterReferrals(List<ClinicalFindingValue> encounterReferrals) {
        this.encounterReferrals = encounterReferrals;
    }

    public List<ClinicalFindingValue> getEncounterPrescreptions() {
        return encounterPrescreptions;
    }

    public void setEncounterPrescreptions(List<ClinicalFindingValue> encounterPrescreptions) {
        this.encounterPrescreptions = encounterPrescreptions;
    }

    public List<Bill> getPatientBills() {
        return patientBills;
    }

    public void setPatientBills(List<Bill> patientBills) {
        this.patientBills = patientBills;
    }

}

enum ClinicalField {

    History,
    Examination,
    Investigations,
    Treatments,
    Management,
}
