package rs.fncore.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Типы агентских услуг
 * @author nick
 *
 */
public enum AgentType {
	/** 
	 * Банковский агент
	 */
	BankAgent,
	/**
	 * Банковский субагент
	 */
	BankSubAgent,
	/**
	 * Платежный агент
	 */
	PaymentAgent,
	/**
	 * Платежный субагент
	 */
	PaymentSubAgent,
	/**
	 * Доверенное лицо
	 */
	Attorney,
	/**
	 * Комиссионер
	 */
	Commisionare,
	/**
	 * Другой тип агента
	 */
	Other;
	public byte bValue() {
		return (byte)(1 << ordinal());
	}
	/**
	 * Декодировать битовую маску агентских услуг в список
	 * @param val
	 * @return
	 */
	public static Set<AgentType> decode(byte val) {
		HashSet<AgentType> result = new HashSet<>();
		for(AgentType a : values())
			if((val & a.bValue()) == a.bValue() )
				result.add(a);
		return result;		
	}
	/**
	 * Закодировать список в битовую маску 
	 * @param val
	 * @return
	 */
	public static byte encode(Iterable<AgentType> val) {
		byte result = 0;
		for(AgentType a: val)
			result |= a.bValue();
		return result;
	}
}
