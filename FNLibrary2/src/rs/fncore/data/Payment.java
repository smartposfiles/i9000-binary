package rs.fncore.data;

import android.os.Parcel;

/**
 * Оплата
 * @author nick
 *
 */
public class Payment implements IReableFromParcel {

	/**
	 * Способы оплаты
	 * @author nick
	 *
	 */
	public static enum PaymentType {
		/**
		 * Наличные
		 */
		Cash,
		/**
		 * Безналичные
		 */
		Card,
		/**
		 * Предоплата
		 */
		Prepayment,
		/**
		 * Кредит
		 */
		Credit,
		/**
		 * Встречная
		 */
		Ahead
	}
	private PaymentType _type = PaymentType.Cash;
	private double _value = 0;
	public Payment() {} 
	/**
	 * 
	 * @param type - тип способа оплаты
	 * @param value - сумма
	 */
	public Payment(PaymentType type, double value) {
		_type = type;
		_value = value;
	}
	/**
	 * Оплата наличными
	 * @param value сумма
	 */
	public Payment(double value) {
		this(PaymentType.Cash,value);
	}

	@Override
	public int describeContents() {
		return 0;
	}
	/**
	 * Получить тип оплаты
	 * @return
	 */
	public PaymentType getType() { return _type; }
	/**
	 * Получить сумму оплаты
	 * @return
	 */
	public double getValue() { return _value; }
	/**
	 * Изменить сумму оплаты
	 * @param val
	 */
	public void setValue(double val) {
		if(val >= 0)
			_value = val;
	}
	
	@Override
	public void writeToParcel(Parcel p, int arg1) {
		p.writeInt(_type.ordinal());
		p.writeDouble(_value);
	}

	@Override
	public void readFromParcel(Parcel p) {
		_type = PaymentType.values()[p.readInt()];
		_value = p.readDouble();

	}

}
