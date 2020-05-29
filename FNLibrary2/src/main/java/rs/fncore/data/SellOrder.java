package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rs.fncore.Const;
import rs.fncore.data.Payment.PaymentType;
import rs.fncore.data.SellItem.ItemPaymentType;
import rs.fncore.data.SellItem.VAT;
import rs.utils.Utils;
/**
 * Чек
 * @author nick
 *
 */
public class SellOrder extends Document {

	/**
	 * Тип чека
	 * @author nick
	 *
	 */
	public static enum OrderType {
		/**
		 * Приход
		 */
		Income, 
		/**
		 * Возврат прихода
		 */
		ReturnIncome, 
		/**
		 * Расход
		 */
		Outcome, 
		/**
		 * Возврат расхода
		 */
		ReturnOutcome;
		public byte bValue() {
			return (byte) (ordinal() + 1);
		}
	}

	protected OrderType _type = OrderType.Income;
	protected ParcelableList<SellItem> _items = new ParcelableList<>(SellItem.class);
	protected Map<PaymentType,Payment> _payments = new HashMap<>();
	protected BigDecimal _refund = BigDecimal.ZERO;
	protected int _number;
	protected int _shiftNumber;
	protected String _fnsUrl;
	protected String _senderEmail = Const.EMPTY_STRING;
	protected String _recipientAddress = Const.EMPTY_STRING;
	private AgentData _agentData = new AgentData();

	public SellOrder() {
	}

	public SellOrder(OrderType type, TaxMode mode) {
		_type = type;
		add(1055, mode.bValue());
	}
	/**
	 * Агентские данные
	 * @return
	 */
	public AgentData getAgentData() {
		return _agentData;
	}

	public TaxMode getTaxMode() {
		return TaxMode.decodeOne(get(1055).asByte());
	}
	@Override
	public void writeToParcel(Parcel p, int flags) {
		super.writeToParcel(p, flags);
		p.writeInt(_type.ordinal());
		p.writeString(_refund.toString());
		p.writeInt(_number);
		p.writeInt(_shiftNumber);
		p.writeString(_fnsUrl);
		p.writeString(_senderEmail);
		p.writeString(_recipientAddress);
		_items.writeToParcel(p, flags);
		p.writeInt(_payments.size());
		for(Payment payment: _payments.values())
			payment.writeToParcel(p, flags);
		_agentData.writeToParcel(p, flags);
		_signature.operator().writeToParcel(p, flags);
	}

	@Override
	public void readFromParcel(Parcel p) {
		super.readFromParcel(p);
		_type = OrderType.values()[p.readInt()];
		_refund = new BigDecimal(p.readString());
		_number = p.readInt();
		_shiftNumber = p.readInt();
		_fnsUrl = p.readString();
		_senderEmail = p.readString();
		_recipientAddress = p.readString();
		_items.readFromParcel(p);
		int cnt = p.readInt();
		_payments.clear();
		while(cnt-->0) {
			Payment payment = new Payment();
			payment.readFromParcel(p);
			_payments.put(payment.getType(), payment);
		}
		_agentData.readFromParcel(p);
		if(p.dataAvail() > 0)
			_signature.operator().readFromParcel(p);
	}

	@Override
	public byte[][] pack() {
		remove(1224);
		remove(1223);
		remove(1057);
		Set<AgentType> agents = new HashSet<>();
		BigDecimal vat[] = new BigDecimal[VAT.values().length];
		Arrays.fill(vat, BigDecimal.ZERO);
		for (SellItem item : _items) {
			vat[item.getVATType().ordinal()] = vat[item.getVATType().ordinal()].add(item.getVATValue());
			if(item.getAgentData().getType() != null)
				agents.add(item.getAgentData().getType());
		}
		if (vat[VAT.vat_20.ordinal()].compareTo(BigDecimal.ZERO)==1)
			add(1102, vat[VAT.vat_20.ordinal()]);
		else if (vat[VAT.vat_18.ordinal()].compareTo(BigDecimal.ZERO)==1)
			add(1102, vat[VAT.vat_18.ordinal()]);
		if (vat[VAT.vat_10.ordinal()].compareTo(BigDecimal.ZERO)==1)
			add(1103, vat[VAT.vat_10.ordinal()]);
		if (vat[VAT.vat_20_120.ordinal()].compareTo(BigDecimal.ZERO)==1)
			add(1106, vat[VAT.vat_20_120.ordinal()]);
		else if (vat[VAT.vat_18_118.ordinal()].compareTo(BigDecimal.ZERO)==1)
			add(1106, vat[VAT.vat_18_118.ordinal()]);
		if (vat[VAT.vat_10_110.ordinal()].compareTo(BigDecimal.ZERO)==1)
			add(1107, vat[VAT.vat_10_110.ordinal()]);
		if (vat[VAT.vat_0.ordinal()].compareTo(BigDecimal.ZERO)==1)
			add(1104, vat[VAT.vat_0.ordinal()]);
		if (vat[VAT.vat_none.ordinal()].compareTo(BigDecimal.ZERO)==1)
			add(1105, vat[VAT.vat_none.ordinal()]);
		BigDecimal[] payments = new BigDecimal[PaymentType.values().length];
		Arrays.fill(payments, BigDecimal.ZERO);
		for (Payment payment : _payments.values())
			payments[payment.getType().ordinal()] = payment.getValue();
//		if (payments[PaymentType.Cash.ordinal()] > 0)
			add(1031, payments[PaymentType.Cash.ordinal()]);

//		if (payments[PaymentType.Card.ordinal()] > 0)
			add(1081, payments[PaymentType.Card.ordinal()]);
//		if (payments[PaymentType.Prepayment.ordinal()] > 0)
			add(1215, payments[PaymentType.Prepayment.ordinal()]);
//		if (payments[PaymentType.Credit.ordinal()] > 0)
			add(1216, payments[PaymentType.Credit.ordinal()]);
//		if (payments[PaymentType.Ahead.ordinal()] > 0)
			add(1217, payments[PaymentType.Ahead.ordinal()]);
		if (!_recipientAddress.isEmpty()) {
			add(1008, _recipientAddress);
			if(!_senderEmail.isEmpty())
				add(1117,_senderEmail);
		}
		
		if (_agentData.getType() != null) {
			agents.add(_agentData.getType());
			
			TLV tlv = new TLV();
			for (int id : SellItem.TAGS_1223)
				tlv.put(id, _agentData.get(id));
			if (tlv.size() > 0)
				put(1223, new Tag(tlv));
			for (int id : SellItem.TAGS_1224)
				tlv.put(id, _agentData.get(id));
			if (tlv.size() > 0)
				put(1224, new Tag(tlv));
		}
		if(!agents.isEmpty())
			add(1057,AgentType.encode(agents));
		return super.pack();
	}
	/**
	 * Сумма по предметам расчета
	 * @return
	 */
	public BigDecimal getTotalSum() {
		BigDecimal result = BigDecimal.ZERO;
		for (SellItem item : _items)
			result = result.add(item.getSum());
		return Utils.round2(result,2);
	}
	/**
	 * Сумма платежей
	 * @return
	 */
	public BigDecimal getTotalPayments() {
		BigDecimal result = BigDecimal.ZERO;
		for (Payment payment : _payments.values())
			result = result.add(payment.getValue());
		return Utils.round2(result,2);
	}

