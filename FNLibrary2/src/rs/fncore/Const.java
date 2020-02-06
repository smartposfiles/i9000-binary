package rs.fncore;

import java.nio.charset.Charset;

import android.content.Intent;

public class Const {
	/**
	 * Ошибки возвращаемые операциями FiscalStorage
	 * @author nick
	 *
	 */
	public static class Errors {
		private Errors() { }
		public static final int NO_ERROR = 0;
		public static final int SUM_MISMATCH = 0xD0;
		public static final int CASH_LESS_REFUND = 0xD1;
		public static final int INVALID_SHIFT_STATE = 0xF0;
		public static final int DEVICE_ABSEND = 0xF1;
		public static final int READ_TIMEOUT = 0xF2;
		public static final int OPERATION_ABORTED = 0xF3;
		public static final int CRC_ERROR = 0xF4;
		public static final int WRITE_ERROR = 0xF5;
		public static final int READ_ERROR = 0xF6;
		public static final int DATA_ERROR = 0xF7;
		public static final int FN_MISMATCH = 0xF8;
		public static final int SYSTEM_ERROR = 0xF9;
		public static final int INVALID_CHECK_ITEMS  =0xFA;
		public static final int DATE_MISMATCH  = 0xFC;
		public static final int HAS_UNSENT_DOCS = 0xFD;
		public static final int SETTINGS_LOST = 0xFE;
		public static final int NEW_FN = 0xCA;
		public static final int OLD_FN_HAS_DATA = 0xCB;
		public static final int FN_REPLACEMENT = 0xCD;
		public static final int NO_CASH = 0xCE;
	}
	
	private Const() { }
	
	public static final String EMPTY_STRING = "";
	public static final Charset ENCODING = Charset.forName("CP866");
	public static final int MAX_TAG_SIZE = 1024;
	public static final short CCIT_POLY = (short)0x1021;
	
	/**
	 * Интент для обращения к FiscalStorage
	 */
	public static Intent FISCAL_STORAGE = new Intent("rs.fncore2.FiscalStorage");
	static {
		FISCAL_STORAGE.setPackage("rs.fncore2");
	}
	/**
	 * Событие, отправляемое при отправке очередного документа в ОФД
	 */
	public static final String OFD_SENT_ACTION = "rs.fncore2.ofd_info";
	/**
	 * Количество документов, оставшееся к отправке, int
	 */
	public static final String OFD_DOCUMENTS = "documents";
	
	public static final String PRINT_COMPLETE_ACTION = "rs.fncore2.print_done";
	public static final String PRINT_COMPLETE_RESULT = "result";
	public static final String PRINT_COMPLETE_DOCUMENT_NO = "docNo";
}
