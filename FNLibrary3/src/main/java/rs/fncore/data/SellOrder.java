package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rs.fncore.Const;
import rs.fncore.FZ54Tag;
import rs.fncore.data.Payment.PaymentTypeE;
import rs.fncore.data.SellItem.ItemPaymentTypeE;
import rs.utils.Utils;

/**
 * Чек
 *
 * @author nick
 */
public class SellOrder extends Document {

    public final static String CLASS_NAME="SellOrder";
    public final static String CLASS_UUID="c8bed264-ebae-11eb-9a03-0242ac130003";

    /**
     * Тип чека
     *
     * @author amv
     */
    public enum OrderTypeE {
        /**
         * Приход
         */
        Income(1,"ПРИХОД"),
        /**
         * Возврат прихода
         */
        ReturnIncome(2,"ВОЗВРАТ ПРИХОДА"),
        /**
         * Расход
         */
        Outcome(3, "РАСХОД"),
        /**
         * Возврат расхода
         */
        ReturnOutcome(4,"ВОЗВРАТ РАСХОДА");

        public final byte bVal;
        public final String pName;

        private OrderTypeE(int value, String name) {
            this.bVal = (byte)value;
            this.pName = name;
        }

        public static OrderTypeE fromByte(byte number){
            for (OrderTypeE val:values()){
                if (val.bVal == number){
                    return val;
                }
            }
            throw new InvalidParameterException("unknown value");
        }

        /**
         * Получить список наименований
         * @return names
         */
        public static String [] getNames(){
            String [] res = new String[values().length];
            for (OrderTypeE val:values()){
                res[val.ordinal()]=val.pName;
            }
            return res;
        }
    }

    protected OrderTypeE mType = OrderTypeE.Income;
    protected ParcelableList<SellItem> mItems = new ParcelableList<>(SellItem.class);
    protected Map<PaymentTypeE, Payment> mPayments = new HashMap<>();
    protected BigDecimal mRefund = BigDecimal.ZERO;
    protected int mNumber;
    protected int mShiftNumber;
    protected String mFnsUrl;
    protected String mSenderEmail = Const.EMPTY_STRING;
    protected String mRecipientAddress = Const.EMPTY_STRING;
    private final AgentData mAgentData = new AgentData();

    public SellOrder() {
    }

    public SellOrder(OrderTypeE type, TaxModeE mode) {
        mType = type;
        setTaxMode(mode);
    }

    /**
     * Агентские данные
     *
     * @return agent data
     */
    public AgentData getAgentData() {
        return mAgentData;
    }

    /**
     * Режим налообложения получить
     *
     * @return tax mode
     */
    public TaxModeE getTaxMode() {
        return TaxModeE.fromByte(get(FZ54Tag.T1055_USED_TAX_SYSTEM).asByte());
    }

    /**
     * Режим налообложения установить
     */
    public void setTaxMode(TaxModeE mode) {
        add(FZ54Tag.T1055_USED_TAX_SYSTEM, mode.bVal);
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeByte(mType.bVal);
        p.writeString(mRefund.toString());
        p.writeInt(mNumber);
        p.writeInt(mShiftNumber);
        p.writeString(mFnsUrl);
        p.writeString(mSenderEmail);
        p.writeString(mRecipientAddress);
        mItems.writeToParcel(p, flags);
        p.writeInt(mPayments.size());
        for (Payment payment : mPayments.values())
            payment.writeToParcel(p, flags);
        mAgentData.writeToParcel(p, flags);
        mSignature.operator().writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        mType = OrderTypeE.fromByte(p.readByte());
        mRefund = new BigDecimal(p.readString());
        mNumber = p.readInt();
        mShiftNumber = p.readInt();
        mFnsUrl = p.readString();
        mSenderEmail = p.readString();
        mRecipientAddress = p.readString();
        mItems.readFromParcel(p);
        int cnt = p.readInt();
        mPayments.clear();
        while (cnt-- > 0) {
            Payment payment = new Payment();
            payment.readFromParcel(p);
            mPayments.put(payment.getType(), payment);
        }
        mAgentData.readFromParcel(p);
        mSignature.operator().readFromParcel(p);
    }

