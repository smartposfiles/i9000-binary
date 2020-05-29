package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import rs.fncore.Const;
/**
 * Отчет о регистрации (состоянии) ККМ
 * @author nick
 *
 */
public class KKMInfo extends Document {

	private final String TAG=this.getClass().getName();
	public static final String KKM_VERSION = "001";
	public static final String FNS_URL = "www.nalog.ru";

	/**
	 * Битовые флаги состояния ФН
	 */
	public static final int FN_STATE_READY_BF = 1;
	public static final int FN_STATE_ACTIVE_BF = 2;
	public static final int FN_STATE_ARCHIVED_BF = 4;
	public static final int FN_STATE_NO_MORE_DATA = 8;

	public static final int ENCRYPTION_MODE = 1;
	public static final int OFFLINE_MODE    = 2;
	public static final int AUTO_MODE       = 4;
	public static final int SERVICE_MODE    = 8;
	public static final int INTERNET_MODE   = 0x20;
	public static final int BSO_MODE        = 0x10;
	
	/**
	 * Причина регистрации - регистрация ФН
	 */
	public static final int REASON_REGISTER = 0;
	/**
	 * Причина регистрации - замена ФН
	 */
	public static final int REASON_REPLACE_FN = 1;
	/**
	 * Причина регистрации - замена ОФД
	 */
	public static final int REASON_CHANGE_OFD = 2;
	/**
	 * Причина регистрации - изменение реквизитов
	 */
	public static final int REASON_CHANGE_SETTINGS = 3;
	/**
	 * Причина регистрации - Изменение настроек ККТ
	 */
	public static final int REASON_CHANGE_KKT_SETTINGS = 4;
	
	/**
	 * Версия ФФД
	 * @author nick
	 *
	 */
	public static enum FFDVersion {
		ver10((byte)1), ver105((byte)2),ver11((byte)3);
		FFDVersion(byte bVal) {
			_bVal = bVal;
		}
		private byte _bVal;
		public byte bVal() { return _bVal; }
		public static FFDVersion find(byte bVal) {
			for(FFDVersion v : values())
				if(v.bVal() == bVal) return v;
			return null;
		}
	}
	/**
	 * Тип физического подключения ФН
	 * @author nick
	 *
	 */
	public static enum FNConnectionMode {
		usb,uart,virtual,network
	}
	/**
	 * Информация о последнем проведенном документе
	 * @author nick
	 *
	 */
	public class LastDocumentInfo {
		private int _number;
		private Calendar _date = Calendar.getInstance();
		private LastDocumentInfo() {
			_date.setTimeInMillis(0);
		}
		/**
		 * Номер документа
		 * @return
		 */
		public int getNumber() { return _number; }
		/**
		 * Дата документа
		 * @return
		 */
		public long getTimeInMillis() { return _date.getTimeInMillis(); }
	}
	public static final String KKM_NUMBER_TAG = "KKMNo";
	public static final String OWNER_TAG = "Owner";
	public static final String OFD_TAG = "OFD";
	public static final String MODES_TAG = "Mode";
	public static final String TAX_MODES_TAG = "TaxModes";
	public static final String AGENT_MODES_TAG = "AgentModes";
	
	protected String _kkm_number;
	protected String _fn_number;
	protected int _fn_state;
	protected int _unfinished_doc_type;
	protected Shift _shift = new Shift();
	protected int _fn_warnings;
	protected String _serviceVersion = Const.EMPTY_STRING;
	protected LastDocumentInfo _last_document = new LastDocumentInfo();
	protected OU _owner = new OU();
	protected OU _ofd = new OU();
	
	protected Set<TaxMode> _tax_modes = new HashSet<>();
	protected Set<AgentType> _agentTypes = new HashSet<>();
	protected int _work_modes;
	protected int _registrationReason;
	protected FNConnectionMode _connection_mode = FNConnectionMode.virtual;
	
