package rs.fncore.data;

import android.os.Parcel;
import rs.fncore.Const;
import rs.fncore.FZ54Tag;

/**
 * Данные агента
 *
 * @author nick
 */
public class AgentData extends TLV implements IReableFromParcel {


    private AgentType _type;

    public AgentData() {
    }

    /**
     * Получить тип агентских услуг
     *
     * @return тип агентских услуг или null если не определена
     */
    public AgentType getType() {
        return _type;
    }

    /**
     * Установить тип агентских услуг
     *
     * @param type
     */
    public void setType(AgentType type) {
        _type = type;

    }

    /**
     * Указать телефон поставщика (тег 1171)
     *
     * @param s
     */
    public void setProviderPhone(String s) {
        if (s != null && !s.isEmpty())
            put(FZ54Tag.T1171_SUPPLIER_PHONE, new Tag(s));
        else
            remove(FZ54Tag.T1171_SUPPLIER_PHONE);
    }

    /**
     * Получить телефон поставщика (тег 1171)
     *
     * @return
     */
    public String getProviderPhone() {
        if (hasTag(FZ54Tag.T1171_SUPPLIER_PHONE))
            return get(FZ54Tag.T1171_SUPPLIER_PHONE).asString();
        return Const.EMPTY_STRING;
    }

    /**
     * Установить наименование поставщика (тег 1225)
     *
     * @param s
     */
    public void setProviderName(String s) {
        if (s != null && !s.isEmpty())
            put(FZ54Tag.T1225_SUPPLIER_NAME, new Tag(s));
        else
            remove(FZ54Tag.T1225_SUPPLIER_NAME);
    }

    /**
     * Получить наименование поставщика (тег 1225)
     *
     * @return
     */
    public String getProviderName() {
        if (hasTag(FZ54Tag.T1225_SUPPLIER_NAME))
            return get(FZ54Tag.T1225_SUPPLIER_NAME).toString();
        return Const.EMPTY_STRING;
    }

    /**
     * Установить телефон оператора перевода (тег 1075)
     *
     * @param s
     */
    public void setOperatorPhone(String s) {
        if (s != null && !s.isEmpty())
            put(FZ54Tag.T1075_TRANSFER_OPERATOR_PHONE, new Tag(s));
        else
            remove(FZ54Tag.T1075_TRANSFER_OPERATOR_PHONE);
    }

    /**
     * Получить телефон оператора перевода (тег 1075)
     *
     * @return
     */
    public String getOperatorPhone() {
        if (hasTag(FZ54Tag.T1075_TRANSFER_OPERATOR_PHONE))
            return get(FZ54Tag.T1075_TRANSFER_OPERATOR_PHONE).asString();
        return Const.EMPTY_STRING;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int arg1) {
        if (_type != null)
            p.writeInt(_type.ordinal());
        else
            p.writeInt(-1);
        p.writeInt(size());
        for (int i = 0; i < size(); i++) {
            p.writeInt(keyAt(i));
            valueAt(i).writeToParcel(p);
        }
    }

    @Override
    public void readFromParcel(Parcel p) {
        clear();
        int t = p.readInt();
        if (t == -1)
            _type = null;
        else
            _type = AgentType.values()[t];
        int count = p.readInt();
        while (count-- > 0)
            put(p.readInt(), new Tag(p));
    }
}