    @Override
    public byte[][] pack() {
        remove(FZ54Tag.T1224_SUPPLIER_DATA_TLV);
        remove(FZ54Tag.T1223_AGENT_DATA_TLV);
        remove(FZ54Tag.T1057_AGENT_FLAG);

        add(FZ54Tag.T1009_TRANSACTION_ADDR, mLocation.getAddress());
        add(FZ54Tag.T1187_TRANSACTION_PLACE, mLocation.getPlace());

        Set<AgentTypeE> agents = new HashSet<>();
        BigDecimal[] vat = new BigDecimal[VatE.values().length];
        Arrays.fill(vat, BigDecimal.ZERO);
        for (SellItem item : mItems) {
            vat[item.getVATType().ordinal()] = vat[item.getVATType().ordinal()].add(item.getVATValue());
            if (item.getAgentData().getType() != null)
                agents.add(item.getAgentData().getType());
        }

        if (vat[VatE.vat_20.ordinal()].compareTo(BigDecimal.ZERO) > 0)
            add(FZ54Tag.T1102_VAT_20_SUM, vat[VatE.vat_20.ordinal()]);
        if (vat[VatE.vat_10.ordinal()].compareTo(BigDecimal.ZERO) > 0)
            add(FZ54Tag.T1103_VAT_10_SUM, vat[VatE.vat_10.ordinal()]);
        if (vat[VatE.vat_20_120.ordinal()].compareTo(BigDecimal.ZERO) > 0)
            add(FZ54Tag.T1106_VAT_20_120_SUM, vat[VatE.vat_20_120.ordinal()]);
        if (vat[VatE.vat_10_110.ordinal()].compareTo(BigDecimal.ZERO) > 0)
            add(FZ54Tag.T1107_VAT_10_110_SUM, vat[VatE.vat_10_110.ordinal()]);
        if (vat[VatE.vat_0.ordinal()].compareTo(BigDecimal.ZERO) > 0)
            add(FZ54Tag.T1104_VAT_0_SUM, vat[VatE.vat_0.ordinal()]);
        if (vat[VatE.vat_none.ordinal()].compareTo(BigDecimal.ZERO) > 0)
            add(FZ54Tag.T1105_NO_VAT_SUM, vat[VatE.vat_none.ordinal()]);
        BigDecimal[] payments = new BigDecimal[PaymentTypeE.values().length];
        Arrays.fill(payments, BigDecimal.ZERO);
        for (Payment payment : mPayments.values())
            payments[payment.getType().ordinal()] = payment.getValue();

        add(FZ54Tag.T1031_CASH_SUM, payments[PaymentTypeE.Cash.ordinal()]);
        add(FZ54Tag.T1081_CARD_SUM, payments[PaymentTypeE.Card.ordinal()]);
        add(FZ54Tag.T1215_PREPAY_SUM, payments[PaymentTypeE.Prepayment.ordinal()]);
        add(FZ54Tag.T1216_POSTPAY_SUM, payments[PaymentTypeE.Credit.ordinal()]);
        add(FZ54Tag.T1217_OTHER_SUM, payments[PaymentTypeE.Ahead.ordinal()]);
        if (!mRecipientAddress.isEmpty()) {
            add(FZ54Tag.T1008_BUYER_PHONE_EMAIL, mRecipientAddress);
        }

        if (!mSenderEmail.isEmpty()) {
            add(FZ54Tag.T1117_SENDER_EMAIL, mSenderEmail);
        }

        if (mAgentData.getType() != null) {
            agents.add(mAgentData.getType());

            TLV tlv = new TLV();
            for (int id : SellItem.TAGS_1223)
                tlv.put(id, mAgentData.get(id));
            if (tlv.size() > 0)
                put(FZ54Tag.T1223_AGENT_DATA_TLV, new Tag(tlv));
            for (int id : SellItem.TAGS_1224)
                tlv.put(id, mAgentData.get(id));
            if (tlv.size() > 0)
                put(FZ54Tag.T1224_SUPPLIER_DATA_TLV, new Tag(tlv));
        }
        if (!agents.isEmpty())
            add(FZ54Tag.T1057_AGENT_FLAG, AgentTypeE.toByteArray(agents));
        return super.pack();
    }

