/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.common.BillBeanController;
import com.divudi.bean.common.CommonController;
import com.divudi.bean.common.PriceMatrixController;
import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.UtilityController;
import com.divudi.bean.common.util.JsfUtil;
import com.divudi.bean.inward.InwardBeanController;
import com.divudi.bean.memberShip.PaymentSchemeController;
import com.divudi.data.BillClassType;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.DepartmentType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.StockQty;
import com.divudi.data.Title;
import com.divudi.data.inward.InwardChargeType;
import com.divudi.data.inward.SurgeryBillType;
import com.divudi.ejb.BillNumberGenerator;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.Department;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PreBill;
import com.divudi.entity.PriceMatrix;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.entity.pharmacy.UserStock;
import com.divudi.entity.pharmacy.UserStockContainer;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import com.divudi.facade.StockFacade;
import com.divudi.facade.StockHistoryFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;

@Named
@SessionScoped
public class PharmacySaleBhtController implements Serializable {

    /**
     * Creates a new instance of PharmacySaleController
     */
    public PharmacySaleBhtController() {
    }

    @Inject
    UserStockController userStockController;
    @Inject
    PaymentSchemeController PaymentSchemeController;

    @Inject
    SessionController sessionController;
    @Inject
    CommonController commonController;
    @Inject
    PharmacyCalculation pharmacyCalculation;
////////////////////////
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    ItemFacade itemFacade;
    @EJB
    StockFacade stockFacade;
    @EJB
    PharmacyBean pharmacyBean;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    BillNumberGenerator billNumberBean;
/////////////////////////
    Item selectedAlternative;
    private PreBill preBill;
    Bill printBill;
    Bill bill;
    BillItem billItem;
    //BillItem removingBillItem;
    BillItem editingBillItem;
    Double qty;
    Stock stock;
    private PatientEncounter patientEncounter;
    int activeIndex;
    boolean billPreview = false;
    Department department;
    /////////////////
    List<Stock> replaceableStocks;
    //List<BillItem> billItems;
    List<Item> itemsWithoutStocks;
    List<BillItem> billItems;
    /////////////////////////
    private UserStockContainer userStockContainer;

    private Bill batchBill;
    @Inject
    private BillBeanController billBean;

    public void selectSurgeryBillListener() {
        patientEncounter = getBatchBill().getPatientEncounter();
    }

    public void settleSurgeryBhtIssue() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;

        if (getBatchBill() == null) {
            return;
        }

        if (getBatchBill().getProcedure() == null) {
            return;
        }

        if (getBatchBill().getPatientEncounter().isDischarged()) {
            UtilityController.addErrorMessage("Sorry Patient is Discharged!!!");
            return;
        }

        settleBhtIssue(BillType.PharmacyBhtPre, getBatchBill().getFromDepartment(), BillNumberSuffix.PHISSUE);

        getBillBean().saveEncounterComponents(getPrintBill(), getBatchBill(), getSessionController().getLoggedUser());
        getBillBean().updateBatchBill(getBatchBill());

