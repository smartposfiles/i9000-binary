package rs.fncore.data;

import android.os.Parcel;
import java.math.BigDecimal;
import java.math.MathContext;
import java.security.InvalidParameterException;
import java.util.UUID;

import rs.fncore.Const;
import rs.fncore.FZ54Tag;
import rs.utils.Utils;

/**
 * Предмет расчета
 *
 * @author nick
 */
public class SellItem extends TLV implements IReableFromParcel {

    /**
     * Поля тега 1223 (для внутреннего использования)
     */
    public static final int[] TAGS_1223 = {
        FZ54Tag.T1005_TRANSFER_OPERATOR_ADDR,
        FZ54Tag.T1016_TRANSFER_OPERATOR_INN,
        FZ54Tag.T1026_TRANSFER_OPERATOR_NAME,
        FZ54Tag.T1044_TRANSFER_OPERATOR_ACTION,
        FZ54Tag.T1073_PAYMENT_AGENT_PHONE,
        FZ54Tag.T1074_PAYMENT_OPERATOR_PHONE,
        FZ54Tag.T1075_TRANSFER_OPERATOR_PHONE,
    };

    /**
     * Поля тега 1224 (для внутреннего использования)
     */
    public static final int[] TAGS_1224 = {
        FZ54Tag.T1171_SUPPLIER_PHONE,
        FZ54Tag.T1225_SUPPLIER_NAME,
        FZ54Tag.T1226_SUPPLIER_INN

    };

    /**
     * Тип предмета расчета
     *
     * @author amv
     */
    public enum SellItemTypeE {
        Good(1, "ТОВАР", "Т"),
        ExcisesGood(2, "ПОДАКЦИЗНЫЙ ТОВАР","АТ"),
        Work(3, "РАБОТА","Р"),
        Service(4, "УСЛУГА", "У"),
        Bet(5, "СТАВКА ИГРЫ", "СА"),
        Gain(6, "ВЫИГРЫШ АИ", "ВА"),
        LotteryTicket(7, "ЛОТЕРЕЙНЫЙ БИЛЕТ","СЛ"),
        LotteryGain(8,"ВЫИГРЫШ ЛОТЕРЕИ","ВЛ"),
        Rid(9, "ПРЕДОСТАВЛЕНИЕ РИД", "РИД"),
        Payment(10, "ПЛАТЕЖ", "В"),
        AgentComission(11, "АГЕНТСКОЕ ВОЗНАГРАЖДЕНИЕ","АВ"),
        Compose(12, "ВЫПЛАТА", "В"),
        Misc(13,"ИНОЙ ПРЕДМЕТ РАСЧЕТА","ИПР"),
        Property(14, "ИМУЩЕСТВЕННОЕ ПРАВО", ""),
        NonSales(15,"ВНЕРЕАЛИЗАЦИОННЫЙ ДОХОД",""),
        AnotherPayments(16, "ИНЫЕ ПЛАТЕЖИ И ВЗНОСЫ",""),
        TradeFee(17, "ТОРГОВЫЙ СБОР",""),
        ResortFee(18, "КУРОРТНЫЙ СБОР",""),
        Pledge(19,"ЗАЛОГ", "" ),
        ProductionCosts(20, "РАСХОД",""),
        CompulsoryPensionInsuranceIndivilual(21, "ВЗНОСЫ НА ОБЯЗАТЕЛЬНОЕ ПЕНСИОННОЕ СТРАХОВАНИЕ ИП",
                "ВЗНОСЫ НА ОПС ИП"),
        CompulsoryPensionInsurance(22, "ВЗНОСЫ НА ОБЯЗАТЕЛЬНОЕ ПЕНСИОННОЕ СТРАХОВАНИЕ",
                "ВЗНОСЫ НА ОПС"),
        CompulsoryMedicalInsuranceIndivilual(23, "ВЗНОСЫ НА ОБЯЗАТЕЛЬНОЕ МЕДИЦИНСКОЕ СТРАХОВАНИЕ ИП",
                "ВЗНОСЫ НА ОМС ИП"),
        CompulsoryMedicalInsurance(24, "ВЗНОСЫ НА ОБЯЗАТЕЛЬНОЕ МЕДИЦИНСКОЕ СТРАХОВАНИЕ",
                "ВЗНОСЫ НА ОМС"),
        CompulsorySocialInsurance(25, "ВЗНОСЫ НА ОБЯЗАТЕЛЬНОЕ СОЦИАЛЬНОЕ СТРАХОВАНИЕ",
                "ВЗНОСЫ НА ОСС"),
        CasinoPayment(26, "ПЛАТЕЖ КАЗИНО", "ПК"),
        FundsDistribution(27, "ВЫДАЧА ДЕНЕЖНЫХ СРЕДСТВ","ВЫДАЧА ДС"),
        ExcisableMarkGoodsNoMark(30, "АТНМ","АТНМ"),
        ExcisableMarkGoodsMark(31, "АТМ","АТМ"),
        MarkGoodsNoMark(32, "ТНМ","ТНМ"),
        MarkGoodsMark(33, "ТМ","ТМ"),
        ;