	/**
	 * Получить сдачу
	 * @return
	 */
	public BigDecimal getRefund() {
		return Utils.round2(_refund,2);
	}
	/**
	 * Установить значение сдачи
	 * @param val
	 */
	public void setRefund(BigDecimal val) {
		if (val.compareTo(BigDecimal.ZERO)>=0)
			_refund = val;
	}

	/**
	 * Номер смены
	 * @return
	 */
	public int getShiftNumber() {
		return _shiftNumber;
	}
	/**
	 * Номер чека
	 * @return
	 */
	public int getNumber() {
		return _number;
	}
	/**
	 * Адрес сайта ФНС
	 * @return
	 */
	public String getFnsUrl() {
		return _fnsUrl;
	}
	/**
	 * Почтовый адрес отправителя чеков
	 * @return
	 */
	public String getSenderEmail() {
		return _senderEmail;
	}
	/**
	 * Адрес получателя чека
	 * @return
	 */
	public String getRecipientAddress() {
		return _recipientAddress;
	}
	/**
	 * Установить адрес получателя чека
	 * @param v
	 */
	public void setRecipientAddress(String v) {
		if (v == null)
			v = Const.EMPTY_STRING;
		_recipientAddress = v;
	}
	/**
	 * Найти оплату по типа
	 * @param type
	 * @return
	 */
	public Payment getPaymentByType(PaymentType type) {
		return _payments.get(type);
	}
	/**
	 * Тип чека
	 * @return
	 */
	public OrderType getType() {
		return _type;
	}
	
	/**
	 * Получить сумму НДС по чеку
	 * @param vat
	 * @return
	 */
	public BigDecimal getVatValue(VAT vat) {
		BigDecimal result = BigDecimal.ZERO;
		for(SellItem i : _items)
			if(i.getVATType() == vat) 
				result = result.add(i.getVATValue());
		return Utils.round2(result,2);	
	}
	
	/**
	 * Список предметов расчета (копию)
	 * @return
	 */
	public List<SellItem> getSellItems() {
		return new ArrayList<>(_items);
	}
	public boolean addItem(SellItem item) {
		if(item.getPaymentType() == ItemPaymentType.CreditPayment && !_items.isEmpty()) return false;
		if(_items.size() > 0 && _items.get(0).getPaymentType() == ItemPaymentType.CreditPayment) return false;
		_items.add(item);
		return true;
	}
	/**
	 * Добавить оплату
	 * @param payment
	 * @return
	 */
	public boolean addPayment(Payment payment) {
		if(_payments.containsKey(payment.getType())) return false;
		_payments.put(payment.getType(), payment);
		return true;
	}
	/**
	 * Получить копию списка оплат
	 * @return
	 */
	public List<Payment> getPayments() {
		return new ArrayList<>(_payments.values());
	}

	public static final Parcelable.Creator<SellOrder> CREATOR = new Parcelable.Creator<SellOrder>() {

		@Override
		public SellOrder createFromParcel(Parcel p) {
			SellOrder result = new SellOrder();
			result.readFromParcel(p);
			return result;
		}

		@Override
		public SellOrder[] newArray(int size) {
			return new SellOrder[size];
		}

	};

	@Override
	public String getClassName() {
		return SellOrder.class.getName();
	}

}