    /**
     * Сумма по предметам расчета
     *
     * @return sum
     */
    public BigDecimal getTotalSum() {
        BigDecimal result = BigDecimal.ZERO;
        for (SellItem item : mItems)
            result = result.add(item.getSum());
        return Utils.round2(result, 2);
    }

    /**
     * Сумма платежей
     *
     * @return payments
     */
    public BigDecimal getTotalPayments() {
        BigDecimal result = BigDecimal.ZERO;
        for (Payment payment : mPayments.values())
            result = result.add(payment.getValue());
        return Utils.round2(result, 2);
    }

    /**
     * Получить сдачу
     *
     * @return refund
     */
    public BigDecimal getRefund() {
        return Utils.round2(mRefund, 2);
    }

    /**
     * Установить значение сдачи
     *
     * @param val refund
     */
    public void setRefund(BigDecimal val) {
        if (val.compareTo(BigDecimal.ZERO) >= 0)
            mRefund = val;
    }

    /**
     * Номер смены
     *
     * @return shift
     */
    public int getShiftNumber() {
        return mShiftNumber;
    }

    /**
     * Номер чека
     *
     * @return bill number
     */
    public int getNumber() {
        return mNumber;
    }

    /**
     * Адрес сайта ФНС
     *
     * @return fns url
     */
    public String getFnsUrl() {
        return mFnsUrl;
    }

    /**
     * Почтовый адрес отправителя чеков
     *
     * @return sender mail
     */
    public String getSenderEmail() {
        return mSenderEmail;
    }

    /**
     * Адрес получателя чека
     *
     * @return recepient address
     */
    public String getRecipientAddress() {
        return mRecipientAddress;
    }

    /**
     * Установить адрес получателя чека
     *
     * @param v recepient address
     */
    public void setRecipientAddress(String v) {
        if (v == null)
            v = Const.EMPTY_STRING;
        mRecipientAddress = v;
    }

    /**
     * Найти оплату по типа
     *
     * @param type
     * @return
     */
    public Payment getPaymentByType(PaymentTypeE type) {
        return mPayments.get(type);
    }

    /**
     * Получить тип чека
     *
     * @return
     */
    public OrderTypeE getType() {
        return mType;
    }

    /**
     * Установить тип чека
     *
     * @return
     */
    public void setType(OrderTypeE type) {
        mType = type;
    }

    /**
     * Содержит маркированные товары
     *
     * @return
     */
    public boolean haveMarkingItems() {
        for (SellItem item : mItems){
            if (!item.getMarkingCode().isEmpty()){
                return true;
            }
        }
        return false;
    }

    /**
     * Получить сумму НДС по чеку
     *
     * @param vat
     * @return
     */
    public BigDecimal getVatValue(VatE vat) {
        BigDecimal result = BigDecimal.ZERO;
        for (SellItem i : mItems)
            if (i.getVATType() == vat)
                result = result.add(i.getVATValue());
        return Utils.round2(result, 2);
    }

    /**
     * Список предметов расчета (копию)
     *
     * @return
     */
    public List<SellItem> getItems() {
        return new ArrayList<>(mItems);
    }

    /**
     * Добавить новую поизцию
     *
     * @return
     */
    public boolean addItem(SellItem item) {
        if (item.getPaymentType() == ItemPaymentTypeE.CreditPayment && !mItems.isEmpty())
            return false;
        if (mItems.size() > 0 && mItems.get(0).getPaymentType() == ItemPaymentTypeE.CreditPayment)
            return false;
        mItems.add(item);
        return true;
    }

    /**
     * Удалить позицию
     *
     * @return
     */
    public void removeItem(SellItem item){
        mItems.remove(item);
    }

    /**
     * Добавить оплату
     *
     * @param payment
     * @return
     */
    public boolean addPayment(Payment payment) {
        if (mPayments.containsKey(payment.getType())) return false;
        mPayments.put(payment.getType(), payment);
        return true;
    }

    /**
     * Получить копию списка оплат
     *
     * @return
     */
    public List<Payment> getPayments() {
        return new ArrayList<>(mPayments.values());
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
        return CLASS_NAME;
    }

    @Override
    public String getClassUUID() {
        return CLASS_UUID;
    }
}
