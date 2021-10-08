package rs.fncore.data;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Рабочая смена
 *
 * @author nick
 */
public class Shift extends Document {

    public final static String CLASS_NAME="Shift";
    public final static String CLASS_UUID="d4d8fcdc-ebae-11eb-9a03-0242ac130003";
    protected int mShiftNumber = 0;
    protected long mWhenOpen = 0;
    protected boolean mIsOpen = false;
    protected int mLastDocNumber = 0;
    protected KKMInfo.FnWarnings mWarnings = new KKMInfo.FnWarnings(0);
    protected OFDStatistic mOFDStat = new OFDStatistic();
    protected int mNumUnsentMarkNotify;

    public Shift() {
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeInt(mShiftNumber);
        p.writeLong(mWhenOpen);
        p.writeInt(mLastDocNumber);
        p.writeInt(mIsOpen ? 1 : 0);
        p.writeInt(mWarnings.iVal);
        mOFDStat.writeToParcel(p, flags);
        p.writeInt(mNumUnsentMarkNotify);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        mShiftNumber = p.readInt();
        mWhenOpen = p.readLong();
        mLastDocNumber = p.readInt();
        mIsOpen = p.readInt() != 0;
        mWarnings = new KKMInfo.FnWarnings(p.readInt());
        mOFDStat.readFromParcel(p);
        mNumUnsentMarkNotify=p.readInt();
    }

    /**
     * Данные о неотправленных в ОФД документах
     *
     * @return
     */
    public OFDStatistic getOFDStatistic() {
        return mOFDStat;
    }

    /**
     * Данные о неотправленных уведомлений по маркировке
     *
     * @return
     */
    public int getUnsentMarkingNotify() {
        return mNumUnsentMarkNotify;
    }

    /**
     * Открыта ли смена
     *
     * @return
     */
    public boolean isOpen() {
        return mIsOpen;
    }

    /**
     * Номер смены
     *
     * @return
     */
    public int getNumber() {
        return mShiftNumber;
    }

    /**
     * Номер последнего документа в смене если смена открыта, количество документов за смену если закрыта
     *
     * @return
     */
    public int getLastDocumentNumber() {
        return mLastDocNumber;
    }

    /**
     * Когда открыта смена
     *
     * @return
     */
    public long getWhenOpen() {
        return mWhenOpen;
    }

    /**
     * Установить, когда открыта смена
     *
     * @return
     */
    public void setWhenOpen(long when) {
        mWhenOpen = when;
    }

    /**
     * Флаги предупреждений ФН
     *
     * @return
     */
    public KKMInfo.FnWarnings getFNWarnings() {
        return mWarnings;
    }

    public static final Parcelable.Creator<Shift> CREATOR = new Parcelable.Creator<Shift>() {
        @Override
        public Shift createFromParcel(Parcel p) {
            Shift result = new Shift();
            result.readFromParcel(p);
            if (p.dataAvail() > 0)
                result.mSignature.mOperator.readFromParcel(p);
            return result;
        }

        @Override
        public Shift[] newArray(int size) {
            return new Shift[size];
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
