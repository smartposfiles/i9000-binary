package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import rs.fncore.FZ54Tag;

/**
 * Отчет о состоянии расчетов
 *
 * @author nick
 */
public class FiscalReport extends Document {

    public final static String CLASS_NAME="FiscalReport";
    public final static String CLASS_UUID="bf308fbc-ebae-11eb-9a03-0242ac130003";

    protected OFDStatistic mOfdStatistic = new OFDStatistic();
    protected Shift mShift = new Shift();
    protected int mNumUnsentMarkNotify;

    public FiscalReport() {
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        mOfdStatistic.writeToParcel(p, flags);
        mShift.writeToParcel(p, flags);
        mSignature.operator().writeToParcel(p, flags);
        p.writeInt(mNumUnsentMarkNotify);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        mOfdStatistic.readFromParcel(p);
        mShift.readFromParcel(p);
        mSignature.operator().readFromParcel(p);
        mNumUnsentMarkNotify=p.readInt();
    }

    /**
     * Информация по документам для отправки в ОФД
     *
     * @return
     */
    public OFDStatistic getOFDStatistic() {
        return mOfdStatistic;
    }

    /**
     * Получить текущую (последнюю) смену
     *
     * @return
     */
    public Shift getShift() {
        return mShift;
    }

    /**
     * Данные о неотправленных уведомлений по маркировке
     *
     * @return
     */
    public int getUnsentMarkingNotify() {
        return mNumUnsentMarkNotify;
    }

    public static final Parcelable.Creator<FiscalReport> CREATOR = new Parcelable.Creator<FiscalReport>() {
        @Override
        public FiscalReport createFromParcel(Parcel p) {
            FiscalReport result = new FiscalReport();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public FiscalReport[] newArray(int size) {
            return new FiscalReport[size];
        }
    };

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public String getClassUUID() {
        return CLASS_UUID;
    }
}