        commonController.printReportDetails(fromDate, toDate, startTime, "Theater/BHT issue/pharmacy issue/Pharmacy BHT issue(/faces/theater/inward_bill_surgery_issue.xhtml)");

    }

    public void settleSurgeryBhtIssueStore() {
        if (getBatchBill() == null) {
            return;
        }

        if (getBatchBill().getProcedure() == null) {
            return;
        }

        settleBhtIssue(BillType.StoreBhtPre, getBatchBill().getFromDepartment(), BillNumberSuffix.PHISSUE);

        getBillBean().saveEncounterComponents(getPrintBill(), getBatchBill(), getSessionController().getLoggedUser());
        getBillBean().updateBatchBill(getBatchBill());

    }

    public void makeNull() {
        selectedAlternative = null;
        preBill = null;
        printBill = null;
        bill = null;
        billItem = null;
        editingBillItem = null;
        qty = null;
        stock = null;
        activeIndex = 0;
        billPreview = false;
        replaceableStocks = null;
        itemsWithoutStocks = null;
        patientEncounter = null;
        batchBill = null;
    }

    public double getOldQty(BillItem bItem) {
        String sql = "Select b.qty From BillItem b"
                + " where b.retired=false and b.bill=:b and b=:itm";
        HashMap hm = new HashMap();
        hm.put("b", getPreBill());
        hm.put("itm", bItem);
        return getBillItemFacade().findDoubleByJpql(sql, hm);
    }

    public void onEdit(RowEditEvent event) {
        BillItem tmp = (BillItem) event.getObject();
        onEdit(tmp);
    }

    private void setZeroToQty(BillItem tmp) {
        tmp.setQty(0.0);
        tmp.getPharmaceuticalBillItem().setQtyInUnit(0.0f);

        userStockController.updateUserStock(tmp.getTransUserStock(), 0);
    }

    //Check when edititng Qty
    //
    public boolean onEdit(BillItem tmp) {
        //Cheking Minus Value && Null
        if (tmp.getQty() <= 0 || tmp.getQty() == null) {
            setZeroToQty(tmp);
            onEditCalculation(tmp);

            UtilityController.addErrorMessage("Can not enter a minus value");
            return true;
        }

        Stock fetchedStock = getStockFacade().find(tmp.getPharmaceuticalBillItem().getStock().getId());

        if (tmp.getQty() > fetchedStock.getStock()) {
            setZeroToQty(tmp);
            onEditCalculation(tmp);

            UtilityController.addErrorMessage("No Sufficient Stocks?");
            return true;
        }

        //Check Is There Any Other User using same Stock
        if (!userStockController.isStockAvailable(tmp.getPharmaceuticalBillItem().getStock(), tmp.getQty(), getSessionController().getLoggedUser())) {

            setZeroToQty(tmp);
            onEditCalculation(tmp);

            UtilityController.addErrorMessage("Another User On Change Bill Item "
                    + " Qty value is resetted");
            return true;
        }

        userStockController.updateUserStock(tmp.getTransUserStock(), tmp.getQty());

        onEditCalculation(tmp);

        return false;
    }

    private void onEditCalculation(BillItem tmp) {
        tmp.setGrossValue(tmp.getQty() * tmp.getRate());
        tmp.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - tmp.getQty()));

        calculateBillItemForEditing(tmp);

        calTotal();

    }

    public Title[] getTitle() {
        return Title.values();
    }

    public Sex[] getSex() {
        return Sex.values();
    }

    public List<Stock> getReplaceableStocks() {
        return replaceableStocks;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        if (qty != null && qty <= 0) {
            UtilityController.addErrorMessage("Can not enter a minus value");
            return;
        }
        this.qty = qty;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public void setReplaceableStocks(List<Stock> replaceableStocks) {
        this.replaceableStocks = replaceableStocks;
    }

    public Item getSelectedAlternative() {
        return selectedAlternative;
    }

    public void setSelectedAlternative(Item selectedAlternative) {
        this.selectedAlternative = selectedAlternative;
    }

    public void selectReplaceableStocks() {
        if (selectedAlternative == null || !(selectedAlternative instanceof Amp)) {
            replaceableStocks = new ArrayList<>();
            return;
        }
        String sql;
        Map m = new HashMap();
        double d = 0.0;
        Amp amp = (Amp) selectedAlternative;
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        m.put("s", d);
        m.put("vmp", amp.getVmp());
        sql = "select i from Stock i join treat(i.itemBatch.item as Amp) amp where i.stock >:s and i.department=:d and amp.vmp=:vmp order by i.itemBatch.item.name";
        replaceableStocks = getStockFacade().findBySQL(sql, m);
    }

    public List<Item> getItemsWithoutStocks() {
        return itemsWithoutStocks;
    }

    public void setItemsWithoutStocks(List<Item> itemsWithoutStocks) {
        this.itemsWithoutStocks = itemsWithoutStocks;
    }

    public void resetAll() {
        userStockController.retiredAllUserStockContainer(getSessionController().getLoggedUser());
        clearBill();
        clearBillItem();
        billPreview = false;
        makeNull();
        department = null;
    }

    public List<Item> completeRetailSaleItems(String qry) {
        Map m = new HashMap<>();
        List<Item> items;
        String sql;
        sql = "select i from Item i"
                + " where i.retired=false "
                + " and upper(i.name) like :n "
                + " and type(i)=:t and i.id not "
                + " in(select ibs.id from Stock ibs "
                + " where ibs.stock >:s "
                + " and ibs.department=:d "
                + " and upper(ibs.itemBatch.item.name) like :n )"
                + " order by i.name ";
        m.put("t", Amp.class);
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        m.put("n", "%" + qry + "%");
        double s = 0.0;
        m.put("s", s);
        items = getItemFacade().findBySQL(sql, m, 10);
        return items;
    }

    public List<Stock> completeAvailableStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("s", d);
        m.put("depTp", DepartmentType.Store);
        m.put("n", "%" + qry.toUpperCase() + "%");
        if (qry.length() > 4) {
            sql = "select i from Stock i"
                    + " where i.stock >:s"
                    + " and i.department=:d "
                    + " and i.itemBatch.item.departmentType is null "
                    + " or i.itemBatch.item.departmentType!=:depTp "
                    + " and (upper(i.itemBatch.item.name) like :n "
                    + " or upper(i.itemBatch.item.code) like :n "
                    + " or upper(i.itemBatch.item.barcode) like :n )  "
                    + " order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        } else {
            sql = "select i from Stock i "
                    + " where i.stock >:s "
                    + " and i.department=:d"
                    + " and i.itemBatch.item.departmentType is null "
                    + " or i.itemBatch.item.departmentType!=:depTp "
                    + "  and (upper(i.itemBatch.item.name) like :n "
                    + " or upper(i.itemBatch.item.code) like :n)  "
                    + " order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        }
        items = getStockFacade().findBySQL(sql, m, 20);
        itemsWithoutStocks = completeRetailSaleItems(qry);
        ////System.out.println("selectedSaleitems = " + itemsWithoutStocks);
        return items;
    }

    public List<Stock> completeAvailableStocksStore(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("s", d);
        m.put("depTp", DepartmentType.Store);
        m.put("n", "%" + qry.toUpperCase() + "%");
        if (qry.length() > 4) {
            sql = "select i from Stock i"
                    + " where i.stock >:s"
                    + " and i.department=:d "
                    + " and i.itemBatch.item.departmentType=:depTp "
                    + " and (upper(i.itemBatch.item.name) like :n "
                    + " or upper(i.itemBatch.item.code) like :n "
                    + " or upper(i.itemBatch.item.barcode) like :n )  "
                    + " order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        } else {
            sql = "select i from Stock i "
                    + " where i.stock >:s "
                    + " and i.department=:d"
                    + " and i.itemBatch.item.departmentType=:depTp "
                    + "  and (upper(i.itemBatch.item.name) like :n "
                    + " or upper(i.itemBatch.item.code) like :n)  "
                    + " order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        }
        items = getStockFacade().findBySQL(sql, m, 20);
        //  itemsWithoutStocks = completeRetailSaleItems(qry);
        ////System.out.println("selectedSaleitems = " + itemsWithoutStocks);
        return items;
    }

    public BillItem getBillItem() {
        if (billItem == null) {
            billItem = new BillItem();
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            PharmaceuticalBillItem pbi = new PharmaceuticalBillItem();
            pbi.setBillItem(billItem);
            billItem.setPharmaceuticalBillItem(pbi);
        }
        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;
    }

    private void savePreBillFinally(Patient pt, Department matrixDepartment, BillType billType, BillNumberSuffix billNumberSuffix) {
        getPreBill().setBillType(billType);
        getPreBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), billType, BillClassType.PreBill, billNumberSuffix));
        getPreBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), billType, BillClassType.PreBill, billNumberSuffix));

        getPreBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getPreBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getPreBill().setCreatedAt(Calendar.getInstance().getTime());
        getPreBill().setCreater(getSessionController().getLoggedUser());

        getPreBill().setPatient(pt);
        getPreBill().setPatientEncounter(getPatientEncounter());

        getPreBill().setToDepartment(null);
        getPreBill().setToInstitution(null);
        getPreBill().setBillDate(new Date());
        getPreBill().setBillTime(new Date());

        getPreBill().setFromDepartment(matrixDepartment);
        getPreBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
        
        if (!getBillItems().isEmpty()) {
            getPreBill().setReferenceBill(getBillItems().get(0).getReferanceBillItem().getBill());
        }

        getBillBean().setSurgeryData(getPreBill(), getBatchBill(), SurgeryBillType.PharmacyItem);

        if (getPreBill().getId() == null) {
            getBillFacade().create(getPreBill());
        }

    }

    private void savePreBillFinallyRequest(Patient pt, Department matrixDepartment, BillType billType, BillNumberSuffix billNumberSuffix) {
        getPreBill().setBillType(billType);
        getPreBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), billType, BillClassType.PreBill, billNumberSuffix));
        getPreBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), billType, BillClassType.PreBill, billNumberSuffix));

        getPreBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getPreBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getPreBill().setCreatedAt(Calendar.getInstance().getTime());
        getPreBill().setCreater(getSessionController().getLoggedUser());

        getPreBill().setPatient(pt);
        getPreBill().setPatientEncounter(getPatientEncounter());

        getPreBill().setToDepartment(getDepartment());
        getPreBill().setToInstitution(getDepartment().getInstitution());
        getPreBill().setBillDate(new Date());
        getPreBill().setBillTime(new Date());

        getPreBill().setFromDepartment(matrixDepartment);
        getPreBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getBillBean().setSurgeryData(getPreBill(), getBatchBill(), SurgeryBillType.PharmacyItem);

        if (getPreBill().getId() == null) {
            getBillFacade().create(getPreBill());
        }

    }

    private void savePreBillItemsFinally(List<BillItem> list) {
        for (BillItem tbi : list) {
            if (onEdit(tbi)) {//If any issue in Stock Bill Item will not save & not include for total
                continue;
            }

            tbi.setInwardChargeType(InwardChargeType.Medicine);
            tbi.setBill(getPreBill());

            tbi.setCreatedAt(Calendar.getInstance().getTime());
            tbi.setCreater(getSessionController().getLoggedUser());

            PharmaceuticalBillItem tmpPh = tbi.getPharmaceuticalBillItem();
            tbi.setPharmaceuticalBillItem(null);

            if (tbi.getId() == null) {
                getBillItemFacade().create(tbi);
            }

            if (tmpPh.getId() == null) {
                getPharmaceuticalBillItemFacade().create(tmpPh);
            }

            tbi.setPharmaceuticalBillItem(tmpPh);
            getBillItemFacade().edit(tbi);
            
            tbi.getPharmaceuticalBillItem().setBillItem(tbi);
            getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());

            double qtyL = tbi.getPharmaceuticalBillItem().getQtyInUnit() + tbi.getPharmaceuticalBillItem().getFreeQtyInUnit();

            //Deduct Stock
            boolean returnFlag = getPharmacyBean().deductFromStock(tbi.getPharmaceuticalBillItem().getStock(),
                    Math.abs(qtyL), tbi.getPharmaceuticalBillItem(), getPreBill().getDepartment());

            if (!returnFlag) {
                tbi.setTmpQty(0);
                getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
                getBillItemFacade().edit(tbi);
            }

            getPreBill().getBillItems().add(tbi);
        }

        userStockController.retiredAllUserStockContainer(getSessionController().getLoggedUser());

        calculateAllRates();

        getBillFacade().edit(getPreBill());
    }

    private void savePreBillItemsFinallyRequest(List<BillItem> list) {
        for (BillItem tbi : list) {
            if (onEdit(tbi)) {//If any issue in Stock Bill Item will not save & not include for total
                continue;
            }

            tbi.setInwardChargeType(InwardChargeType.Medicine);
            tbi.setBill(getPreBill());

            tbi.setCreatedAt(Calendar.getInstance().getTime());
            tbi.setCreater(getSessionController().getLoggedUser());

            PharmaceuticalBillItem tmpPh = tbi.getPharmaceuticalBillItem();
            tbi.setPharmaceuticalBillItem(null);

            if (tbi.getId() == null) {
                getBillItemFacade().create(tbi);
            }

            if (tmpPh.getId() == null) {
                getPharmaceuticalBillItemFacade().create(tmpPh);
            }

            tbi.setPharmaceuticalBillItem(tmpPh);
            getBillItemFacade().edit(tbi);
            tbi.getPharmaceuticalBillItem().setBillItem(tbi);
            getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());

//            double qtyL = tbi.getPharmaceuticalBillItem().getQtyInUnit() + tbi.getPharmaceuticalBillItem().getFreeQtyInUnit();
//
//            //Deduct Stock
//            boolean returnFlag = getPharmacyBean().deductFromStock(tbi.getPharmaceuticalBillItem().getStock(),
//                    Math.abs(qtyL), tbi.getPharmaceuticalBillItem(), getPreBill().getDepartment());
//
//            if (!returnFlag) {
//                tbi.setTmpQty(0);
//                getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
//                getBillItemFacade().edit(tbi);
//            }
            getPreBill().getBillItems().add(tbi);
        }

        userStockController.retiredAllUserStockContainer(getSessionController().getLoggedUser());

        calculateAllRates();

        getBillFacade().edit(getPreBill());
    }

    private boolean checkAllBillItem() {
        for (BillItem b : getPreBill().getBillItems()) {

            if (onEdit(b)) {
                return true;
            }
        }

        return false;

    }

    public void settlePharmacyBhtIssue() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;
        if (errorCheck()) {
            return;
        }
        settleBhtIssue(BillType.PharmacyBhtPre, getPatientEncounter().getCurrentPatientRoom().getRoomFacilityCharge().getDepartment(), BillNumberSuffix.PHISSUE);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/BHT Bills/Inward Billing(/faces/inward/pharmacy_bill_issue_bht.xhtml)");
    }

    public void settlePharmacyBhtIssueAccept() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;
        if (errorCheck()) {
            return;
        }
        
        if (getBillItems().isEmpty()) {
            UtilityController.addErrorMessage("Nothing To Settle.");
            return;
        }
        
        settleBhtIssueRequestAccept(BillType.PharmacyBhtPre, getPatientEncounter().getCurrentPatientRoom().getRoomFacilityCharge().getDepartment(), BillNumberSuffix.PHISSUE);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/BHT Bills/Inward Billing(/faces/inward/pharmacy_bill_issue_bht.xhtml)");
    }

    public void settlePharmacyBhtIssueRequest() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;
        if (errorCheck()) {
            return;
        }
        settleBhtIssueRequest(BillType.InwardPharmacyRequest, getPatientEncounter().getCurrentPatientRoom().getRoomFacilityCharge().getDepartment(), BillNumberSuffix.PHISSUEREQ);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/BHT Bills/Inward Billing(/faces/inward/pharmacy_bill_issue_bht.xhtml)");
    }

    public void settleStoreBhtIssue() {
        if (errorCheck()) {
            return;
        }
        settleBhtIssue(BillType.StoreBhtPre, getPatientEncounter().getCurrentPatientRoom().getRoomFacilityCharge().getDepartment(), BillNumberSuffix.PHISSUE);
    }

    private boolean errorCheck() {
        if (getPatientEncounter() == null || getPatientEncounter().getPatient() == null) {
            UtilityController.addErrorMessage("Please Select a BHT");
            return true;
        }

        if (getPatientEncounter().getCurrentPatientRoom() == null) {
            UtilityController.addErrorMessage("Please Select Patient Room");
            return true;
        }

        if (getPatientEncounter().getCurrentPatientRoom().getRoomFacilityCharge() == null) {
            UtilityController.addErrorMessage("Please Set Room");
            return true;
        }

        if (getPatientEncounter().isDischarged()) {
            UtilityController.addErrorMessage("Sorry Patient is Discharged!!!");
            return true;
        }

        if (checkAllBillItem()) {
            //  UtilityController.addErrorMessage("Please Set Room 33");
            return true;
        }

        return false;
    }

    private void settleBhtIssue(BillType btp, Department matrixDepartment, BillNumberSuffix billNumberSuffix) {

        if (matrixDepartment == null) {
            UtilityController.addErrorMessage("This Bht can't issue as this Surgery Has No Department");
            return;
        }

        Patient pt = getPatientEncounter().getPatient();
        getPreBill().setPaidAmount(0);

        List<BillItem> tmpBillItems = getPreBill().getBillItems();
        getPreBill().setBillItems(null);

        savePreBillFinally(pt, matrixDepartment, btp, billNumberSuffix);
        savePreBillItemsFinally(tmpBillItems);

        // Calculation Margin
        updateMargin(getPreBill().getBillItems(), getPreBill(), getPreBill().getFromDepartment(), getPatientEncounter().getPaymentMethod());

        setPrintBill(getBillFacade().find(getPreBill().getId()));

        clearBill();
        clearBillItem();
        billPreview = true;

    }

    private void settleBhtIssueRequest(BillType btp, Department matrixDepartment, BillNumberSuffix billNumberSuffix) {

        if (matrixDepartment == null) {
            UtilityController.addErrorMessage("This Bht can't issue as this Surgery Has No Department");
            return;
        }

        Patient pt = getPatientEncounter().getPatient();
        getPreBill().setPaidAmount(0);

        List<BillItem> tmpBillItems = getPreBill().getBillItems();
        getPreBill().setBillItems(null);

        savePreBillFinallyRequest(pt, matrixDepartment, btp, billNumberSuffix);
        savePreBillItemsFinallyRequest(tmpBillItems);

        // Calculation Margin
        updateMargin(getPreBill().getBillItems(), getPreBill(), getPreBill().getFromDepartment(), getPatientEncounter().getPaymentMethod());

        setPrintBill(getBillFacade().find(getPreBill().getId()));

        clearBill();
        clearBillItem();
        billPreview = true;

    }

    private void settleBhtIssueRequestAccept(BillType btp, Department matrixDepartment, BillNumberSuffix billNumberSuffix) {

        if (matrixDepartment == null) {
            UtilityController.addErrorMessage("This Bht can't issue as this Surgery Has No Department");
            return;
        }
        
        Patient pt = getPatientEncounter().getPatient();
        getPreBill().setPaidAmount(0);

        List<BillItem> tmpBillItems = getBillItems();
        getPreBill().setBillItems(null);

        savePreBillFinally(pt, matrixDepartment, btp, billNumberSuffix);
        savePreBillItemsFinally(tmpBillItems);

        // Calculation Margin
        updateMargin(getPreBill().getBillItems(), getPreBill(), getPreBill().getFromDepartment(), getPatientEncounter().getPaymentMethod());

        setPrintBill(getBillFacade().find(getPreBill().getId()));

        clearBill();
        clearBillItem();
        billPreview = true;

    }

    public void updateMargin(List<BillItem> billItems, Bill bill, Department matrixDepartment, PaymentMethod paymentMethod) {
        double total = 0;
        double netTotal = 0;
        double marginTotal = 0;
        for (BillItem bi : billItems) {

            double rate = Math.abs(bi.getRate());
            double margin = 0;

            PriceMatrix priceMatrix = getPriceMatrixController().fetchInwardMargin(bi, rate, matrixDepartment, paymentMethod);

            if (priceMatrix != null) {
                margin = ((bi.getGrossValue() * priceMatrix.getMargin()) / 100);
            }

            bi.setMarginValue(margin);

            bi.setNetValue(bi.getGrossValue() + bi.getMarginValue());
            bi.setAdjustedValue(bi.getNetValue());
            getBillItemFacade().edit(bi);

            total += bi.getGrossValue();
            netTotal += bi.getNetValue();
            marginTotal += bi.getMarginValue();
        }

        bill.setTotal(total);
        bill.setNetTotal(netTotal);
        bill.setMargin(marginTotal);
        getBillFacade().edit(bill);

    }

    @EJB
    private BillFeeFacade billFeeFacade;
    @Inject
    private InwardBeanController inwardBean;

    public InwardBeanController getInwardBean() {
        return inwardBean;
    }

    public void setInwardBean(InwardBeanController inwardBean) {
        this.inwardBean = inwardBean;
    }

    @Inject
    PriceMatrixController priceMatrixController;

    public PriceMatrixController getPriceMatrixController() {
        return priceMatrixController;
    }

    public void setPriceMatrixController(PriceMatrixController priceMatrixController) {
        this.priceMatrixController = priceMatrixController;
    }

    private boolean checkItemBatch() {
        for (BillItem bItem : getPreBill().getBillItems()) {
            if (bItem.getPharmaceuticalBillItem().getStock().getId() == getBillItem().getPharmaceuticalBillItem().getStock().getId()) {
                return true;
            }
        }

        return false;
    }

    public void addBillItem() {

        if (billItem == null) {
            return;
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            return;
        }
        if (getStock() == null) {
            UtilityController.addErrorMessage("Item?");
            return;
        }
        if (getQty() == null) {
            UtilityController.addErrorMessage("Quentity?");
            return;
        }

        Stock fetchStock = getStockFacade().find(getStock().getId());

        if (getQty() > fetchStock.getStock()) {
            UtilityController.addErrorMessage("No Sufficient Stocks?");
            return;
        }

        if (checkItemBatch()) {
            UtilityController.addErrorMessage("Already added this item batch");
            return;
        }
        //Checking User Stock Entity
        if (!userStockController.isStockAvailable(getStock(), getQty(), getSessionController().getLoggedUser())) {
            UtilityController.addErrorMessage("Sorry Already Other User Try to Billing This Stock You Cant Add");
            return;
        }

        billItem.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - qty));
        billItem.getPharmaceuticalBillItem().setStock(stock);
        billItem.getPharmaceuticalBillItem().setItemBatch(getStock().getItemBatch());
        calculateBillItem();

        billItem.setInwardChargeType(InwardChargeType.Medicine);

        billItem.setItem(getStock().getItemBatch().getItem());
        billItem.setBill(getPreBill());

        billItem.setSearialNo(getPreBill().getBillItems().size() + 1);
        getPreBill().getBillItems().add(billItem);

        //User Stock Container Save if New Bill
        UserStockContainer usc = userStockController.saveUserStockContainer(getUserStockContainer(), getSessionController().getLoggedUser());
        UserStock us = userStockController.saveUserStock(billItem, getSessionController().getLoggedUser(), usc);
        billItem.setTransUserStock(us);

        calculateAllRates();

        calTotal();

        clearBillItem();
        setActiveIndex(1);
    }

    private void calTotal() {
        getPreBill().setTotal(0);
        double netTot = 0.0;
        double discount = 0.0;
        double grossTot = 0.0;
        int index = 0;
        for (BillItem b : getPreBill().getBillItems()) {
            if (b.isRetired()) {
                continue;
            }
            b.setSearialNo(index++);

            netTot = netTot + b.getNetValue();
            grossTot = grossTot + b.getGrossValue();
            discount = discount + b.getDiscount();
            getPreBill().setTotal(getPreBill().getTotal() + b.getNetValue());
        }
        getPreBill().setNetTotal(netTot);
        getPreBill().setTotal(grossTot);
        getPreBill().setGrantTotal(grossTot);
        getPreBill().setDiscount(discount);

    }

    @EJB
    private StockHistoryFacade stockHistoryFacade;

    public void removeBillItem(BillItem b) {
        if (b.getTransUserStock().isRetired()) {
            UtilityController.addErrorMessage("This Item Already removed");
            return;
        }

        userStockController.removeUserStock(b.getTransUserStock(), getSessionController().getLoggedUser());

        getPreBill().getBillItems().remove(b.getSearialNo());

        calTotal();
    }

    public void calculateBillItemListner(AjaxBehaviorEvent event) {
        calculateBillItem();
    }

    public void calculateBillItem() {
        if (stock == null) {
            return;
        }
        if (getPreBill() == null) {
            return;
        }
        if (billItem == null) {
            return;
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            return;
        }
        if (billItem.getPharmaceuticalBillItem().getStock() == null) {
            getBillItem().getPharmaceuticalBillItem().setStock(stock);
        }
        if (getQty() == null) {
            qty = 0.0;
        }

        //Bill Item
//        billItem.setInwardChargeType(InwardChargeType.Medicine);
        billItem.setItem(getStock().getItemBatch().getItem());
        billItem.setQty(qty);

        //pharmaceutical Bill Item
        billItem.getPharmaceuticalBillItem().setDoe(getStock().getItemBatch().getDateOfExpire());
        billItem.getPharmaceuticalBillItem().setFreeQty(0.0f);
        billItem.getPharmaceuticalBillItem().setItemBatch(getStock().getItemBatch());
        billItem.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - qty));

        //Rates
        //Values
        billItem.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * qty);
        billItem.setNetValue(qty * billItem.getNetRate());
        billItem.setDiscount(billItem.getGrossValue() - billItem.getNetValue());

    }

    public void calculateBillItemForEditing(BillItem bi) {
        ////System.out.println("calculateBillItemForEditing");
        ////System.out.println("bi = " + bi);
        if (getPreBill() == null || bi == null || bi.getPharmaceuticalBillItem() == null || bi.getPharmaceuticalBillItem().getStock() == null) {
            ////System.out.println("calculateItemForEditingFailedBecause of null");
            return;
        }
        ////System.out.println("bi.getQty() = " + bi.getQty());
        ////System.out.println("bi.getRate() = " + bi.getRate());
        bi.setGrossValue(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate() * bi.getQty());
        bi.setNetValue(bi.getQty() * bi.getNetRate());
        bi.setDiscount(bi.getGrossValue() - bi.getNetValue());
        ////System.out.println("bi.getNetValue() = " + bi.getNetValue());

    }

    public void handleSelect(SelectEvent event) {
        getBillItem().getPharmaceuticalBillItem().setStock(stock);
        calculateRates(billItem);
    }

    public void paymentSchemeChanged(AjaxBehaviorEvent ajaxBehavior) {
        calculateAllRates();
    }

    public void calculateAllRates() {
        ////System.out.println("calculating all rates");
        for (BillItem tbi : getPreBill().getBillItems()) {
            calculateRates(tbi);
            calculateBillItemForEditing(tbi);
        }
        calTotal();
    }

    public void calculateRateListner(AjaxBehaviorEvent event) {

    }

    public void calculateRates(BillItem bi) {
        ////System.out.println("calculating rates");
        if (bi.getPharmaceuticalBillItem().getStock() == null) {
            ////System.out.println("stock is null");
            return;
        }
        getBillItem();
        bi.setRate(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate());
        //   //System.err.println("Rate "+bi.getRate());
//        bi.setDiscount(calculateBillItemDiscountRate(bi));
        //  //System.err.println("Discount "+bi.getDiscount());
        bi.setNetRate(bi.getRate() - bi.getDiscount());
        //  //System.err.println("Net "+bi.getNetRate());
    }

    public List<Stock> completeAvailableStocksSelectedPharmacy(String qry) {
        System.out.println("department = " + department);
        if (department == null) {
            JsfUtil.addErrorMessage("Please Select Depatment");
            return new ArrayList<>();
        }

        String sql;
        Map m = new HashMap();
        m.put("d", department);
        double d = 0.0;
        m.put("s", d);
        m.put("n", "%" + qry.toUpperCase() + "%");
        if (qry.length() > 4) {
            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n or upper(i.itemBatch.item.barcode) like :n )  order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        } else {
            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n)  order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        }
