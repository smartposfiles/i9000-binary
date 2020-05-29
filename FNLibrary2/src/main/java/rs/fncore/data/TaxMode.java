package rs.fncore.data;

import java.util.HashSet;
import java.util.Set;

/** 
 * Системы налогообложения
 * @author nick
 *
 */
public enum TaxMode {
	/**
	 * Общая
	 */
	Common,
	/**
	 * Упрощенная 
	 */
	SimpleIncome,
	/**
	 * Упрощенная "доход - расход"
	 */
	SimpleIncomeExcense,
	/**
	 * Единый налог
	 */
	UnitedTax,
	/**
	 * Сельскохозяйственный налог
	 */
	AgroTax,
	/**
	 * Патентная
	 */
	Patent;
	public byte bValue() {
		return (byte)(1 << ordinal());
	}
	public static TaxMode decodeOne(byte b) {
		for(TaxMode mode : values())
			if(b == mode.bValue()) return mode;
		return null;
	}
	public static byte encode(Iterable<TaxMode> modes) {
		byte b = 0;
		for(TaxMode mode : modes)
			b |= mode.bValue(); 
		return b;	
	}
	public static Set<TaxMode> decode(byte val) {
		Set<TaxMode> result = new HashSet<>();
		for(TaxMode mode: values())
			if((val & mode.bValue()) == mode.bValue())
				result.add(mode);
		return result;
	}
}