        public final byte bVal;
        public final String pName;
        public final String shortName;

        private SellItemTypeE(int value, String name, String shortName) {
            this.bVal = (byte)value;
            this.pName = name;
            this.shortName=shortName;
        }

        public static SellItemTypeE fromByte(byte number){
            for (SellItemTypeE val:values()){
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
            for (SellItemTypeE val:values()){
                res[val.ordinal()]=val.pName;
            }
            return res;
        }
    }

    /**
     * Тип способа оплаты
     *
     * @author amv
     */
    public enum ItemPaymentTypeE {
        /**
         * Предоплата 100%
         */
        Ahead_100(1, "Предоплата 100%"),
        /**
         * Предоплата
         */
        Ahead(2, "Предоплата"),
        /**
         * Встречная
         */
        Advance(3, "Аванс"),
        /**
         * Полный расчет
         */
        Full(4, "Полный расчет"),
        /**
         * Частичный кредит
         */
        PatrialCredit(5, "Частичный расчет и кредит"),
        /**
         * Передача в кредит
         */
        CreditTransfer(6, "Передача в кредит"),
        /**
         * Оплата кредита
         */
        CreditPayment(7, "Оплата кредита")
        ;

        public final byte bVal;
        public final String pName;

        private ItemPaymentTypeE(int value, String name) {
            this.bVal = (byte)value;
            this.pName = name;
        }

        public static ItemPaymentTypeE fromByte(byte number){
            for (ItemPaymentTypeE val:values()){
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
            for (ItemPaymentTypeE val:values()){
                res[val.ordinal()]=val.pName;
            }
            return res;
        }
    }

    public SellItem() {
    }

    private ItemPaymentTypeE mItemPaymentType = ItemPaymentTypeE.Full;
    private SellItemTypeE mType = SellItemTypeE.Good;
    private String mName = Const.EMPTY_STRING;
    protected MeasureTypeE mMeasure = MeasureTypeE.Piece;
    private BigDecimal mQtty = BigDecimal.ONE;
    private BigDecimal mPrice = BigDecimal.ZERO;
    private VatE mVat = VatE.vat_none;
    private volatile AgentData mAgentData = new AgentData();
    protected volatile MarkingCode mMarkingCode = new MarkingCode();
    public String mUUID = UUID.randomUUID().toString();

    public SellItem(SellItemTypeE type, ItemPaymentTypeE paymentType, String name, BigDecimal qtty, MeasureTypeE measure,
                    BigDecimal price, VatE vat) {
        mType = type;
        mName = name;
        mQtty = qtty;
        mMeasure = measure;
        mPrice = price;
        mVat = vat;
        mItemPaymentType = paymentType;
    }

    public SellItem(String name, BigDecimal qtty, BigDecimal price, VatE vat) {
        this(SellItemTypeE.Good, ItemPaymentTypeE.Full, name, qtty,
                (qtty.remainder(BigDecimal.ONE).compareTo(new BigDecimal(0.001)) == -1 ?
                        MeasureTypeE.Piece : MeasureTypeE.Kilogram),
                price, vat);
    }

