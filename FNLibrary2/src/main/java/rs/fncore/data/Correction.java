package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rs.fncore.Const;
import rs.fncore.data.Payment.PaymentType;
import rs.fncore.data.SellItem.VAT;
import rs.fncore.data.SellOrder.OrderType;
/**
 * Коррекция
 * @author nick
 *
 */
public class Correction extends Document {

	/**
	 * Тип коррекции
	 * @author nick
	 *
	 */
	public static enum CorrectionType {
		/**
		 * По предписанию
		 */
		byArbitarity((byte)1),
		/**
		 * Произвольная
		 */
		byPercept((byte)0);
		private final byte bVal;
		CorrectionType(byte bval) {
			bVal = bval;
		}
		public byte bValue() {
			return bVal;
		}
	}
	protected CorrectionType _type = CorrectionType.byPercept;
	protected SellOrder.OrderType _checkType = OrderType.Outcome;
	protected BigDecimal _sum;
	protected VAT _vat = VAT.vat_20;
	protected TaxMode _taxMode = TaxMode.Common;
	protected Map<PaymentType, Payment> _payments = new HashMap<>();
	protected String _baseDocumentNo = Const.EMPTY_STRING;
	protected long _baseDocumentDate = 0;
	protected int _number;
	protected int _shiftNumber;
	public Correction() {
	}
	/**
	 * Создать новую коррекцию
	 * @param type - тип коррекции
	 * @param checkType - тип чека коррекции (приход, расход и т.д.)
	 * @param sum - сумма коррекции
	 * @param vat - ставка НДС
	 * @param taxMode - СНО
	 */
	public Correction(CorrectionType type, SellOrder.OrderType checkType, BigDecimal sum, VAT vat, TaxMode taxMode) {
		_type = type;
		_checkType = checkType;
		_sum = sum;
		_vat = vat;
		_taxMode = taxMode;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		super.writeToParcel(p, flags);
		p.writeInt(_type.ordinal());
		p.writeInt(_checkType.ordinal());
		p.writeString(_sum.toString());
		p.writeInt(_vat.ordinal());
		p.writeInt(_taxMode.ordinal());
		p.writeString(_baseDocumentNo);
		p.writeLong(_baseDocumentDate);
		p.writeInt(_number);
		p.writeInt(_shiftNumber);
		p.writeInt(_payments.size()); 
		for(Payment payment : _payments.values())
			payment.writeToParcel(p,flags);
		_signature.operator().writeToParcel(p, flags);
	}
	/**
	 * Получить платеж по типу
	 * @param type
	 * @return
	 */
	public Payment getPaymentByType(PaymentType type) {
		return _payments.get(type);
	}

	public boolean addPayment(Payment p) {
		if(_payments.containsKey(p.getType())) 
			return false;
		_payments.put(p.getType(), p);
		return true;
	}
	
	/**
	 * Получить список платежей (копию)
	 * @return
	 */
	public List<Payment> getPayments() { return new ArrayList<>(_payments.values()); }
	@Override
	public void readFromParcel(Parcel p) {
		super.readFromParcel(p);
		_type = CorrectionType.values()[p.readInt()];
		_checkType = SellOrder.OrderType.values()[p.readInt()];
		_sum = new BigDecimal(p.readString());
		_vat = VAT.values()[p.readInt()];
		_taxMode = TaxMode.values()[p.readInt()];
		_baseDocumentNo = p.readString();
		_baseDocumentDate = p.readLong();
		_number = p.readInt();
		_shiftNumber = p.readInt();
		int cnt = p.readInt();
		_payments.clear();
		while(cnt-- > 0) {
			Payment payment = new Payment();
			payment.readFromParcel(p);
			_payments.put(payment.getType(), payment);
		}
		if(p.dataAvail() > 0)
			_signature.operator().readFromParcel(p);
		
	}
	/**
	 * Номер смены
	 * @return
	 */
	public int getShiftNumber() { return _shiftNumber; }
	/**
	 * Номер чека
	 * @return
	 */
	public int getNumber() { return _number; }
	/**
	 * Номер документа-основния
	 * @return
	 */
	public String getBaseDocumentNumber() { return _baseDocumentNo; }
	/**
	 * Указать номер документа-основания
	 * @param s
	 */
	public void setBaseDocumentNumber(String s) {
		if(s == null) s  = Const.EMPTY_STRING;
		_baseDocumentNo = s;
	}
	/**
	 * Дата документа-основания
	 * @return
	 */
	public long getBaseDocumentDate() { return _baseDocumentDate; }
	/**
	 * Установить дату документа-основания
	 * @param v
	 */
	public void setBaseDocumentDate(long v) {
		_baseDocumentDate = v;
	}
	
