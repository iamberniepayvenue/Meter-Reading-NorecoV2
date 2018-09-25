package Model;

import java.util.ArrayList;

public class Bill {


    private ArrayList<Rates> mRates;
    private float TotalAmount;
    private float TotalBilledAmount;
    private float NetBillAmountExport;
    private float TotalAmountDueExport;

    public Bill() {

    }

    public Bill(ArrayList<Rates> rates, float totalAmount, float totalBilledAmount,float netBillAmountExport,float totalExportAmountDue) {
        this.mRates = rates;
        TotalAmount = totalAmount;
        TotalBilledAmount = totalBilledAmount;
        NetBillAmountExport = netBillAmountExport;
        TotalAmountDueExport = totalExportAmountDue;
    }

    public ArrayList<Rates> getRates() {
        return mRates;
    }

    public float getTotalAmount() {
        return TotalAmount;
    }

    public float getTotalBilledAmount() {
        return TotalBilledAmount;
    }

    public float getNetBillAmountExport() {
        return NetBillAmountExport;
    }

    public float getTotalAmountDueExport() {
        return TotalAmountDueExport;
    }
}