    public SellItem(String name, BigDecimal qtty, MeasureTypeE measure, BigDecimal price, VatE vat) {
        this(SellItemTypeE.Good, ItemPaymentTypeE.Full, name, qtty, measure, price, vat);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Установить код маркировки
     *
     * @return
     */
    public void setMarkingCode(MarkingCode code) {
        mMarkingCode =code;
    }

    /**
     * Установить код маркировки
     *
     * @return
     */
    public void setMarkingCode(String codeStr, SellOrder.OrderTypeE type) {
        MarkingCode.PlannedItemStatusE itemStatus;
        switch (type){
            case Outcome:
            case ReturnOutcome:
                itemStatus =  MarkingCode.PlannedItemStatusE.ItemNotChanged;
                break;
            case Income:
                if (mMeasure == MeasureTypeE.Piece){
                    itemStatus =  MarkingCode.PlannedItemStatusE.PieceItemSell;
                }
                else{
                    itemStatus =  MarkingCode.PlannedItemStatusE.MeasuredItemSell;
                }
                break;
            case ReturnIncome:
                if (mMeasure == MeasureTypeE.Piece){
                    itemStatus =  MarkingCode.PlannedItemStatusE.PieceItemReturned;
                }
                else{
                    itemStatus =  MarkingCode.PlannedItemStatusE.MeasuredItemReturned;
                }
                break;
            default : throw new InvalidParameterException("unknown sell order type");
        }
        setMarkingCode(new MarkingCode(codeStr,itemStatus));
    }

    /**
     * Получить код маркировки
     *
     * @return
     */
    public MarkingCode getMarkingCode() {
        return mMarkingCode;
    }

    /**
     * Установить дробное количество маркированного товара
     *
     * @return
     */
    public void setMarkingFractionalAmount(int itemsNumber, int intemsInPackage) throws InvalidParameterException{
        if (mMeasure != MeasureTypeE.Piece || !getMarkingCode().isEmpty()) {
            throw new InvalidParameterException("wrong measure type or marking not applied");
        }

        TLV tlv = new TLV();

        tlv.put(FZ54Tag.T1293_FRACTIONAL_MARKING_ITEM_NUMERATOR, new Tag(itemsNumber));
        tlv.put(FZ54Tag.T1294_FRACTIONAL_MARKING_ITEM_DENOMERATOR,
                new Tag(intemsInPackage));
        tlv.put(FZ54Tag.T1292_FRACTIONAL_MARKING_ITEM_FRACT,
                new Tag("" + itemsNumber + "/" + intemsInPackage));

        put(FZ54Tag.T1291_MARKING_FRACTIONAL_ITEM, new Tag(tlv));
        put(FZ54Tag.T1023_QUANTITY, new Tag(BigDecimal.ONE, 3));
    }

    /**
     * Установить ИНН клиента
     *
     * @return
     */
    public void setClientINN(String inn) {
        add(FZ54Tag.T1228_CLIENT_INN, inn);
    }

    /**
     * Получить ИНН клиента
     *
     * @return
     */
    public String getClientINN(String inn) {
        return get(FZ54Tag.T1228_CLIENT_INN).asString();
    }

    /**
     * Получить тип оплаты
     *
     * @return
     */
    public ItemPaymentTypeE getPaymentType() {
        return mItemPaymentType;
    }

    /**
     * Получить тип предмета расчета
     *
     * @return
     */
    public SellItemTypeE getType() {
        return mType;
    }

    /**
     * Получить наименование предмета расчета
     *
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * Получить UUID генерирующийся при создании класса
     *
     * @return
     */
    public String getUUID() {
        return mUUID;
    }

    /**
     * Получить наименование единицы измерения
     *
     * @return
     */
    public MeasureTypeE getMeasure() {
        return mMeasure;
    }

    /**
     * Получить количество
     *
     * @return
     */
    public BigDecimal getQTTY() {
        return Utils.round2(mQtty, 3);
    }

    /**
     * Получить стоимость
     *
     * @return
     */
    public BigDecimal getPrice() {
        return Utils.round2(mPrice, 2);
    }

    /**
     * Получить сумму (стоимость * количество)
     *
     * @return
     */
    public BigDecimal getSum() {
        return Utils.round2(mQtty.multiply(mPrice, MathContext.DECIMAL128), 2);
    }

    /**
     * Получить тип НДС
     *
     * @return
     */
    public VatE getVATType() {
        return mVat;
    }

    /**
     * Получить значение ставки НДС
     *
     * @return
     */
    public BigDecimal getVATValue() {
        return Utils.round2(mVat.calc(mQtty.multiply(mPrice, MathContext.DECIMAL128)), 2);
    }

    /**
     * Агентские данные
     *
     * @return
     */
    public AgentData getAgentData() {
        return mAgentData;
    }

    /**
     * Упаковать предмет расчета в тег
     *
     * @return
     */
    public Tag pack() {
        remove(FZ54Tag.T1224_SUPPLIER_DATA_TLV);
        remove(FZ54Tag.T1223_AGENT_DATA_TLV);
        remove(FZ54Tag.T1057_AGENT_FLAG);

        if (mMeasure == null) {
            if (mQtty.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 1)
                mMeasure = MeasureTypeE.Kilogram;
            else
                mMeasure = MeasureTypeE.Piece;
        }
        if (mAgentData.getType() != null) {
            TLV tlv = new TLV();
            for (int id : TAGS_1223)
                tlv.put(id, mAgentData.get(id));
            if (tlv.size() > 0)
                put(FZ54Tag.T1223_AGENT_DATA_TLV, new Tag(tlv));
            for (int id : TAGS_1224)
                tlv.put(id, mAgentData.get(id));
            if (tlv.size() > 0)
                put(FZ54Tag.T1224_SUPPLIER_DATA_TLV, new Tag(tlv));
        }
        put(FZ54Tag.T1214_PAYMENT_TYPE, new Tag(mItemPaymentType.bVal));
        put(FZ54Tag.T1212_ITEM_TYPE, new Tag(mType.bVal));
        if (mItemPaymentType != ItemPaymentTypeE.Advance) {
            put(FZ54Tag.T1030_SUBJECT, new Tag(mName));
        }
        else{
            remove(FZ54Tag.T1030_SUBJECT);
        }
        put(FZ54Tag.T1079_ONE_ITEM_PRICE, new Tag(mPrice, 2));
        put(FZ54Tag.T1023_QUANTITY, new Tag(mQtty, 3));
        put(FZ54Tag.T1199_VAT_ID, new Tag(mVat.bVal));
        put(FZ54Tag.T1043_ITEM_PRICE, new Tag(getSum(), 2));
        if (mVat != VatE.vat_none && mVat != VatE.vat_0)
            put(FZ54Tag.T1200_ITEM_VAT, new Tag(getVATValue(), 2));
        return new Tag(this);
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeByte(mType.bVal);
        p.writeByte(mItemPaymentType.bVal);
        p.writeByte(mVat.bVal);
        p.writeString(mName);
        p.writeByte(mMeasure.bVal);
        p.writeString(mQtty.toString());
        p.writeString(mPrice.toString());
        p.writeString(mUUID);
        p.writeInt(size());
        for (int i = 0; i < size(); i++) {
            p.writeInt(keyAt(i));
            valueAt(i).writeToParcel(p);
        }
        mAgentData.writeToParcel(p, flags);
        mMarkingCode.writeToParcel(p,flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        mType = SellItemTypeE.fromByte(p.readByte());
        mItemPaymentType = ItemPaymentTypeE.fromByte(p.readByte());
        mVat = VatE.fromByte(p.readByte());
        mName = p.readString();
        mMeasure = MeasureTypeE.fromByte(p.readByte());
        mQtty = new BigDecimal(p.readString());
        mPrice = new BigDecimal(p.readString());
        mUUID=p.readString();
        clear();
        int count = p.readInt();
        while (count-- > 0)
            put(p.readInt(), new Tag(p));
        mAgentData.readFromParcel(p);
        mMarkingCode.readFromParcel(p);
    }

    public static final Creator<SellItem> CREATOR = new Creator<SellItem>() {
        @Override
        public SellItem createFromParcel(Parcel p) {
            SellItem result = new SellItem();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public SellItem[] newArray(int size) {
            return new SellItem[size];
        }
    };
}
