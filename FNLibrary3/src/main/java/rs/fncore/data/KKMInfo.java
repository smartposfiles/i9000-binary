package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import rs.fncore.Const;
import rs.fncore.FZ54Tag;

/**
 * Отчет о регистрации (состоянии) ККТ
 *
 * @author nick
 */
public class KKMInfo extends Document {

    public final static String CLASS_NAME="KKMInfo";
    public static final String KKT_VERSION = "003";
    public static final String FNS_URL = "www.nalog.ru";

    public final static String CLASS_UUID="1f1d43e4-ebae-11eb-9a03-0242ac130003";

    /**
     * Битовые флаги состояния ФН
     */
    public enum FNStateE{
        UNKNOWN(0x0),
        STAGE1(0x1),
        STAGE2(0x3),
        STAGE3(0x7),
        STAGE4(0xF)
        ;

        public final byte bVal;

        private FNStateE(int val) {
            this.bVal = (byte)val;
        }

        public static FNStateE fromByte(byte state){
            for (FNStateE val:values()){
                if (val.bVal == state){
                    return val;
                }
            }
            return UNKNOWN;
        }

        /**
         * Битовые флаги состояния ФН
         */
        public static final int FN_STATE_READY_BF = 0;
        public static final int FN_STATE_ACTIVE_BF = 1;
        public static final int FN_STATE_ARCHIVED_BF = 2;
        public static final int FN_STATE_NO_MORE_DATA = 3;

        public boolean isActive(){
            return (bVal & (0x01 << FN_STATE_ACTIVE_BF))!=0;
        }

        public boolean isArchived(){
            return (bVal & (0x01 << FN_STATE_ARCHIVED_BF))!=0;
        }
    }

    public enum WorkModeE{
        UNKNOWN(0),
        ENCRYPTION(1<<0),
        OFFLINE (1<<1),
        AUTO (1<<2),
        SERVICE  (1<<3),
        BSO (1<<4),
        INTERNET (1<<5)
        ;

        public final byte bVal;

        private WorkModeE(int val) {
            this.bVal = (byte)val;
        }

        /**
         * Декодировать битовую маску в список
         *
         * @param val
         * @return
         */
        public static Set<WorkModeE> fromByteArray(byte val) {
            HashSet<WorkModeE> result = new HashSet<>();
            for (WorkModeE a : values()) {
                if (a == UNKNOWN) continue;
                if ((val & a.bVal) == a.bVal)
                    result.add(a);
            }
            return result;
        }

        /**
         * Закодировать список в битовую маску
         *
         * @param val
         * @return
         */
        public static byte toByteArray(Iterable<WorkModeE> val) {
            byte result = 0;
            for (WorkModeE a : val)
                result |= a.bVal;
            return result;
        }
    }

    public enum WorkModeExE {
        UNKNOWN(0),
        EXCISABLE_GOODS(1<<0),
        GAMBLING_GAMES (1<<1),
        LOTTERY (1<<2),
        AUTO_PRINTER(1<<3),
        MARKING_GOODS(1<<4),
        PAWNSHOP_ACTIVITY (1<<5),
        INSURANCE_ACTIVITY (1<<6)
        ;

        public final byte bVal;

        private WorkModeExE(int val) {
            this.bVal = (byte)val;
        }

        /**
         * Декодировать битовую маску в список
         *
         * @param val
         * @return
         */
        public static Set<WorkModeExE> fromByteArray(byte val) {
            HashSet<WorkModeExE> result = new HashSet<>();
            for (WorkModeExE a : values()) {
                if (a == UNKNOWN) continue;
                if ((val & a.bVal) == a.bVal)
                    result.add(a);
            }
            return result;
        }

        /**
         * Закодировать список в битовую маску
         *
         * @param val
         * @return
         */
        public static byte toByteArray(Iterable<WorkModeExE> val) {
            byte result = 0;
            for (WorkModeExE a : val)
                result |= a.bVal;
            return result;
        }
    }

    public static class FnWarnings {
        private static final int MASK_REPLACE_FN_REMAIN_3_DAYS=(1<<0);
        private static final int MASK_FN_REMAIN_30_DAYS =(1<<1);
        private static final int MASK_FN_MEMORY_FILL_99=(1<<2);
        private static final int MASK_OFD_TIMEOUT_OVERLOAD=(1<<3);
        private static final int MASK_FAILURE_FORMAT=(1<<4);
        private static final int MASK_NEED_KKT_SETUP=(1<<5);
        private static final int MASK_OFD_CANCELED=(1<<6);
        private static final int MASK_CRITICAL_FN_ERROR=(1<<7);