//        itemsWithoutStocks = completeRetailSaleItems(qry);
        ////System.out.println("selectedSaleitems = " + itemsWithoutStocks);
        return getStockFacade().findBySQL(sql, m, 20);
    }

    public void generateBillComponent(Bill b) {

        //User Stock Container Save if New Bill
        UserStockContainer usc = userStockController.saveUserStockContainer(getUserStockContainer(), getSessionController().getLoggedUser());
        System.out.println("b.getPatientEncounter().getBhtNo() = " + b.getPatientEncounter().getBhtNo());
        setPatientEncounter(b.getPatientEncounter());
        billItems = new ArrayList<>();
        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(b)) {
            System.out.println("i.getQtyInUnit() = " + i.getQtyInUnit());
            double billedIssue = getPharmacyCalculation().getBilledInwardPharmacyRequest(i.getBillItem(), BillType.PharmacyBhtPre);
            System.out.println("billedIssue = " + billedIssue);
            double cancelledIssue = getPharmacyCalculation().getCancelledInwardPharmacyRequest(i.getBillItem(), BillType.PharmacyBhtPre);
            System.out.println("cancelledIssue = " + cancelledIssue);
            double refundedIssue = getPharmacyCalculation().getRefundedInwardPharmacyRequest(i.getBillItem(), BillType.PharmacyBhtPre);
            System.out.println("refundedIssue = " + refundedIssue);
            
            double issuableQty = Math.abs(i.getQtyInUnit()) - (Math.abs(billedIssue) - (Math.abs(cancelledIssue)+Math.abs(refundedIssue)));
            System.out.println("issuableQty = " + issuableQty);

            List<StockQty> stockQtys = pharmacyBean.getStockByQty(i.getBillItem().getItem(), issuableQty, getSessionController().getDepartment());
            System.out.println("stockQtys.size() = " + stockQtys.size());

            for (StockQty sq : stockQtys) {
                if (sq.getQty() == 0) {
                    continue;
                }

                //Checking User Stock Entity
                if (!userStockController.isStockAvailable(sq.getStock(), sq.getQty(), getSessionController().getLoggedUser())) {
                    UtilityController.addErrorMessage("Sorry Already Other User Try to Billing This Stock You Cant Add");
                    continue;
                }
                billItem = new BillItem();
                billItem.setPharmaceuticalBillItem(new PharmaceuticalBillItem());
                billItem.getPharmaceuticalBillItem().setQtyInUnit((double) (sq.getQty()));
//                billItem.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - sq.getQty()));
                billItem.getPharmaceuticalBillItem().setStock(sq.getStock());
                billItem.getPharmaceuticalBillItem().setItemBatch(sq.getStock().getItemBatch());

                billItem.setItem(sq.getStock().getItemBatch().getItem());
                billItem.setQty(sq.getQty());

                billItem.getPharmaceuticalBillItem().setDoe(sq.getStock().getItemBatch().getDateOfExpire());
                billItem.getPharmaceuticalBillItem().setFreeQty(0.0f);
                billItem.getPharmaceuticalBillItem().setItemBatch(sq.getStock().getItemBatch());
                billItem.getPharmaceuticalBillItem().setQtyInUnit((double) (sq.getQty()));
//                billItem.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - sq.getQty()));

                billItem.setGrossValue(sq.getStock().getItemBatch().getRetailsaleRate() * sq.getQty());
                billItem.setNetValue(sq.getQty() * billItem.getNetRate());

                billItem.setInwardChargeType(InwardChargeType.Medicine);

                billItem.setItem(sq.getStock().getItemBatch().getItem());
                billItem.setReferanceBillItem(i.getBillItem());

                billItem.setSearialNo(getBillItems().size() + 1);
                getBillItems().add(billItem);

            }

        }

        System.out.println("getBillItems().size() = " + getBillItems().size());
        getPreBill().setBillItems(getBillItems());

