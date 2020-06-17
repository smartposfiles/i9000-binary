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

    protected boolean _isAutomateMode;
    protected String _automateNumber = Const.EMPTY_STRING;

    public ArchiveReport() {
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeInt(_isAutomateMode ? 1 : 0);
        p.writeString(_automateNumber);
        _signature.operator().writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        _isAutomateMode = p.readInt() != 0;
        _automateNumber = p.readString();
        if (p.dataAvail() > 0)
            _signature.operator().readFromParcel(p);
    }

    /**
     * Признак "установлен в автомате"
     *
     * @return
     */
    public boolean isAutomatedMode() {
        return _isAutomateMode;
    }

    /**
     * Номер автомата
     *
     * @return
     */
    public String getAutomateNumber() {
        return _automateNumber;
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
        return ArchiveReport.class.getName();
    }
}
