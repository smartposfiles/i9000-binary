package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rs.fncore.Const;
import rs.fncore.FZ54Tag;
import rs.fncore.data.Payment.PaymentTypeE;
import rs.fncore.data.SellOrder.OrderTypeE;
import rs.utils.Utils;

/**
 * Коррекция
 *
 * @author nick
 */
public class Correction extends Document {

    public final static String CLASS_NAME="Correction";
    public final static String CLASS_UUID="632d0ae2-ebae-11eb-9a03-0242ac130003";

    /**
     * Тип коррекции
     *
     * @author nick
     */
    public enum CorrectionTypeE {
        /**
         * По предписанию
         */
        byArbitarity(1, "ПО ПРЕДПИСАНИЮ"),

        /**
         * Самостоятельно
         */
        byOwn(0, "САМОСТОЯТЕЛЬНО")
        ;

        public final byte bVal;
        public final String pName;

        private CorrectionTypeE(int value, String name) {
            this.bVal = (byte)value;
            this.pName = name;
        }

        public static CorrectionTypeE fromByte(byte number){
            for (CorrectionTypeE val:values()){
                if (val.bVal == number){
                    return val;
                }
            }
            throw new InvalidParameterException("unknown value");
        }

        /**
         * Получить список наименований
         * @return
         */
        public static String [] getNames(){
            String [] res = new String[values().length];
            for (CorrectionTypeE val:values()){
                res[val.ordinal()]=val.pName;
            }
            return res;
        }
    }

    protected ParcelableList<SellItem> mItems = new ParcelableList<>(SellItem.class);
    protected CorrectionTypeE mType = CorrectionTypeE.byOwn;
    protected OrderTypeE mOrderType = OrderTypeE.Outcome;
    protected BigDecimal mSum = BigDecimal.ZERO;
    protected VatE mVat = VatE.vat_20;
    protected TaxModeE mTaxMode = TaxModeE.Common;
    protected Map<PaymentTypeE, Payment> mPayments = new HashMap<>();
    protected String mBaseDocumentNo = Const.EMPTY_STRING;
    protected long mBaseDocumentDate = 0;
    protected int mNumber;
    protected int mShiftNumber;

    public Correction() {
    }

    /**
     * Создать новую коррекцию
     *
     * @param type      - тип коррекции
     * @param orderType - тип чека коррекции (приход, расход и т.д.)
     * @param sum       - сумма коррекции
     * @param vat       - ставка НДС
     * @param taxMode   - СНО
     */
    public Correction(CorrectionTypeE type, OrderTypeE orderType, BigDecimal sum, VatE vat, TaxModeE taxMode) {
        mSum = sum;
        mVat = vat;
        setType(type);
        setOrderType(orderType);
        setTaxMode(taxMode);
    }