	public KKMInfo() {
		super();
		add(1193, false);
		add(1126, false);
		add(1207, false);
		add(1013, Const.EMPTY_STRING);
		add(1209, FFDVersion.ver105.bVal());
		add(1189, FFDVersion.ver105.bVal());
		add(1188, KKM_VERSION);
		add(1117, Const.EMPTY_STRING);
		add(1057, (byte) 0);
		add(1060,FNS_URL);
	}
	

	public KKMInfo(JSONObject json) {
		super(json);
		try {
			_kkm_number = json.getString(KKM_NUMBER_TAG);
			if(json.has(OWNER_TAG))
				_owner = new OU(json.getJSONObject(OWNER_TAG));
			if(json.has(OFD_TAG))
				_ofd = new OU(json.getJSONObject(OFD_TAG));
			_work_modes = json.getInt(MODES_TAG);
			_agentTypes = AgentType.decode((byte)json.getInt(AGENT_MODES_TAG));
			_tax_modes = TaxMode.decode((byte)json.getInt(TAX_MODES_TAG));
		} catch(JSONException jse) {
			Log.e(TAG,"exception",jse);
		}
	}
	/**
	 * Получить заводской номер ККМ
	 * @return
	 */
	public String getKKMSerial() {
		return get(1013).asString(); 
	}
	/**
	 * Получить регистрационный номер ККМ
	 * @return
	 */
	public String getKKMNumber() { return _kkm_number; }
	/**
	 * Установить  регистрационный номер ККМ
	 * @param v
	 */
	public void setKKMNumber(String v) {
		_kkm_number = v;
	}
	/**
	 * Получить номер фискального накопителя
	 * @return
	 */
	public String getFNNumber() { return _fn_number; }
	
	/**
	 * Получить текущую или последнюю открытую смену
	 * @return
	 */
	public Shift getShift() {return _shift; }
	/**
	 * Владелец ККМ
	 * @return
	 */
	public OU getOwner() {
		return _owner;
	}
	/**
	 * Информация о предупреждениях ФН
	 * @return
	 */
	public int getFNWarings() {
		return _fn_warnings;
	}

	/**
	 * Получить версию протокола ФФД
	 * @return
	 */
	public FFDVersion getFFDProtocolVersion() {
		byte bVal = 0; 
		if(hasTag(1209)) 
			bVal =  get(1209).asByte();
		else	
			bVal =  get(1189).asByte();
		return FFDVersion.find(bVal);
	}
	/**
	 * Установить версию протокола ФФД
	 * @param ver
	 */
	public void setFFDProtocolVersion(FFDVersion ver) {
		add(1209,ver.bVal());
		add(1189,ver.bVal());
	}
	
	public FNConnectionMode getConnectionMode() { return _connection_mode; }
	/**
	 * ФН подключен к ККМ
	 * @return
	 */
	public boolean isFNPresent() { return _fn_state != 0; }
	/**
	 * ФН находится в фискальном режиме
	 * @return
	 */
	public boolean isFNActive() {
		return ((_fn_state & FN_STATE_ACTIVE_BF) == FN_STATE_ACTIVE_BF) && !isFNArchived() ; 
	}
	/**
	 * ФН находится в постфискальном режиме
	 */
	public boolean isFNArchived() { return (_fn_state & FN_STATE_ARCHIVED_BF) == FN_STATE_ARCHIVED_BF; }

	/**
	 * Получить признак "автономная работа"
	 * @return
	 */
	public boolean isOfflineMode() {
		return (_work_modes & OFFLINE_MODE) == OFFLINE_MODE;
	}
	 /** Установить признак "автономная работа"
	 * @param val
	 */
	public void setOfflineMode(boolean val) {
		if (val) 
			_work_modes |= OFFLINE_MODE;
		 else 
			_work_modes &= ~OFFLINE_MODE;
	}
	/**
	 * Находится ли ККМ в режиме автомата
	 * @return
	 */
	public boolean isAutomatedMode() {
		return (_work_modes & AUTO_MODE) ==  AUTO_MODE;
	}

