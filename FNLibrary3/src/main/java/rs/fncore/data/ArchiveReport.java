package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import rs.fncore.Const;
import rs.fncore.FZ54Tag;

/**
 * Отчет о переводе ФН в постфискальный режим
 *
 * @author nick
 */
public class ArchiveReport extends Document {

    protected boolean mIsAutomateMode;
    protected String mAutomateNumber = Const.EMPTY_STRING;
    public final static String CLASS_NAME="ArchiveReport";
    public final static String CLASS_UUID="ae948b36-ebae-11eb-9a03-0242ac130003";

    public ArchiveReport() {
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeInt(mIsAutomateMode ? 1 : 0);
        p.writeString(mAutomateNumber);
        mSignature.operator().writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        mIsAutomateMode = p.readInt() != 0;
        mAutomateNumber = p.readString();
        if (p.dataAvail() > 0)
            mSignature.operator().readFromParcel(p);
    }

    /**
     * Признак "установлен в автомате"
     *
     * @return
     */
    public boolean isAutomatedMode() {
        return mIsAutomateMode;
    }

    /**
     * Номер автомата
     *
     * @return
     */
    public String getAutomateNumber() {
        return mAutomateNumber;
    }

    /**
     * Номер последней смены
     *
     * @return
     */
    public int getShiftNumber() {
        return get(FZ54Tag.T1038_SHIFT_NO).asInt();
    }

    public static final Parcelable.Creator<ArchiveReport> CREATOR = new Parcelable.Creator<ArchiveReport>() {
        @Override
        public ArchiveReport createFromParcel(Parcel p) {
            ArchiveReport result = new ArchiveReport();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public ArchiveReport[] newArray(int size) {
            return new ArchiveReport[size];
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
