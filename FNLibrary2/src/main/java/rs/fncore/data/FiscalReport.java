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

    protected OFDStatistic _ofdStatistic = new OFDStatistic();
    protected Shift _shift = new Shift();

    public FiscalReport() {
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        _ofdStatistic.writeToParcel(p, flags);
        _shift.writeToParcel(p, flags);
        _signature.operator().writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        _ofdStatistic.readFromParcel(p);
        _shift.readFromParcel(p);
        if (p.dataAvail() > 0)
            _signature.operator().readFromParcel(p);

    }

    /**
     * Информация по документам для отправки в ОФД
     *
     * @return
     */
    public OFDStatistic getOFDStatistic() {
        return _ofdStatistic;
    }

    /**
     * Получить текущую (последнюю) смену
     *
     * @return
     */
    public Shift getShift() {
        return _shift;
    }

    /**
     * Находится ли ККМ в автономном режиме
     *
     * @return
     */
    public boolean isKKMOffline() {
        return hasTag(FZ54Tag.T1002_AUTONOMOUS_MODE);
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
        return FiscalReport.class.getName();
    }
}