	public void setBaseDocumentDate(Calendar cal) {
		_baseDocumentDate = cal.getTimeInMillis();
	}
	public void setBaseDocumentDate(Date date) {
		_baseDocumentDate = date.getTime();
	}
	/**
	 * Тип коррекции
	 * @return
	 */
	public CorrectionType getType() { return _type; }
	/**
	 * Тип чека коррекции
	 * @return
	 */
	public SellOrder.OrderType getCheckType() { return _checkType; }
	/**
	 * Используемая СНО
	 * @return
	 */
	public TaxMode getTaxMode() { return _taxMode; }
	/**
	 * Используемая ставка НДС
	 * @return
	 */
	public VAT getVATMode() { return _vat; }
	/**
	 * Сумма коррекции 
	 * @return
	 */
	public BigDecimal getSum() { return _sum; }
	/**
	 * Значение ставки НДС
	 * @return
	 */
	public BigDecimal getVATValue() { return _vat.calc(_sum); }
	@Override
	public String getClassName() {
		return Correction.class.getName();
	}
	@SuppressWarnings("unchecked")
	@Override
	public byte[][] pack() {
		add(1055,_vat.bVal());
		add(1173,_type.bValue());
		put(1174,new Tag(new  Pair[] {
						 new Pair<Integer, Tag>(1179, new Tag(_baseDocumentNo))
						,new Pair<Integer, Tag>(1178, new Tag((int)(_baseDocumentDate/1000)))
						 }));
		switch(_vat) {
		case vat_18:
		case vat_20:
			add(1102,getVATValue());
			break;
		case vat_18_118:
		case vat_20_120:
			add(1106,getVATValue());
			break;
		case vat_10:
			add(1103,getVATValue());
			break;
		case vat_10_110:
			add(1107,getVATValue());
			break;
		case vat_none:
			add(1105,getSum());
			break;
		case vat_0:
			add(1104,getSum());
			break;
		}
		BigDecimal [] payments = new BigDecimal[PaymentType.values().length];
		Arrays.fill(payments, BigDecimal.ZERO);
		for(Payment payment: _payments.values()) 
			payments[payment.getType().ordinal()] = payments[payment.getType().ordinal()].add(payment.getValue());
//		if(payments[PaymentType.Cash.ordinal()] > 0)
			add(1031,payments[PaymentType.Cash.ordinal()]);
		
//		if(payments[PaymentType.Card.ordinal()] > 0)
			add(1081,payments[PaymentType.Card.ordinal()]);
//		if(payments[PaymentType.Prepayment.ordinal()] > 0)
			add(1215,payments[PaymentType.Prepayment.ordinal()]);
//		if(payments[PaymentType.Credit.ordinal()] > 0)
			add(1216,payments[PaymentType.Credit.ordinal()]);
//		if(payments[PaymentType.Ahead.ordinal()] > 0)
			add(1217,payments[PaymentType.Ahead.ordinal()]);
		
		return super.pack();
	}

	public static final Parcelable.Creator<Correction> CREATOR = new Parcelable.Creator<Correction>() {

		@Override
		public Correction createFromParcel(Parcel p) {
			Correction result = new Correction();
			result.readFromParcel(p);
			return result;
		}

		@Override
		public Correction[] newArray(int size) {
			return new Correction[size];
		}
		
	};
}
