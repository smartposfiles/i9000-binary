package rs.fncore.data;

import android.os.Parcel;

import java.math.BigDecimal;
import java.math.MathContext;

import rs.fncore.Const;
import rs.utils.Utils;

/**
 * Предмет расчета
 *
 * @author nick
 */
public class SellItem extends TLV implements IReableFromParcel {

    /**
     * Поля тега 1224 (для внутреннего использования)
     */
    public static final int[] TAGS_1224 = {1171, 1225};
    /**
     * Поля тега 1223 (для внутреннего использования)
     */
    public static final int[] TAGS_1223 = {1075, 1044, 1073, 1074, 1026, 1005, 1016};

    /**
     * Ставка НДС
     *
     * @author nick
     */
    public static enum VAT {
        vat_20, vat_10, vat_20_120, vat_10_110, vat_0, vat_none, vat_18, vat_18_118;

        public byte bVal() {
            if (this == VAT.vat_18)
                return (byte) (VAT.vat_20.ordinal() + 1);
            if (this == VAT.vat_18_118)
                return (byte) (VAT.vat_20_120.ordinal() + 1);
            return (byte) (ordinal() + 1);
        }

        /**
         * Расчитать значение ставки для указаной суммы
         *
         * @param sum
         * @return
         */
        public BigDecimal calc(BigDecimal sum) {
            switch (this) {
                case vat_20:
                case vat_20_120:
                    return sum.multiply(new BigDecimal(20.0)).divide(new BigDecimal(120.0), MathContext.DECIMAL128);
                case vat_10:
                case vat_10_110:
                    return sum.multiply(new BigDecimal(10.0)).divide(new BigDecimal(110.0), MathContext.DECIMAL128);
                case vat_18:
                case vat_18_118:
                    return sum.multiply(new BigDecimal(18.0)).divide(new BigDecimal(118.0), MathContext.DECIMAL128);
                default:
                    return sum;
            }
        }

    }

    /**
     * Тип предмета расчета
     *
     * @author nick
     */
    public static enum SellItemType {
        Good,
        ExcisesGood,
        Work,
        Service,
        Bet,
        Gain,
        LotteryTicket,
        LotteryGain,
        Rid,
        Payment,
        AgentComission,
        Compose,
        Misc,
        Reserved1,
        Reserved2,
        Reserved3,
        Reserved4,
        Reserved5;

        public byte bVal() {
            return (byte) (ordinal() + 1);
        }
    }

    /**
     * Тип способа оплаты
     *
     * @author nick
     */
    public static enum ItemPaymentType {
        /**
         * Предоплата 100%
         */
        Ahead_100,
        /**
         * Предоплата
         */
        Ahead,
        /**
         * Встречная
         */
        Advance,
        /**
         * Полный расчет
         */
        Full,
        /**
         * Частичный кредит
         */
        PatrialCredit,
        /**
         * Оплата кредита
         */
        CreditPayment;

        public byte bVal() {
            return (byte) (ordinal() + 1);
        }
    }

    public SellItem() {
    }

    private ItemPaymentType _paymentType = ItemPaymentType.Full;
    private SellItemType _type = SellItemType.Good;
    private String _name = Const.EMPTY_STRING;
    private String _measure = Const.EMPTY_STRING;
    private BigDecimal _qtty = BigDecimal.ONE;
    private BigDecimal _price = BigDecimal.ZERO;
    private VAT _vat = VAT.vat_none;
    private AgentData _agentData = new AgentData();

    public SellItem(SellItemType type, ItemPaymentType paymentType, String name, BigDecimal qtty, String measure,
                    BigDecimal price, VAT vat) {
        _type = type;
        _name = name;
        _qtty = qtty;
        _measure = measure;
        _price = price;
        _vat = vat;
        _paymentType = paymentType;
    }

    public SellItem(String name, BigDecimal qtty, BigDecimal price, VAT vat) {
        this(SellItemType.Good, ItemPaymentType.Full, name, qtty, (qtty.remainder(BigDecimal.ONE).compareTo(new BigDecimal(0.001)) == -1 ? "шт." : "кг."), price,
                vat);
    }

