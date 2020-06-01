package rs.fncore.data;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Рабочая смена
 *
 * @author nick
 */
public class Shift extends Document {

    protected int _shift_number = 0;
    protected long _when_open = 0;
    protected boolean _is_open = false;
    protected int _last_doc_number = 0;
    protected int _warnings;
    protected OFDStatistic _ofdState = new OFDStatistic();

    public Shift() {
    }


    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeInt(_shift_number);
        p.writeLong(_when_open);
        p.writeInt(_last_doc_number);
        p.writeInt(_is_open ? 1 : 0);
        p.writeInt(_warnings);
        _ofdState.writeToParcel(p, flags);
        if (flags == 1)
            _signature._operator.writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        _shift_number = p.readInt();
        _when_open = p.readLong();
        _last_doc_number = p.readInt();
        _is_open = p.readInt() != 0;
        _warnings = p.readInt();
        _ofdState.readFromParcel(p);
    }

    /**
     * Данные о неотправленных в ОФД документах
     *
     * @return
     */
    public OFDStatistic getOFDStatistic() {
        return _ofdState;
    }

    /**
     * Открыта ли смена
     *
     * @return
     */
    public boolean isOpen() {
        return _is_open;
    }

    /**
     * Номер смены
     *
     * @return
     */
    public int getNumber() {
        return _shift_number;
    }

    /**
     * Номер последнего документа в смене если смена открыта, количество документов за смену если закрыта
     *
     * @return
     */
    public int getLastDocumentNumber() {
        return _last_doc_number;
    }

    /**
     * Когда открыта смена
     *
     * @return
     */
    public long getWhenOpen() {
        return _when_open;
    }

    /**
     * Флаги предупреждений ФН
     *
     * @return
     */
    public int getFNWarnings() {
        return _warnings;
    }

    public static final Parcelable.Creator<Shift> CREATOR = new Parcelable.Creator<Shift>() {
        @Override
        public Shift createFromParcel(Parcel p) {
            Shift result = new Shift();
            result.readFromParcel(p);
            if (p.dataAvail() > 0)
                result._signature._operator.readFromParcel(p);
            return result;
        }

        @Override
        public Shift[] newArray(int size) {
            return new Shift[size];
        }
    };

    @Override
    public String getClassName() {
        return Shift.class.getName();
    }

}
