package Model;

import java.util.ArrayList;

public class Bill {


    private ArrayList<Rates> mRates;
    private float TotalAmount;
    private float TotalBilledAmount;

    public Bill() {

    }

    public Bill(ArrayList<Rates> rates, float totalAmount, float totalBilledAmount) {
        this.mRates = rates;
        TotalAmount = totalAmount;
        TotalBilledAmount = totalBilledAmount;
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




}
