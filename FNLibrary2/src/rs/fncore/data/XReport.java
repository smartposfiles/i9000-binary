package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import rs.fncore.data.Payment.PaymentType;

/**
 * Нефискальный отчет о сменных остатках
 * @author nick
 *
 */
public class XReport implements IReableFromParcel {

	protected long _date;
	protected Shift _shift = new Shift();
	protected OU _owner = new OU();
	
	protected double [] _income = new double[PaymentType.values().length], 
			          _outcome = new double[PaymentType.values().length],
			          _rests = new double[PaymentType.values().length];
	public XReport() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Приход за смену по типу оплаты 
	 * @param type
	 * @return
	 */
	public double getIncome(PaymentType type) {
		return _income[type.ordinal()];
	}
	/**
	 * Расход за смену по типу оплаты
	 * @param type
	 * @return
	 */
	public double getSpend(PaymentType type) {
		return _outcome[type.ordinal()];
	}
	/**
	 * Остаток за смену по типу оплаты
	 * @param type
	 * @return
	 */
	public double getRest(PaymentType type) {
		return _rests[type.ordinal()];
	}
	/**
	 * Информация о владельце ККМ
	 * @return
	 */
	public OU getOwner() { return _owner; }
	/**
	 * Дата формирования отчета
	 * @return
	 */
	public long getDate() { return _date; }
	/**
	 * Смена
	 * @return
	 */
	public Shift getShift() { return _shift; }
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeLong(_date);
		_shift.writeToParcel(p, flags);
		_owner.writeToParcel(p, flags);
		p.writeDoubleArray(_income);
		p.writeDoubleArray(_outcome);
		p.writeDoubleArray(_rests);
	}

	@Override
	public void readFromParcel(Parcel p) {
		_date = p.readLong();
		_shift.readFromParcel(p);
		_owner.readFromParcel(p);
		p.readDoubleArray(_income);
		p.readDoubleArray(_outcome);
		p.readDoubleArray(_rests);
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