        public final int iVal;

        public FnWarnings(byte val) {
            this.iVal = val&0xFF;
        }

        public FnWarnings(int val) {
            this.iVal = val;
        }

        public boolean isReplaceUrgent3Days(){
            return (iVal &MASK_REPLACE_FN_REMAIN_3_DAYS)==MASK_REPLACE_FN_REMAIN_3_DAYS;
        }

        public boolean isReplace30Days(){
            return (iVal & MASK_FN_REMAIN_30_DAYS)== MASK_FN_REMAIN_30_DAYS;
        }

        public boolean isMemoryFull99(){
            return (iVal &MASK_FN_MEMORY_FILL_99)==MASK_FN_MEMORY_FILL_99;
        }

        public boolean isOFDTimeout(){
            return (iVal &MASK_OFD_TIMEOUT_OVERLOAD)==MASK_OFD_TIMEOUT_OVERLOAD;
        }

        public boolean isFailureFormat(){
            return (iVal &MASK_FAILURE_FORMAT)==MASK_FAILURE_FORMAT;
        }

        public boolean isNeedKKTSetup(){
            return (iVal &MASK_NEED_KKT_SETUP)==MASK_NEED_KKT_SETUP;
        }

        public boolean isOFDCanceled(){
            return (iVal &MASK_OFD_CANCELED)==MASK_OFD_CANCELED;
        }

        public boolean isFNCriticalError(){
            return (iVal &MASK_CRITICAL_FN_ERROR)==MASK_CRITICAL_FN_ERROR;
        }
    }

    /**
     * Причина регистрации
     */
    public enum FiscalReasonE implements Parcelable{
        UNKNOWN(-1,"неизвестно", "неизвестно"),
        /**
         * Регистрация ФН
         */
        REGISTER(0, "ОТЧЕТ О РЕГ.", "Регистрация ККТ"),
        /**
         * Замена ФН
         */
        REPLACE_FN(1, "ИЗМ. СВЕД. О ККТ","Замена ФН"),
        /**
         * Замена ОФД
         */
        CHANGE_OFD(2, "ИЗМ. СВЕД. О ККТ", "Замена ОФД"),
        /**
         * Изменение реквизитов
         */
        CHANGE_SETTINGS(3, "ИЗМ. СВЕД. О ККТ", "Изменение реквизитов пользователя"),
        /**
         * Изменение настроек ККТ
         */
        CHANGE_KKT_SETTINGS(4, "ИЗМ. СВЕД. О ККТ", "Изменение настроек ККТ"),
        /**
         * Изменение ИНН
         */
        CHANGE_INN(5, "ИЗМ. СВЕД. О ККТ", "Изменение ИНН"),

        /**
         * Другие изменения
         */
        CHANGE_OTHERS(6, "ИЗМ. СВЕД. О ККТ", "Другие изменения")

        ;

        public final byte bVal;
        public final String pName;
        public final String desc;

        private FiscalReasonE(int val, String pName, String desc) {
            this.bVal = (byte)val;
            this.pName=pName;
            this.desc=desc;
        }

        public static FiscalReasonE fromByte(byte reason){
            for (FiscalReasonE val:values()){
                if (val.bVal == reason){
                    return val;
                }
            }
            return UNKNOWN;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel p, int flags) {
            p.writeByte(bVal);
        }

        public static final Parcelable.Creator<FiscalReasonE> CREATOR = new Parcelable.Creator<FiscalReasonE>() {
            @Override
            public FiscalReasonE createFromParcel(Parcel p) {
                return fromByte(p.readByte());
            }

            @Override
            public FiscalReasonE[] newArray(int size) {
                return new FiscalReasonE[size];
            }
        };
    }

    /**
     * Версия ФФД
     *
     * @author amv
     */
    public enum FFDVersionE {
        unknown(0, "unk"),
        ver10(1, "1.0"),
        ver105(2,"1.05"),
        ver11(3, "1.1"),
        ver12(4,"1.2"),
        ;

