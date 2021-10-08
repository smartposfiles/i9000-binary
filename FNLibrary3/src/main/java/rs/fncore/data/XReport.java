package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import rs.fncore.data.Payment.PaymentTypeE;

public class XReport extends Document {
    public final static String CLASS_NAME="XReport";
    public final static String CLASS_UUID="d111a7aa-ebb0-11eb-9a03-0242ac130003";

    protected long mDate;

    protected double[] mIncomeSum = new double[PaymentTypeE.values().length];
    protected double[] mOutcomeSum = new double[PaymentTypeE.values().length];
    protected double[] mReturnIncomeSum = new double[PaymentTypeE.values().length];
    protected double[] mReturnOutcomeSum = new double[PaymentTypeE.values().length];

    protected int mIncomeCount = 0;
    protected int mOutcomeCount = 0;
    protected int mReturnIncomeCount = 0;
    protected int mReturnOutcomeCount = 0;

    protected double mCashInSum = 0;
    protected double mCashOutSum = 0;

    protected int mCashInCount = 0;
    protected int mCashOutCount = 0;

    protected double mCashBoxSum = 0;

    protected OU mOwner = new OU();
    protected Shift mShift = new Shift();

    public long getDate() {
        return mDate;
    }

    public double getIncomeSum(PaymentTypeE type) {
        return mIncomeSum[type.ordinal()];
    }

    public double getOutcomeSum(PaymentTypeE type) {
        return mOutcomeSum[type.ordinal()];
    }

    public double getReturnIncomeSum(PaymentTypeE type) {
        return mReturnIncomeSum[type.ordinal()];
    }

    public double getReturnOutcomeSum(PaymentTypeE type) {
        return mOutcomeSum[type.ordinal()];
    }

    public int getIncomeCount() {
        return mIncomeCount;
    }

    public int getOutcomeCount() {
        return mOutcomeCount;
    }

    public int getReturnIncomeCount() {
        return mReturnIncomeCount;
    }

    public int getReturnOutcomeCount() {
        return mReturnOutcomeCount;
    }

    public double getCashInSum() {
        return mCashInSum;
    }

    public double getCashOutSum() {
        return mCashOutSum;
    }

    public int getCashInCount() {
        return mCashInCount;
    }

    public int getCashOutCount() {
        return mCashOutCount;
    }

    public double getCashBoxSum() {
        return mCashBoxSum;
    }

    public OU getOwner() {
        return mOwner;
    }

    public Shift getShift() {
        return mShift;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeLong(mDate);

        p.writeDoubleArray(mIncomeSum);
        p.writeDoubleArray(mOutcomeSum);
        p.writeDoubleArray(mReturnIncomeSum);
        p.writeDoubleArray(mReturnOutcomeSum);

        p.writeInt(mIncomeCount);
        p.writeInt(mOutcomeCount);
        p.writeInt(mReturnIncomeCount);
        p.writeInt(mReturnOutcomeCount);

        p.writeDouble(mCashInSum);
        p.writeDouble(mCashOutSum);

        p.writeInt(mCashInCount);
        p.writeInt(mCashOutCount);

        p.writeDouble(mCashBoxSum);

        mOwner.writeToParcel(p, flags);
        mShift.writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        mDate = p.readLong();

        p.readDoubleArray(mIncomeSum);
        p.readDoubleArray(mOutcomeSum);
        p.readDoubleArray(mReturnIncomeSum);
        p.readDoubleArray(mReturnOutcomeSum);

        mIncomeCount = p.readInt();
        mOutcomeCount = p.readInt();
        mReturnIncomeCount = p.readInt();
        mReturnOutcomeCount = p.readInt();

        mCashInSum = p.readDouble();
        mCashOutSum = p.readDouble();

        mCashInCount = p.readInt();
        mCashOutCount = p.readInt();

        mCashBoxSum = p.readDouble();

        mOwner.readFromParcel(p);
        mShift.readFromParcel(p);
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public String getClassUUID() {
        return CLASS_UUID;
    }

    public static final Parcelable.Creator<XReport> CREATOR = new Parcelable.Creator<XReport>() {
        @Override
        public XReport createFromParcel(Parcel p) {
            XReport result = new XReport();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public XReport[] newArray(int size) {
            return new XReport[size];
        }
    };
}
