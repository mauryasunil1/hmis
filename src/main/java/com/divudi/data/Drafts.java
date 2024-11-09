package com.divudi.data;

import com.divudi.entity.Bill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Payment;
import com.divudi.entity.WebUser;
import com.divudi.entity.cashTransaction.CashBook;
import com.divudi.entity.cashTransaction.CashBookEntry;
import java.util.Date;
import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author Dr M H B Ariyaratne
 * 
 */
public class Drafts {
    
    
    @ManyToOne
    private Bill bill;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date paymentDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date writtenAt;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date toRealizeAt;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    boolean realized;
    @ManyToOne
    private Payment referancePayment;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date realizedAt;

    @ManyToOne
    private WebUser realizer;

    @Lob
    private String realizeComments;

    @ManyToOne
    private Institution bank;

    @Lob
    private String comments;

   
    private String chequeRefNo;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date chequeDate;

    private String creditCardRefNo;

    double paidValue;

    private int creditDurationInDays;

   

    @ManyToOne
    private Institution institution;

    @ManyToOne
    private Department department;

   
    private String referenceNo;

   
    // New attributes for marking a cheque as paid
    @ManyToOne
    private WebUser chequePayer;  // User who marked the cheque as paid
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date chequePaidAt;  // Timestamp when the cheque was marked as paid
    private boolean chequePaid;  // Indicates if the cheque has been marked as paid
    @ManyToOne
    private Bill chequePaidBill;  // Reference to the Bill where the cheque was marked as paid

    // New attributes for marking a cheque as realized
    @ManyToOne
    private WebUser chequeRealizer;  // User who marked the cheque as realized
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date chequeRealizedAt;  // Timestamp when the cheque was marked as realized
    private boolean chequeRealized;  // Indicates if the cheque has been marked as realized
    @ManyToOne
    private Bill chequeRealizedBill;  // Reference to the Bill where the cheque was marked as realized

    @ManyToOne
    private Institution fromInstitution;
    @ManyToOne
    private Institution toInstitution;
    
}
