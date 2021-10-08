package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

/**
 * Оплата
 *
 * @author nick
 */
public class Payment implements IReableFromParcel {

    /**
     * Способы оплаты
     *
     * @author nick
     */
    public enum PaymentTypeE {
        /**
         * Наличные
         */
        Cash("Наличными"),
        /**
         * Безналичные
         */
        Card("Безналичными"),
        /**
         * Предоплата
         */
        Prepayment("Предварительная оплата"),
        /**
         * Кредит
         */
        Credit("Кредит, последующая оплата"),
        /**
         * Встречная
         */
        Ahead("Встречная");


        public final String pName;

        private PaymentTypeE(String name) {
            this.pName = name;
        }

        /**
         * Получить список наименований
         * @return
         */
        public static String [] getNames(){
            String [] res = new String[values().length];
            for (PaymentTypeE val:values()){
                res[val.ordinal()]=val.pName;
            }
            return res;
        }
    }

    private PaymentTypeE mType = PaymentTypeE.Cash;
    private BigDecimal mValue = BigDecimal.ZERO;

    public Payment() {
    }

    /**
     * @param type  - тип способа оплаты
     * @param value - сумма
     */
    public Payment(PaymentTypeE type, BigDecimal value) {
        mType = type;
        mValue = value;
    }

    /**
     * Оплата наличными
     *
     * @param value сумма
     */
    public Payment(BigDecimal value) {
        this(PaymentTypeE.Cash, value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Получить тип оплаты
     *
     * @return
     */
    public PaymentTypeE getType() {
        return mType;
    }

    /**
     * Получить сумму оплаты
     *
     * @return
     */
    public BigDecimal getValue() {
        return mValue;
    }

    /**
     * Изменить сумму оплаты
     *
     * @param val
     */
    public void setValue(BigDecimal val) {
        if (val.compareTo(BigDecimal.ZERO) >= 0)
            mValue = val;
    }

    @Override
    public void writeToParcel(Parcel p, int arg1) {
        p.writeInt(mType.ordinal());
        p.writeString(mValue.toString());
    }

    @Override
    public void readFromParcel(Parcel p) {
        mType = PaymentTypeE.values()[p.readInt()];
        mValue = new BigDecimal(p.readString());

    }

    public static final Creator<Payment> CREATOR = new Creator<Payment>() {

        @Override
        public Payment createFromParcel(Parcel p) {
            Payment result = new Payment();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };

}
