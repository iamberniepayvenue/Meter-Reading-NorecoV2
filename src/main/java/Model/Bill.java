package Model;

import java.util.ArrayList;

public class Bill {


    private ArrayList<Rates> mRates;
    private double TotalAmount;
    private double TotalBilledAmount;
    private double NetBillAmountExport;
    private double TotalAmountDueExport;

    public Bill() {

    }

    public Bill(ArrayList<Rates> rates, double totalAmount, double totalBilledAmount,double netBillAmountExport,double totalExportAmountDue) {
        this.mRates = rates;
        TotalAmount = totalAmount;
        TotalBilledAmount = totalBilledAmount;
        NetBillAmountExport = netBillAmountExport;
        TotalAmountDueExport = totalExportAmountDue;
    }

    public ArrayList<Rates> getRates() {
        return mRates;
    }

    public double getTotalAmount() {
        return TotalAmount;
    }

    public double getTotalBilledAmount() {
        return TotalBilledAmount;
    }

    public double getNetBillAmountExport() {
        return NetBillAmountExport;
    }

    public double getTotalAmountDueExport() {
        return TotalAmountDueExport;
    }
}