        FFDVersionE(int bVal, String name) {
            this.bVal = (byte)bVal;
            this.name=name;
        }

        public final String name;
        public final byte bVal;

        public static FFDVersionE fromByte(byte bVal) {
            for (FFDVersionE v : values())
                if (v.bVal == bVal) return v;
            return unknown;
        }

        public boolean is12_OrMore(){
            return bVal>=ver12.bVal;
        }

    }

    /**
     * Тип физического подключения ФН
     *
     * @author amv
     */
    public enum FNConnectionModeE implements Parcelable{
        UNKNOWN,
        USB,
        UART,
        VIRTUAL,
        CLOUD;

        public static final Creator<FNConnectionModeE> CREATOR = new Creator<FNConnectionModeE>() {
            @Override
            public FNConnectionModeE createFromParcel(Parcel in) {
                return FNConnectionModeE.fromInt(in.readInt());
            }

            @Override
            public FNConnectionModeE[] newArray(int size) {
                return new FNConnectionModeE[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(toInt());
        }

        public int toInt() { return this.ordinal(); }
        public static FNConnectionModeE fromInt(int value) {
            return values()[value];
        }
    }

    /**
     * Информация о последнем проведенном документе
     *
     * @author nick
     */
    public class LastDocumentInfo {
        private long mNumber;
        private Calendar mDate = Calendar.getInstance();

        private LastDocumentInfo() {
            mDate.setTimeInMillis(0);
        }

        /**
         * Номер документа
         *
         * @return
         */
        public long getNumber() {
            return mNumber;
        }

        /**
         * Дата документа
         *
         * @return
         */
        public long getTimeInMillis() {
            return mDate.getTimeInMillis();
        }
    }

    public static final String KKM_NUMBER_TAG = "KKMNo";
    public static final String OWNER_TAG = "Owner";
    public static final String OFD_TAG = "OFD";
    public static final String MODES_TAG = "Mode";
    public static final String MODES_EX_TAG = "ModeEx";
    public static final String TAX_MODES_TAG = "TaxModes";
    public static final String AGENT_MODES_TAG = "AgentModes";
    public static final String SUPP_FFD_VER_TAG = "SuppFFDVer";

    protected String mKkmNumber ="";
    protected String mFnNumber ="";
    protected FNStateE mFnState = FNStateE.UNKNOWN;
    protected int mUnfinishedDocType;
    protected Shift mShift = new Shift();
    protected FnWarnings mFnWarnings = new FnWarnings((byte)0);
    protected String mServiceVersion = Const.EMPTY_STRING;
    protected LastDocumentInfo mLastDocument = new LastDocumentInfo();
    protected OU mOwner = new OU();
    protected OU mOfd = new OU();
    protected FFDVersionE mSupppFFDVer = FFDVersionE.unknown;

    protected Set<TaxModeE> mTaxModes = new HashSet<>();
    protected Set<AgentTypeE> mAgentTypes = new HashSet<>();
    protected Set<WorkModeE> mWorkModes = new HashSet<>();
    protected Set<WorkModeExE> mWorkModesEx = new HashSet<>();

    protected FiscalReasonE mRegistrationReason = FiscalReasonE.UNKNOWN;
    protected long mReRegistrationReason = 0;
    protected FNConnectionModeE mConnectionMode = FNConnectionModeE.UNKNOWN;

    public KKMInfo() {
        super();
        add(FZ54Tag.T1193_GAMBLING_FLAG, false);
        add(FZ54Tag.T1126_LOTTERY_FLAG, false);
        add(FZ54Tag.T1221_AUTOMAT_FLAG, false);
        add(FZ54Tag.T1207_EXCISE_GOODS_FLAG, false);
        add(FZ54Tag.T1013_KKT_SERIAL_NO, Const.EMPTY_STRING);
        add(FZ54Tag.T1209_FFD_VERSION, FFDVersionE.unknown.bVal);
        add(FZ54Tag.T1189_KKT_FFD_VERSION, FFDVersionE.unknown.bVal);
        add(FZ54Tag.T1188_KKT_VERSION, KKT_VERSION);
        add(FZ54Tag.T1117_SENDER_EMAIL, Const.EMPTY_STRING);
        add(FZ54Tag.T1057_AGENT_FLAG, (byte) 0);
        add(FZ54Tag.T1060_FNS_URL, FNS_URL);
    }


    public KKMInfo(JSONObject json) throws JSONException {
        super(json);
        mKkmNumber = json.getString(KKM_NUMBER_TAG);
        if (json.has(OWNER_TAG))
            mOwner = new OU(json.getJSONObject(OWNER_TAG));
        if (json.has(OFD_TAG))
            mOfd = new OU(json.getJSONObject(OFD_TAG));
        mWorkModes = WorkModeE.fromByteArray((byte) json.getInt(MODES_TAG));
        mWorkModesEx = WorkModeExE.fromByteArray((byte) json.getInt(MODES_EX_TAG));
        mAgentTypes = AgentTypeE.fromByteArray((byte) json.getInt(AGENT_MODES_TAG));
        mTaxModes = TaxModeE.fromByteArray((byte) json.getInt(TAX_MODES_TAG));
        mSupppFFDVer = FFDVersionE.fromByte((byte) json.getInt(SUPP_FFD_VER_TAG));
    }

    /**
     * Получить заводской номер ККТ
     *
     * @return
     */
    public String getKKMSerial() {
        return get(FZ54Tag.T1013_KKT_SERIAL_NO).asString();
    }

    /**
     * Получить регистрационный номер ККТ
     *
     * @return
     */
    public String getKKMNumber() {
        return mKkmNumber;
    }

    /**
     * Установить  регистрационный номер ККТ
     *
     * @param v
     */
    public void setKKMNumber(String v) {
        mKkmNumber = v;
    }

    /**
     * Получить номер фискального накопителя
     *
     * @return
     */
    public String getFNNumber() {
        return mFnNumber;
    }

    /**
     * Получить текущую или последнюю открытую смену
     *
     * @return shift
     */
    public Shift getShift() {
        return mShift;
    }

    /**
     * Владелец ККТ
     *
     * @return
     */
    public OU getOwner() {
        return mOwner;
    }

    /**
     * Информация о предупреждениях ФН
     *
     * @return
     */
    public FnWarnings getFNWarings() {
        return mFnWarnings;
    }

    /**
     * Получить максимальную поддерживаемую версию протокола ФФД
     *
     * @return
     */
    public FFDVersionE getMaxSuppFFDVersion() {
        return mSupppFFDVer;
    }

    /**
     * Получить текущую версию протокола ФФД
     *
     * @return
     */
    public FFDVersionE getFFDProtocolVersion() {
        byte bVal;
        if (hasTag(FZ54Tag.T1209_FFD_VERSION))
            bVal = get(FZ54Tag.T1209_FFD_VERSION).asByte();
        else if (hasTag(FZ54Tag.T1189_KKT_FFD_VERSION))
            bVal = get(FZ54Tag.T1189_KKT_FFD_VERSION).asByte();
        else bVal = FFDVersionE.unknown.bVal;

        return FFDVersionE.fromByte(bVal);
    }

    /**
     * Установить версию протокола ФФД
     *
     * @param ver
     */
    public void setFFDProtocolVersion(FFDVersionE ver) {
        add(FZ54Tag.T1209_FFD_VERSION, ver.bVal);
        add(FZ54Tag.T1189_KKT_FFD_VERSION, ver.bVal);
    }

    public FNConnectionModeE getConnectionMode() {
        return mConnectionMode;
    }

    public void setConnectionMode(FNConnectionModeE mode) {
        mConnectionMode = mode;
    }

    /**
     * ФН подключен к ККТ
     *
     * @return
     */
    public boolean isFNPresent() {
        return mFnState != FNStateE.UNKNOWN;
    }

    /**
     * ФН находится в фискальном режиме
     *
     * @return
     */
    public boolean isFNActive() {
        return mFnState.isActive();
    }

    /**
     * ФН находится в постфискальном режиме
     */
    public boolean isFNArchived() {
        return mFnState.isArchived();
    }

    /**
     * Получить признак "автономная работа"
     *
     * @return
     */
    public boolean isOfflineMode() {
        return mWorkModes.contains(WorkModeE.OFFLINE);
    }

    /**
     * Установить признак "автономная работа"
     *
     * @param val
     */
    public void setOfflineMode(boolean val) {
        if (val)
            mWorkModes.add(WorkModeE.OFFLINE);
        else
            mWorkModes.remove(WorkModeE.OFFLINE);
    }

    /**
     * Получить признак "Продажа подакцизного товара"
     *
     * @return
     */
    public boolean isExcisesMode() {
        return mWorkModesEx.contains(WorkModeExE.EXCISABLE_GOODS);
    }

    /**
     * Установить признак "Продажа подакцизного товара"
     *
     * @param val
     */
    public void setExcisesMode(boolean val) {
        if (val) {
            mWorkModesEx.add(WorkModeExE.EXCISABLE_GOODS);
        } else {
            mWorkModesEx.remove(WorkModeExE.EXCISABLE_GOODS);
        }

        add(FZ54Tag.T1207_EXCISE_GOODS_FLAG, val);
    }

    /**
     * Получить признак "Проведение лотереи"
     *
     * @return
     */
    public boolean isLotteryMode() {
        return mWorkModesEx.contains(WorkModeExE.LOTTERY);
    }

    /**
     * Установить признак "Проведение лотереи"
     *
     * @param val
     */
    public void setLotteryMode(boolean val) {
        if (val) {
            mWorkModesEx.add(WorkModeExE.LOTTERY);
        } else {
            mWorkModesEx.remove(WorkModeExE.LOTTERY);
        }
        add(FZ54Tag.T1126_LOTTERY_FLAG, val);
    }

    /**
     * Получить признак "Установка принтера в автомате"
     *
     * @return
     */
    public boolean isAutoPrinter() {
        return mWorkModesEx.contains(WorkModeExE.AUTO_PRINTER);
    }

    /**
     * Установить признак "Установка принтера в автомате"
     *
     * @param val
     */
    public void setAutoPrinter(boolean val) {
        if (val) {
            mWorkModesEx.add(WorkModeExE.AUTO_PRINTER);
        } else {
            mWorkModesEx.remove(WorkModeExE.AUTO_PRINTER);
        }
        add(FZ54Tag.T1221_AUTOMAT_FLAG, val);
    }

    /**
     * Получить признак "Работы с маркированными товарами"
     *
     * @return
     */
    public boolean isMarkingGoods() {
        return mWorkModesEx.contains(WorkModeExE.MARKING_GOODS);
    }

    /**
     * Установить признак "Работы с маркированными товарами"
     *
     * @param val
     */
    public void setMarkingGoods(boolean val) {
        if (val) {
            mWorkModesEx.add(WorkModeExE.MARKING_GOODS);
        } else {
            mWorkModesEx.remove(WorkModeExE.MARKING_GOODS);
        }
    }

    /**
     * Получить признак "Осуществления ломбардной деятельности"
     *
     * @return
     */
    public boolean isPawnShopActivity() {
        return mWorkModesEx.contains(WorkModeExE.PAWNSHOP_ACTIVITY);
    }

    /**
     * Установить признак "Осуществления ломбардной деятельности"
     *
     * @param val
     */
    public void setPawnShopActivity(boolean val) {
        if (val) {
            mWorkModesEx.add(WorkModeExE.PAWNSHOP_ACTIVITY);
        } else {
            mWorkModesEx.remove(WorkModeExE.PAWNSHOP_ACTIVITY);
        }
    }

    /**
     * Получить признак "Осуществления страховой деятельности"
     *
     * @return
     */
    public boolean isInsuranceActivity() {
        return mWorkModesEx.contains(WorkModeExE.INSURANCE_ACTIVITY);
    }

    /**
     * Установить признак "Осуществления страховой деятельности"
     *
     * @param val
     */
    public void setInsuranceActivity(boolean val) {
        if (val) {
            mWorkModesEx.add(WorkModeExE.INSURANCE_ACTIVITY);
        } else {
            mWorkModesEx.remove(WorkModeExE.INSURANCE_ACTIVITY);
        }
    }

    /**
     * Получить признак "Проведение азартных игр"
     *
     * @return
     */
    public boolean isGamblingMode() {
        return mWorkModesEx.contains(WorkModeExE.GAMBLING_GAMES);
    }

    /**
     * Установить признак "Проведение азартных игр"
     *
     * @param val
     */
    public void setGamblingMode(boolean val) {
        if (val) {
            mWorkModesEx.add(WorkModeExE.GAMBLING_GAMES);
        } else {
            mWorkModesEx.remove(WorkModeExE.GAMBLING_GAMES);
        }
        add(FZ54Tag.T1193_GAMBLING_FLAG, val);
    }

    /**
     * @return
     */
    public Set<AgentTypeE> getAgentType() {
        return mAgentTypes;
    }

    /**
     * @return
     */
    public boolean isAgent() {
        return mAgentTypes.size()>0;
    }

    /**
     * Находится ли ККТ в режиме автомата
     *
     * @return
     */
    public boolean isAutomatedMode() {
        return mWorkModes.contains(WorkModeE.AUTO);
    }

    public void setAutomatedMode(boolean val) {
        if (val) {
            mWorkModes.add(WorkModeE.AUTO);
            put(FZ54Tag.T1036_AUTOMAT_NO, new Tag(String.format("% 20d", 1)));
        } else {
            mWorkModes.remove(WorkModeE.AUTO);
            remove(FZ54Tag.T1036_AUTOMAT_NO);
        }
    }

    public String getAutomateNumber() {
        if (isAutomatedMode())
            return get(FZ54Tag.T1036_AUTOMAT_NO).asString();
        return Const.EMPTY_STRING;
    }

    public void setAutomateNumber(String v) throws NumberFormatException {
        if (!isAutomatedMode()) return;

        Integer.parseInt(v);

        StringBuilder vBuilder = new StringBuilder(v);
        while (vBuilder.length() < 10) vBuilder.insert(0, " ");
        v = vBuilder.toString();
        put(FZ54Tag.T1036_AUTOMAT_NO, new Tag(v));
    }

    public Set<TaxModeE> getTaxModes() {
        return mTaxModes;
    }

    /**
     * Получить e-mail отправителя
     *
     * @return
     */
    public String getSenderEmail() {
        return get(FZ54Tag.T1117_SENDER_EMAIL).asString();
    }

    /**
     * Установить e-mail отправителя
     *
     * @param value
     */
    public void setSenderEmail(String value) {
        add(FZ54Tag.T1117_SENDER_EMAIL, value);
    }

    /**
     * Получить признак "ККТ для Интернет"
     *
     * @return
     */
    public boolean isInternetMode() {
        return mWorkModes.contains(WorkModeE.INTERNET);
    }

    /**
     * Установить признак "ККТ для Интернет"
     *
     * @param val
     */
    public void setInternetMode(boolean val) {
        if (val)
            mWorkModes.add(WorkModeE.INTERNET);
        else
            mWorkModes.remove(WorkModeE.INTERNET);
    }

    /**
     * Получить признак "режим шифрования"
     *
     * @return
     */
    public boolean isEncryptionMode() {
        return mWorkModes.contains(WorkModeE.ENCRYPTION);
    }

    public void setEncryptionMode(boolean val) {
        if (val)
            mWorkModes.add(WorkModeE.ENCRYPTION);
        else
            mWorkModes.remove(WorkModeE.ENCRYPTION);
    }

    /**
     * Получить признак "Оказание услуг"
     *
     * @return
     */
    public boolean isServiceMode() {
        return mWorkModes.contains(WorkModeE.SERVICE);
    }

    /**
     * Установить признак "Оказание услуг"
     *
     * @param val
     */
    public void setServiceMode(boolean val) {
        if (val)
            mWorkModes.add(WorkModeE.SERVICE);
        else
            mWorkModes.remove(WorkModeE.SERVICE);
    }

    /**
     * Получить признак "Использование БСО"
     *
     * @return
     */
    public boolean isBSOMode() {
        return mWorkModes.contains(WorkModeE.BSO);
    }

    /**
     * Установить признак "Использование БСО"
     *
     * @param val
     */
    public void setBSOMode(boolean val) {
        if (val)
            mWorkModes.add(WorkModeE.BSO);
        else
            mWorkModes.remove(WorkModeE.BSO);
    }

    /**
     * Получить адрес сайта ФНС
     *
     * @return
     */
    public String getFNSUrl() {
        return get(FZ54Tag.T1060_FNS_URL).asString();
    }

    /**
     * Установить адрес сайта ФНС
     *
     * @param value
     */
    public void setFNSUrl(String value) {
        add(FZ54Tag.T1060_FNS_URL, value);
    }

    /**
     * Данные ОФД
     *
     * @return
     */
    public OU ofd() {
        return mOfd;
    }

    /**
     * Получить причину изменений параметров ККТ/регистрации
     *
     * @return
     */
    public FiscalReasonE getRegistrationReason() {
        return mRegistrationReason;
    }

    /**
     * Получить номер последнего документа
     *
     * @return
     */
    public long getLastFNDocNumber(){return mLastDocument.mNumber;}

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeString(mFnNumber);
        p.writeString(mKkmNumber);
        p.writeString(mServiceVersion);
        p.writeByte(mFnState.bVal);
        p.writeByte(mSupppFFDVer.bVal);
        p.writeInt(mUnfinishedDocType);
        p.writeLong(mLastDocument.mNumber);
        p.writeLong(mLastDocument.getTimeInMillis());
        p.writeInt(mFnWarnings.iVal);
        p.writeInt(mConnectionMode.ordinal());
        p.writeByte(TaxModeE.toByteArray(mTaxModes));
        p.writeByte(AgentTypeE.toByteArray(mAgentTypes));
        p.writeByte(WorkModeE.toByteArray(mWorkModes));
        p.writeByte(WorkModeExE.toByteArray(mWorkModesEx));
        p.writeByte(mRegistrationReason.bVal);
        p.writeLong(mReRegistrationReason);
        mOwner.writeToParcel(p, flags);
        mOfd.writeToParcel(p, flags);
        mShift.writeToParcel(p, 0);
        mSignature.operator().writeToParcel(p, flags);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        mFnNumber = p.readString();
        mKkmNumber = p.readString();
        mServiceVersion = p.readString();
        mFnState = FNStateE.fromByte(p.readByte());
        mSupppFFDVer = FFDVersionE.fromByte(p.readByte());
        mUnfinishedDocType = p.readInt();
        mLastDocument.mNumber = p.readLong();
        mLastDocument.mDate.setTimeInMillis(p.readLong());
        mFnWarnings = new FnWarnings(p.readInt());
        mConnectionMode = FNConnectionModeE.values()[p.readInt()];
        mTaxModes=TaxModeE.fromByteArray(p.readByte());
        mAgentTypes = AgentTypeE.fromByteArray(p.readByte());
        mWorkModes = WorkModeE.fromByteArray(p.readByte());
        mWorkModesEx = WorkModeExE.fromByteArray(p.readByte());
        mRegistrationReason = FiscalReasonE.fromByte(p.readByte());
        mReRegistrationReason=p.readLong();
        mOwner.readFromParcel(p);
        mOfd.readFromParcel(p);
        mShift.readFromParcel(p);
        mSignature.operator().readFromParcel(p);
    }

