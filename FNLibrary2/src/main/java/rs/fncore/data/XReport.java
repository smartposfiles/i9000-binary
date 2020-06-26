package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import rs.fncore.data.Payment.PaymentType;

public class XReport implements IReableFromParcel {
    protected long _date;

    protected double[] _incomeSum = new double[PaymentType.values().length];
    protected double[] _outcomeSum = new double[PaymentType.values().length];
    protected double[] _returnIncomeSum = new double[PaymentType.values().length];
    protected double[] _returnOutcomeSum = new double[PaymentType.values().length];

    protected int _incomeCount = 0;
    protected int _outcomeCount = 0;
    protected int _returnIncomeCount = 0;
    protected int _returnOutcomeCount = 0;

    protected double _cashInSum = 0;
    protected double _cashOutSum = 0;

    protected int _cashInCount = 0;
    protected int _cashOutCount = 0;

    protected double _cashBoxSum = 0;

    protected OU _owner = new OU();
    protected Shift _shift = new Shift();

    public long getDate() {
        return _date;
    }

    public double getIncomeSum(PaymentType type) {
        return _incomeSum[type.ordinal()];
    }

    public double getOutcomeSum(PaymentType type) {
        return _outcomeSum[type.ordinal()];
    }

    public double getReturnIncomeSum(PaymentType type) {
        return _returnIncomeSum[type.ordinal()];
    }

    public double getReturnOutcomeSum(PaymentType type) {
        return _outcomeSum[type.ordinal()];
    }

    public int getIncomeCount() {
        return _incomeCount;
    }

    public int getOutcomeCount() {
        return _outcomeCount;
    }

    public int getReturnIncomeCount() {
        return _returnIncomeCount;
    }

    public int getReturnOutcomeCount() {
        return _returnOutcomeCount;
    }

    public double getCashInSum() {
        return _cashInSum;
    }

    public double getCashOutSum() {
        return _cashOutSum;
    }

    public int getCashInCount() {
        return _cashInCount;
    }

    public int getCashOutCount() {
        return _cashOutCount;
    }

    public double getCashBoxSum() {
        return _cashBoxSum;
    }

    public OU getOwner() {
        return _owner;
    }

    public Shift getShift() {
        return _shift;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeLong(_date);

        p.writeDoubleArray(_incomeSum);
        p.writeDoubleArray(_outcomeSum);
        p.writeDoubleArray(_returnIncomeSum);
        p.writeDoubleArray(_returnOutcomeSum);

        p.writeInt(_incomeCount);
        p.writeInt(_outcomeCount);
        p.writeInt(_returnIncomeCount);
        p.writeInt(_returnOutcomeCount);

        p.writeDouble(_cashInSum);
        p.writeDouble(_cashOutSum);

        p.writeInt(_cashInCount);
        p.writeInt(_cashOutCount);

        p.writeDouble(_cashBoxSum);

        _owner.writeToParcel(p, flags);
        _shift.writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        _date = p.readLong();

        p.readDoubleArray(_incomeSum);
        p.readDoubleArray(_outcomeSum);
        p.readDoubleArray(_returnIncomeSum);
        p.readDoubleArray(_returnOutcomeSum);

        _incomeCount = p.readInt();
        _outcomeCount = p.readInt();
        _returnIncomeCount = p.readInt();
        _returnOutcomeCount = p.readInt();

        _cashInSum = p.readDouble();
        _cashOutSum = p.readDouble();

        _cashInCount = p.readInt();
        _cashOutCount = p.readInt();

        _cashBoxSum = p.readDouble();

        _owner.readFromParcel(p);
        _shift.readFromParcel(p);
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
