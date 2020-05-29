package rs.fncore.data;

import android.os.Parcel;

import rs.fncore.Const;

/** 
 * Данные агента
 * @author nick
 *
 */
public class AgentData extends TLV implements IReableFromParcel {

	
	private AgentType _type;
	
	public AgentData() {
	}

	/**
	 * Получить тип агентских услуг
	 * @return тип агентских услуг или null если не определена
	 */
	public AgentType getType() { return _type; }
	/**
	 * Установить тип агентских услуг
	 * @param type
	 */
	public void setType(AgentType type) {
		_type = type;
		 
	}
	/**
	 * Указать телефон поставщика (тег 1171)
	 * @param s
	 */
	public void setProviderPhone(String s) {
		if(s != null && !s.isEmpty())
			put(1171,new Tag(s));
		else
			remove(1171);
	}
	/**
	 * Получить телефон поставщика (тег 1171)
	 * @return
	 */
	public String getProviderPhone() {
		if(hasTag(1171))
			return get(1171).asString();
		return Const.EMPTY_STRING;
	}
	/**
	 * Установить наименование поставщика (тег 1225)
	 * @param s
	 */
	public void setProviderName(String s) {
		if(s != null && !s.isEmpty())
			put(1225,new Tag(s));
		else
			remove(1225);
	}
	/**
	 * Получить наименование поставщика (тег 1225)
	 * @return
	 */
	public String getProviderName() {
		if(hasTag(1225))
			return get(1225).toString();
		return Const.EMPTY_STRING;
	}
	/**
	 * Установить телефон оператора перевода (тег 1075)
	 * @param s
	 */
	public void setOperatorPhone(String s) {
		if(s != null && !s.isEmpty())
			put(1075,new Tag(s));
		else
			remove(1075);
	}
	/**
	 * Получить телефон оператора перевода (тег 1075)
	 * @return
	 */
	public String getOperatorPhone() {
		if(hasTag(1075))
			return get(1075).asString();
		return Const.EMPTY_STRING;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int arg1) {
		if(_type!=null)
			p.writeInt(_type.ordinal());
		else
			p.writeInt(-1);
		p.writeInt(size());
		for(int i=0;i<size();i++) {
			p.writeInt(keyAt(i));
			valueAt(i).writeToParcel(p);
		}
	}

	@Override
	public void readFromParcel(Parcel p) {
		clear();
		int t = p.readInt();
		if(t == -1) 
			_type = null;
		else
			_type = AgentType.values()[t];
		int count = p.readInt();
		while(count-- > 0) 
			put(p.readInt(),new Tag(p));
	}

}
