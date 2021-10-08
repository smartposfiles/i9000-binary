package rs.fncore.data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.security.InvalidParameterException;

/**
 * Ставка НДС
 *
 * @author amv
 */
public enum VatE {
    vat_20(1,"НДС 20%"),
    vat_10(2,"НДС 10%"),
    vat_20_120(3,"НДС 20/120"),
    vat_10_110(4,"НДС 10/110"),
    vat_0(5,"НДС 0%"),
    vat_none(6,"НДС НЕ ОБЛАГАЕТСЯ")
    ;

    public final byte bVal;
    public final String pName;

    private VatE(int value, String name) {
        this.bVal = (byte)value;
        this.pName = name;
    }

    public static VatE fromByte(byte number){
        for (VatE val:values()){
            if (val.bVal == number){
                return val;
            }
        }
        throw new InvalidParameterException("unknown value");
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
        default:
            return sum;
        }
    }

    /**
     * Получить список наименований
     * @return
     */
    public static String [] getNames(){
        String [] res = new String[values().length];
        for (VatE val:values()){
            res[val.ordinal()]=val.pName;
        }
        return res;
    }
}