//        boolean flag = false;
//        for (BillItem bi : getBillItems()) {
//            if (Objects.equals(bi.getPharmaceuticalBillItem().getStock().getId(), stock.getId())) {
//                flag = true;
//                break;
//            }
//            stock = bi.getPharmaceuticalBillItem().getStock();
//        }
//
//        if (flag) {
//            billItems = null;
//            UtilityController.addErrorMessage("There is Some Item in request that are added Multiple Time in Transfer request!!! please check request you can't issue errornus transfer request");
//        }
    }

    private void clearBill() {
        preBill = null;
        userStockContainer = null;
    }

    private void clearBillItem() {
        billItem = null;
//        removingBillItem = null;
        editingBillItem = null;
        qty = null;
        stock = null;

    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public BillItem getEditingBillItem() {
        return editingBillItem;
    }

    public void setEditingBillItem(BillItem editingBillItem) {
        this.editingBillItem = editingBillItem;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public PreBill getPreBill() {
        if (preBill == null) {
            preBill = new PreBill();
            // preBill.setPaymentScheme(getPaymentSchemeController().getItems().get(0));
        }
        return preBill;
    }

    public void setPreBill(PreBill preBill) {
        this.preBill = preBill;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public PatientFacade getPatientFacade() {
        return patientFacade;
    }

    public void setPatientFacade(PatientFacade patientFacade) {
        this.patientFacade = patientFacade;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public BillNumberGenerator getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberGenerator billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public boolean isBillPreview() {
        return billPreview;
    }

    public void setBillPreview(boolean billPreview) {
        this.billPreview = billPreview;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public Bill getPrintBill() {
        return printBill;
    }

    public void setPrintBill(Bill printBill) {
        this.printBill = printBill;
    }

    public PaymentSchemeController getPaymentSchemeController() {
        return PaymentSchemeController;
    }

    public void setPaymentSchemeController(PaymentSchemeController PaymentSchemeController) {
        this.PaymentSchemeController = PaymentSchemeController;
    }

    public StockHistoryFacade getStockHistoryFacade() {
        return stockHistoryFacade;
    }

    public void setStockHistoryFacade(StockHistoryFacade stockHistoryFacade) {
        this.stockHistoryFacade = stockHistoryFacade;
    }

    public UserStockContainer getUserStockContainer() {
        if (userStockContainer == null) {
            userStockContainer = new UserStockContainer();
        }
        return userStockContainer;
    }

    public void setUserStockContainer(UserStockContainer userStockContainer) {
        this.userStockContainer = userStockContainer;
    }

    public PatientEncounter getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(PatientEncounter patientEncounter) {
        this.patientEncounter = patientEncounter;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public Bill getBatchBill() {
        return batchBill;
    }

    public void setBatchBill(Bill batchBill) {
        this.batchBill = batchBill;
    }

    public BillBeanController getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBeanController billBean) {
        this.billBean = billBean;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public PharmacyCalculation getPharmacyCalculation() {
        return pharmacyCalculation;
    }

    public void setPharmacyCalculation(PharmacyCalculation pharmacyCalculation) {
        this.pharmacyCalculation = pharmacyCalculation;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

}