    public SellItem(String name, BigDecimal qtty, String measure, BigDecimal price, VAT vat) {
        this(SellItemType.Good, ItemPaymentType.Full, name, qtty, measure, price, vat);
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
    public ItemPaymentType getPaymentType() {
        return _paymentType;
    }

    /**
     * Получить тип предмета расчета
     *
     * @return
     */
    public SellItemType getType() {
        return _type;
    }

    /**
     * Получить наименование предмета расчета
     *
     * @return
     */
    public String getName() {
        return _name;
    }

    /**
     * Получить наименование единицы измерения
     *
     * @return
     */
    public String getMeasure() {
        return _measure;
    }

    /**
     * Получить количество
     *
     * @return
     */
    public BigDecimal getQTTY() {
        return Utils.round2(_qtty, 3);
    }

    /**
     * Получить стоимость
     *
     * @return
     */
    public BigDecimal getPrice() {
        return Utils.round2(_price, 2);
    }

    /**
     * Получить сумму (стоимость * количество)
     *
     * @return
     */
    public BigDecimal getSum() {
        return Utils.round2(_qtty.multiply(_price, MathContext.DECIMAL128), 2);
    }

    /**
     * Получить тип НДС
     *
     * @return
     */
    public VAT getVATType() {
        return _vat;
    }

    /**
     * Получить значение ставки НДС
     *
     * @return
     */
    public BigDecimal getVATValue() {
        return Utils.round2(_vat.calc(_qtty.multiply(_price, MathContext.DECIMAL128)), 2);
    }

    /**
     * Агентские данные
     *
     * @return
     */
    public AgentData getAgentData() {
        return _agentData;
    }

    /**
     * Упаковать предмет расчета в тег
     *
     * @return
     */
    public Tag pack() {
        remove(1224);
        remove(1223);
        remove(1057);
        if (_measure == null || _measure.isEmpty()) {
            if (_qtty.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 1)
                _measure = "кг";
            else
                _measure = "шт";
        }
        if (_agentData.getType() != null) {
            TLV tlv = new TLV();
            for (int id : TAGS_1223)
                tlv.put(id, _agentData.get(id));
            if (tlv.size() > 0)
                put(1223, new Tag(tlv));
            for (int id : TAGS_1224)
                tlv.put(id, _agentData.get(id));
            if (tlv.size() > 0)
                put(1224, new Tag(tlv));
        }
        put(1214, new Tag(_paymentType.bVal()));
        put(1212, new Tag(_type.bVal()));
        put(1030, new Tag(_name));
        put(1197, new Tag(_measure));
        put(1079, new Tag(_price, 2));
        put(1023, new Tag(_qtty, 3));
        put(1199, new Tag(_vat.bVal()));
        put(1043, new Tag(getSum(), 2));
        if (_vat != VAT.vat_none && _vat != VAT.vat_0)
            put(1200, new Tag(getVATValue(), 2));
        return new Tag(this);
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(_type.ordinal());
        p.writeInt(_paymentType.ordinal());
        p.writeInt(_vat.ordinal());
        p.writeString(_name);
        p.writeString(_measure);
        p.writeString(_qtty.toString());
        p.writeString(_price.toString());
        p.writeInt(size());
        for (int i = 0; i < size(); i++) {
            p.writeInt(keyAt(i));
            valueAt(i).writeToParcel(p);
        }
        _agentData.writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        _type = SellItemType.values()[p.readInt()];
        _paymentType = ItemPaymentType.values()[p.readInt()];
        _vat = VAT.values()[p.readInt()];
        _name = p.readString();
        _measure = p.readString();
        _qtty = new BigDecimal(p.readString());
        _price = new BigDecimal(p.readString());
        clear();
        int count = p.readInt();
        while (count-- > 0)
            put(p.readInt(), new Tag(p));
        _agentData.readFromParcel(p);
    }

}
