package rs.fncore.data;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

/**
 * Типы агентских услуг
 *
 * @author amv
 */

public enum AgentTypeE {
    /**
     * Банковский агент
     */
    BankAgent((1<<0), "БАНК. ПЛ. АГЕНТ", "Оказание услуг покупателю (клиенту) пользователем, являющимся банковским платежным агентом"),
    /**
     * Банковский субагент
     */
    BankSubAgent((1<<1), "БАНК. ПЛ. СУБАГЕНТ","Оказание услуг покупателю (клиенту) пользователем, являющимся банковским платежным субагентом"),
    /**
     * Платежный агент
     */
    PaymentAgent((1<<2),"ПЛ. АГЕНТ", "Оказание услуг покупателю (клиенту) пользователем, являющимся платежным агентом"),
    /**
     * Платежный субагент
     */
    PaymentSubAgent((1<<3),"ПЛ. СУБАГЕНТ", "Оказание услуг покупателю (клиенту) пользователем, являющимся платежным субагентом"),
    /**
     * Доверенное лицо
     */
    Attorney((1<<4),"ПОВЕРЕННЫЙ","Осуществление расчета с покупателем (клиентом) пользователем, являющимся поверенным"),
    /**
     * Комиссионер
     */
    Commisionare((1<<5),"КОМИССИОНЕР","Осуществление расчета с покупателем (клиентом) пользователем, являющимся комиссионером"),
    /**
     * Другой тип агента
     */
    Other((1<<6),"АГЕНТ","Осуществление расчета с покупателем (клиентом) пользователем, являющимся агентом и не являющимся банковским платежным агентом (субагентом), платежным агентом (субагентом), поверенным, комиссионером")
    ;

    public final byte bVal;
    public final String pName;
    public final String desc;

    private AgentTypeE(int value, String pName, String desc) {
        this.bVal = (byte)value;
        this.pName = pName;
        this.desc = desc;
    }

    public static AgentTypeE fromByte(byte number){
        for (AgentTypeE val:values()){
            if (val.bVal == number){
                return val;
            }
        }
        throw new InvalidParameterException("unknown value");
    }

    /**
     * Декодировать битовую маску агентских услуг в список
     *
     * @param val
     * @return
     */
    public static Set<AgentTypeE> fromByteArray(byte val) {
        HashSet<AgentTypeE> result = new HashSet<>();
        for (AgentTypeE a : values()) {
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
    public static byte toByteArray(Iterable<AgentTypeE> val) {
        byte result = 0;
        for (AgentTypeE a : val)
            result |= a.bVal;
        return result;
    }

    /**
     * Получить список наименований
     * @return
     */
    public static String [] getNames(){
        String [] res = new String[values().length];
        for (AgentTypeE val:values()){
            res[val.ordinal()]=val.pName;
        }
        return res;
    }
}