	/**
	 * Получить признак "Продажа подакцизного товара"
	 * @return
	 */
	public boolean isExcisesMode() {
		return get(1207).asBoolean();
	}
	/**
	 * Установить признак "Продажа подакцизного товара" 
	 * @param val
	 */
	public void setExcisesMode(boolean val) {
		add(1207, val);
	}
	/**
	 * Получить признак "Проведение лотереи"
	 * @return
	 */
	public boolean isLotteryMode() {
		return get(1126).asBoolean();
	}

	/**
	 * Установить признак "Проведение лотереи"
	 * @param val
	 */
	public void setLotteryMode(boolean val) {
		add(1126, val);
	}
	/**
	 * Получить признак "Проведение азартных игр"
	 * @return
	 */
	public boolean isCasinoMode() {
		return get(1193).asBoolean();
	}
	/**
	 * Установить признак "Проведение азартных игр" 
	 * @param val
	 */
	public void setCasinoMode(boolean val) {
		add(1193, val);
	}
	/**
	 * 
	 * @return
	 */
	public Set<AgentType> getAgentType() {
		return _agentTypes;
	}
	

	public void setAutomatedMode(boolean val) {
		if(val) {
			_work_modes |= AUTO_MODE;
			put(1036,new Tag(String.format("% 20d", 1)));
		}
		else {
			_work_modes &= ~AUTO_MODE;
			remove(1036);
		}
	}
	public String getAutomateNumber() {
		if(isAutomatedMode())
			return get(1036).asString();
		return Const.EMPTY_STRING;
	}
	public void setAutomateNumber(String v) {
		if(!isAutomatedMode()) return;
		try {
			Integer.parseInt(v);
		} catch(NumberFormatException nfe) {
			Log.e(TAG,"exception",nfe);
			return;
		}
		while(v.length() < 10) v = " "+v;
		put(1036,new Tag(v));
		
	}
	
	public Set<TaxMode> getTaxModes() {
		return _tax_modes;
	}
	
	/**
	 * Получить e-mail отправителя
	 * @return
	 */
	public String getSenderEmail() {
		return get(1117).asString();
	}
	/**
	 * Установить e-mail отправителя
	 * @param value
	 */
	public void setSenderEmail(String value) {
		add(1117, value);
	}

	/**
	 * Получить признак "ККТ для Интернет"
	 * @return
	 */
	public boolean isInternetMode() {
		return (_work_modes & INTERNET_MODE) == INTERNET_MODE;
	}
	/**
	 * Установить признак "ККТ для Интернет"
	 * @param val
	 */
	public void setInternetMode(boolean val) {
		if (val)
			_work_modes |= INTERNET_MODE;
		else
			_work_modes &= ~INTERNET_MODE;
	}
	/**
	 * Получить признак "режим шифрования"
	 * @return
	 */
	public boolean isEncryptionMode() {
		return (_work_modes & ENCRYPTION_MODE) == ENCRYPTION_MODE;
	}
	public void setEncryptionMode(boolean val) {
		if(val)
			_work_modes |= ENCRYPTION_MODE;
		else
			_work_modes &= ~ENCRYPTION_MODE;
	}
	/**
	 * Получить признак "Оказание услуг"
	 * @return
	 */
	public boolean isServiceMode() {
		return (_work_modes & SERVICE_MODE) == SERVICE_MODE;
	}
	/**
	 * Установить признак "Оказание услуг"
	 * @param val
	 */
	public void setServiceMode(boolean val) {
		if (val)
			_work_modes |= SERVICE_MODE;
		else
			_work_modes &= ~SERVICE_MODE;
	}
	/**
	 * Получить признак "Использование БСО"
	 * @return
	 */
	public boolean isBSOMode() {
		return (_work_modes & BSO_MODE) == BSO_MODE;
	}
	/**
	 * Установить признак "Использование БСО"
	 * @param val
	 */
	public void setBSOMode(boolean val) {
		if (val)
			_work_modes |= BSO_MODE;
		else
			_work_modes &= ~BSO_MODE;
	}
	