    /**
     * Создать новую коррекцию
     *
     * @param type      - тип коррекции
     * @param orderType - тип чека коррекции (приход, расход и т.д.)
     * @param taxMode   - СНО
     */
    public Correction(CorrectionTypeE type, OrderTypeE orderType, TaxModeE taxMode) {
        setType(type);
        setOrderType(orderType);
        setTaxMode(taxMode);
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
        if (item.getPaymentType() == SellItem.ItemPaymentTypeE.CreditPayment && !mItems.isEmpty())
            return false;
        if (mItems.size() > 0 && mItems.get(0).getPaymentType() == SellItem.ItemPaymentTypeE.CreditPayment)
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
     * Получить платеж по типу
     *
     * @param type
     * @return
     */
    public Payment getPaymentByType(PaymentTypeE type) {
        return mPayments.get(type);
    }

    public boolean addPayment(Payment p) {
        if (mPayments.containsKey(p.getType()))
            return false;
        mPayments.put(p.getType(), p);
        return true;
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
     * Получить список платежей (копию)
     *
     * @return
     */
    public List<Payment> getPayments() {
        return new ArrayList<>(mPayments.values());
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeByte(mType.bVal);
        p.writeByte(mOrderType.bVal);
        p.writeString(mSum.toString());
        p.writeInt(mVat.bVal);
        p.writeByte(mTaxMode.bVal);
        p.writeString(mBaseDocumentNo);
        p.writeLong(mBaseDocumentDate);
        p.writeInt(mNumber);
        p.writeInt(mShiftNumber);
        mItems.writeToParcel(p, flags);
        p.writeInt(mPayments.size());
        for (Payment payment : mPayments.values())
            payment.writeToParcel(p, flags);
        mSignature.operator().writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        mType = CorrectionTypeE.fromByte(p.readByte());
        mOrderType = OrderTypeE.fromByte(p.readByte());
        mSum = new BigDecimal(p.readString());
        mVat = VatE.fromByte(p.readByte());
        mTaxMode = TaxModeE.fromByte(p.readByte());
        mBaseDocumentNo = p.readString();
        mBaseDocumentDate = p.readLong();
        mNumber = p.readInt();
        mShiftNumber = p.readInt();
        mItems.readFromParcel(p);
        int cnt = p.readInt();
        mPayments.clear();
        while (cnt-- > 0) {
            Payment payment = new Payment();
            payment.readFromParcel(p);
            mPayments.put(payment.getType(), payment);
        }
        mSignature.operator().readFromParcel(p);
    }

    /**
     * Номер смены
     *
     * @return
     */
    public int getShiftNumber() {
        return mShiftNumber;
    }

    /**
     * Номер чека
     *
     * @return
     */
    public int getNumber() {
        return mNumber;
    }

    /**
     * Номер документа-основния
     *
     * @return
     */
    public String getBaseDocumentNumber() {
        return mBaseDocumentNo;
    }

    /**
     * Указать номер документа-основания
     *
     * @param s
     */
    public void setBaseDocumentNumber(String s) {
        if (s == null) s = Const.EMPTY_STRING;
        mBaseDocumentNo = s;
    }

    /**
     * Дата документа-основания
     *
     * @return
     */
    public long getBaseDocumentDate() {
        return mBaseDocumentDate;
    }

    /**
     * Установить дату документа-основания
     *
     * @param v
     */
    public void setBaseDocumentDate(long v) {
        mBaseDocumentDate = v;
    }

    public void setBaseDocumentDate(Calendar cal) {
        mBaseDocumentDate = cal.getTimeInMillis();
    }

    public void setBaseDocumentDate(Date date) {
        mBaseDocumentDate = date.getTime();
    }

    /**
     * Тип коррекции
     *
     * @return
     */
    public CorrectionTypeE getType() {
        return mType;
    }

    /**
     * Установить тип коррекции
     *
     * @return
     */
    public void setType(CorrectionTypeE type) {
        mType = type;
    }

    /**
     * Тип чека коррекции
     *
     * @return
     */
    public OrderTypeE getOrderType() {
        return mOrderType;
    }

    /**
     * Установить тип чека
     *
     * @return
     */
    public void setOrderType(OrderTypeE type) {
        mOrderType = type;
    }

    /**
     * Получить используемую СНО
     *
     * @return
     */
    public TaxModeE getTaxMode() {
        return mTaxMode;
    }

    /**
     * Установить используемую СНО
     */
    public void setTaxMode(TaxModeE mode) {
        mTaxMode = mode;
    }

    /**
     * Получить используемую ставку НДС ФФД 1.05
     *
     * @return
     */
    public VatE getVATMode() {
        return mVat;
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
     * Установить используемую ставку НДС ФФД 1.05
     *
     * @return
     */
    public void setVATMode(VatE vat) {
        mVat = vat;
    }

    /**
     * Получить сумму коррекции ФФД 1.05
     *
     * @return
     */
    public BigDecimal getSum() {
        return mSum;
    }

    /**
     * Установить сумму коррекции ФФД 1.05
     *
     * @return
     */
    public void setSum(BigDecimal sum){
        mSum = sum;
    }

    /**
     * Значение ставки НДС
     *
     * @return
     */
    public BigDecimal getVATValue() {
        return mVat.calc(mSum);
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

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public String getClassUUID() {
        return CLASS_UUID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[][] pack() {
        add(FZ54Tag.T1009_TRANSACTION_ADDR, mLocation.getAddress());
        add(FZ54Tag.T1187_TRANSACTION_PLACE, mLocation.getPlace());
        add(FZ54Tag.T1055_USED_TAX_SYSTEM, mTaxMode.bVal);
        add(FZ54Tag.T1173_CORRECTION_TYPE, mType.bVal);
        {
            List<Pair<Integer, Tag>> tags = new ArrayList<>();
            tags.add(new Pair<>(FZ54Tag.T1178_CORRECTION_BASE_DATE, new Tag((int) (mBaseDocumentDate / 1000))));
            if (getType() == CorrectionTypeE.byArbitarity){
                tags.add(new Pair<>(FZ54Tag.T1179_CORRECTION_BASE_NO, new Tag(mBaseDocumentNo)));
            }
            put(FZ54Tag.T1174_CORRECTION_REASON_TLV, new Tag(tags));
        }
        switch (mVat) {
            case vat_20:
                add(FZ54Tag.T1102_VAT_20_SUM, getVATValue());
                break;
            case vat_20_120:
                add(FZ54Tag.T1106_VAT_20_120_SUM, getVATValue());
                break;
            case vat_10:
                add(FZ54Tag.T1103_VAT_10_SUM, getVATValue());
                break;
            case vat_10_110:
                add(FZ54Tag.T1107_VAT_10_110_SUM, getVATValue());
                break;
            case vat_none:
                add(FZ54Tag.T1105_NO_VAT_SUM, getSum());
                break;
            case vat_0:
                add(FZ54Tag.T1104_VAT_0_SUM, getSum());
                break;
        }
        BigDecimal[] payments = new BigDecimal[PaymentTypeE.values().length];
        Arrays.fill(payments, BigDecimal.ZERO);
        for (Payment payment : mPayments.values())
            payments[payment.getType().ordinal()] = payments[payment.getType().ordinal()].add(payment.getValue());
//      if(payments[PaymentTypeE.Cash.ordinal()] > 0)
        add(FZ54Tag.T1031_CASH_SUM, payments[PaymentTypeE.Cash.ordinal()]);

//      if(payments[PaymentTypeE.Card.ordinal()] > 0)
        add(FZ54Tag.T1081_CARD_SUM, payments[PaymentTypeE.Card.ordinal()]);
//      if(payments[PaymentTypeE.Prepayment.ordinal()] > 0)
        add(FZ54Tag.T1215_PREPAY_SUM, payments[PaymentTypeE.Prepayment.ordinal()]);
//      if(payments[PaymentTypeE.Credit.ordinal()] > 0)
        add(FZ54Tag.T1216_POSTPAY_SUM, payments[PaymentTypeE.Credit.ordinal()]);
//      if(payments[PaymentTypeE.Ahead.ordinal()] > 0)
        add(FZ54Tag.T1217_OTHER_SUM, payments[PaymentTypeE.Ahead.ordinal()]);
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
