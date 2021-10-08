package rs.fncore.data;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

/**
 * Системы налогообложения
 *
 * @author amv
 */

public enum TaxModeE {
    /**
     * Общая
     */
    Common((1<<0),"ОСН", "Общая"),
    /**
     * Упрощенная
     */
    SimpleIncome((1<<1),"УСН доход","Упрощенная"),
    /**
     * Упрощенная "доход - расход"
     */
    SimpleIncomeExcense((1<<2),"УСН доход - расход","Упрощенная \"доход - расход\""),
    /**
     * Единый налог
     */
    UnitedTax((1<<3),"ЕНВД","Единый налог"),
    /**
     * Сельскохозяйственный налог
     */
    AgroTax((1<<4),"ЕСН","Сельскохозяйственный налог"),
    /**
     * Патентная
     */
    Patent((1<<5),"Патент","Патентная");

    public final byte bVal;
    public final String pName;
    public final String desc;

    private TaxModeE(int value,String name, String descr) {
        this.bVal = (byte)value;
        this.pName = name;
        this.desc = descr;
    }

    public static TaxModeE fromByte(byte number){
        for (TaxModeE val:values()){
            if (val.bVal == number){
                return val;
            }
        }
        throw new InvalidParameterException("unknown value");
    }

    public static byte toByteArray(Iterable<TaxModeE> modes) {
        byte b = 0;
        for (TaxModeE mode : modes)
            b |= mode.bVal;
        return b;
    }

    public static Set<TaxModeE> fromByteArray(byte val) {
        Set<TaxModeE> result = new HashSet<>();
        for (TaxModeE mode : values()) {
            if ((val & mode.bVal) == mode.bVal)
                result.add(mode);
        }
        return result;
    }

    /**
     * Получить список наименований
     * @return
     */
    public static String [] getNames(){
        String [] res = new String[values().length];
        for (TaxModeE val:values()){
            res[val.ordinal()]=val.pName;
        }
        return res;
    }
}
