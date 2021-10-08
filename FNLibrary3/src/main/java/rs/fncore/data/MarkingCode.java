package rs.fncore.data;


import android.os.Parcel;

import java.security.InvalidParameterException;

import rs.fncore.Errors;
import rs.fncore.FZ54Tag;

import static rs.fncore.Errors.CHECK_MARKING_CODE;

/**
 * Рабочая смена
 *
 * @author amv
 */
public class MarkingCode extends Document {

    public final static String CLASS_NAME="MarkingCode";
    public final static String CLASS_UUID="72c6d356-ebaf-11eb-9a03-0242ac130003";

    /**
     * Тип кода маркировки тег 2100
     *
     * @author amv
     */
    public enum CodeTypesE {
        Unknown(0, false),
        Short(1, false),
        Resp88(2, true),
        Resp44NoFnCheck(3, false),
        Resp44FnCheck(4, true),
        Resp4NoFnCheck(5, false),
        ;

        CodeTypesE(int bVal, boolean needGSOffset) {
            this.bVal = (byte)bVal;
            this.needGSOffset=needGSOffset;
        }

        public final byte bVal;
        public final boolean needGSOffset;

        public static CodeTypesE fromByte(byte bVal) {
            for (CodeTypesE v : values())
                if (v.bVal == bVal) return v;
            return Unknown;
        }
    }

    public static class ItemCheckResult_2106 {
        private static final int CODE_PROCESSED_MASK =(1<<0);
        private static final int CODE_CHECKED_MASK =(1<<1);
        private static final int CODE_OISM_PROCESSED_MASK =(1<<2);
        private static final int CODE_OISM_CHECKED_MASK =(1<<3);
        private static final int AUTONOMOUS_MASK =(1<<4);

        public final boolean codeProcessed;
        public final boolean codeChecked;
        public final boolean codeOISMProcessed;
        public final boolean codeOISMChecked;
        public final boolean autonomousMode;

        public final byte bVal;

        public ItemCheckResult_2106(byte val){
            bVal=val;
            codeProcessed = (val& CODE_PROCESSED_MASK)== CODE_PROCESSED_MASK;
            codeChecked = (val& CODE_CHECKED_MASK)== CODE_CHECKED_MASK;
            codeOISMProcessed = (val& CODE_OISM_PROCESSED_MASK)== CODE_OISM_PROCESSED_MASK;
            codeOISMChecked = (val& CODE_OISM_CHECKED_MASK)== CODE_OISM_CHECKED_MASK;
            autonomousMode = (val& AUTONOMOUS_MASK)== AUTONOMOUS_MASK;
        }

        public String getMarkTag(){
            if (bVal==1||bVal==0x11||bVal==0x5||bVal==0x7){
                return "[M-]";
            } else if (isPositiveChecked()){
                return "[M+]";
            } else {
                return "[M]";
            }
        }

        public boolean isPositiveChecked(){
            if (bVal==0xF || bVal==0xB) return true;
            return false;
        }
    }

    /**
     * Планируемый статус товара тег 2003, 2110
     *
     * @author amv
     */
    public enum PlannedItemStatusE {
        Unknown(0),
        PieceItemSell(1),
        MeasuredItemSell(2),
        PieceItemReturned(3),
        MeasuredItemReturned(4),
        ItemNotChanged(255),
        ;

        PlannedItemStatusE(int bVal) {
            this.bVal = (byte)bVal;
        }

        public final byte bVal;
        public static PlannedItemStatusE fromByte(byte bVal) {
            for (PlannedItemStatusE v : values())
                if (v.bVal == bVal) return v;
            return Unknown;
        }
    }

    protected String mFullCode=null;
    protected ItemCheckResult_2106 mMarkingCheckResult = new ItemCheckResult_2106((byte)0);

    public MarkingCode(){}

    public MarkingCode(String code, PlannedItemStatusE itemStatus) throws InvalidParameterException{
        mFullCode=code;
        setPlannedItemStatus(itemStatus);
        add(FZ54Tag.T2102_MARKING_CODE_REGIME, (byte)0);
    }

    /**
     * Признак наличия кода маркировки
     *
     * @return
     */
    public boolean isEmpty(){
        return mFullCode == null || mFullCode.isEmpty();
    }

    /**
     * получить код маркировки
     *
     * @return
     */
    public String getCode(){
        return mFullCode;
    }

    /**
     * получить планируемый статус товара
     *
     * @return
     */
    public PlannedItemStatusE getPlannedItemStatus() {
       return PlannedItemStatusE.fromByte(get(FZ54Tag.T2003_PLANNED_ITEM_STATE).asByte());
    }

    /**
     * установить планируемый статус товара
     *
     * @return
     */
    public void setPlannedItemStatus(PlannedItemStatusE itemStatus) {
        add(FZ54Tag.T2003_PLANNED_ITEM_STATE, itemStatus.bVal);
        add(FZ54Tag.T2110_ASSIGNED_ITEM_STATUS, itemStatus.bVal);
    }

    /**
     * получить результат проверки маркировки
     *
     * @return
     */
    public ItemCheckResult_2106 getCheckResult() {
        if (isEmpty()){
            return null;
        }
        return mMarkingCheckResult;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeString(mFullCode);
        p.writeByte(mMarkingCheckResult.bVal);
    }

    @Override
    public void readFromParcel(Parcel p) {
        super.readFromParcel(p);
        mFullCode=p.readString();
        mMarkingCheckResult=new ItemCheckResult_2106(p.readByte());
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public String getClassUUID() {
        return CLASS_UUID;
    }

    public static final Creator<MarkingCode> CREATOR = new Creator<MarkingCode>() {
        @Override
        public MarkingCode createFromParcel(Parcel p) {
            MarkingCode result = new MarkingCode();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public MarkingCode[] newArray(int size) {
            return new MarkingCode[size];
        }
    };
}