    protected void updateLastDocumentInfo(long date, long number) {
        if (date != -1)
            mLastDocument.mDate.setTimeInMillis(date);
        mLastDocument.mNumber = number;
    }

    /**
     * Получить информацию о последнем проведенном документе
     *
     * @return
     */
    public LastDocumentInfo getLastDocument() {
        return mLastDocument;
    }

    /**
     * Версия фискального сервиса
     *
     * @return
     */
    public String getServiceVersion() {
        return mServiceVersion;
    }

    protected void updateLastDocumentInfo(Document doc) {
        mLastDocument.mNumber = doc.signature().number();
        mLastDocument.mDate.setTimeInMillis(doc.signature().signDate());
    }

    public static final Parcelable.Creator<KKMInfo> CREATOR = new Parcelable.Creator<KKMInfo>() {
        @Override
        public KKMInfo createFromParcel(Parcel p) {
            KKMInfo result = new KKMInfo();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public KKMInfo[] newArray(int size) {
            return new KKMInfo[size];
        }

    };

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    public String getClassUUID() {
        return CLASS_UUID;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject result = super.toJSON();

        result.put(KKM_NUMBER_TAG, mKkmNumber);
        result.put(OWNER_TAG, mOwner.toJSON());
        result.put(MODES_TAG, WorkModeE.toByteArray(mWorkModes));
        result.put(MODES_EX_TAG, WorkModeExE.toByteArray(mWorkModesEx));
        result.put(AGENT_MODES_TAG, AgentTypeE.toByteArray(mAgentTypes));
        result.put(TAX_MODES_TAG, TaxModeE.toByteArray(mTaxModes));
        result.put(SUPP_FFD_VER_TAG, mSupppFFDVer.bVal);

        return result;
    }

    public void resetState(){
        mFnState = FNStateE.UNKNOWN;
    }
}