	/**
	 * Получить адрес сайта ФНС
	 * @return
	 */
	public String getFNSUrl() { return get(1060).asString(); }
	/**
	 * Установить адрес сайта ФНС
	 * @param value
	 */
	public void setFNSUrl(String value) {
		add(1060,value);
	}
	/**
	 * Данные ОФД
	 * @return
	 */
	public OU ofd() { return _ofd; }
	/**
	 * Получить причину изменений параметров ККМ/регистрации
	 * @return
	 */
	public int getRegistrationReason() { return _registrationReason; }
	
	@Override
	public void writeToParcel(Parcel p, int flags) {
		super.writeToParcel(p, flags);
		p.writeString(_fn_number);
		p.writeString(_kkm_number);
		p.writeString(_serviceVersion);
		p.writeInt(_fn_state);
		p.writeInt(_unfinished_doc_type);
		p.writeInt(_last_document._number);
		p.writeLong(_last_document.getTimeInMillis());
		p.writeInt(_fn_warnings);
		p.writeInt(_connection_mode.ordinal());
		p.writeInt(_tax_modes.size());
		for(TaxMode mode: _tax_modes)
			p.writeInt(mode.ordinal());
		p.writeInt(_agentTypes.size());
		for(AgentType a : _agentTypes)
			p.writeInt(a.ordinal());
		p.writeInt(_work_modes);
		p.writeInt(_registrationReason);
		_owner.writeToParcel(p, flags);
		_ofd.writeToParcel(p, flags);
		_shift.writeToParcel(p, 0);
		_signature.operator().writeToParcel(p, flags);
	}
	
	@Override
	public void readFromParcel(Parcel p) {
		super.readFromParcel(p);
		_fn_number = p.readString();
		_kkm_number = p.readString();
		_serviceVersion = p.readString();
		_fn_state = p.readInt();
		_unfinished_doc_type = p.readInt();
		_last_document._number = p.readInt();
		_last_document._date.setTimeInMillis(p.readLong());
		_fn_warnings = p.readInt();
		_connection_mode = FNConnectionMode.values()[p.readInt()];
		int count = p.readInt();
		_tax_modes.clear();
		while(count-->0)
			_tax_modes.add(TaxMode.values()[p.readInt()]);
		count = p.readInt();
		_agentTypes.clear();
		while(count-- > 0)
			_agentTypes.add(AgentType.values()[p.readInt()]);
		_work_modes = p.readInt();
		_registrationReason = p.readInt();
		_owner.readFromParcel(p);
		_ofd.readFromParcel(p);
		_shift.readFromParcel(p);
		if(p.dataAvail() > 0)
			_signature.operator().readFromParcel(p);
	}

	@Override
	public byte[][] pack() {
		add(1057,AgentType.encode(_agentTypes));
		return super.pack();
	}
	
	protected void updateLastDocumentInfo(long date, int number) {
		if(date != -1)
			_last_document._date.setTimeInMillis(date);
		_last_document._number = number;
	}
	/**
	 * Получить информацию о последнем проведенном документе
	 * @return
	 */
	public LastDocumentInfo getLastDocument() { return _last_document; }
	
	/**
	 * Версия фискального сервиса
	 * @return
	 */
	public String getServiceVersion() { return _serviceVersion; }
	
	protected void updateLastDocumentInfo(Document doc) {
		_last_document._number = doc.signature().number();
		_last_document._date.setTimeInMillis(doc.signature().signDate());
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
		return KKMInfo.class.getName();
	}
	@Override
	public JSONObject toJSON() {
		JSONObject result = super.toJSON();
		try {
			result.put(KKM_NUMBER_TAG, _kkm_number);
			result.put(OWNER_TAG,_owner.toJSON());
			result.put(MODES_TAG,_work_modes);
			result.put(AGENT_MODES_TAG,AgentType.encode(_agentTypes));
			result.put(TAX_MODES_TAG,TaxMode.encode(_tax_modes));
		} catch(JSONException jse) { }
		return result;
	}
}
