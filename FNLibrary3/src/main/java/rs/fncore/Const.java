package rs.fncore;

import android.content.Intent;

import java.nio.charset.Charset;

public class Const {

    private Const() {
    }

    public static final String EMPTY_STRING = "";
    public static final Charset ENCODING = Charset.forName("CP866");
    public static final int MAX_TAG_SIZE = 1024;
    public static final short CCIT_POLY = (short) 0x1021;

    /**
     * Интент для обращения к FiscalStorage
     */
    public static Intent FISCAL_STORAGE = new Intent("rs.fncore2.FiscalStorage");

    static {
        FISCAL_STORAGE.setPackage("rs.fncore2");
    }

    /**
     * Событие, отправляемое при изменении ФН
     */
    public static final String FN_STATE_CHANGED_ACTION = "rs.fncore2.fn.changed";

    /**
     * Событие, отправляемое при отправке очередного документа в ОФД
     */
    public static final String OFD_SENT_ACTION = "rs.fncore2.ofd.info";

    /**
     * Количество документов, оставшееся к отправке в ОФД, int для OFD_SENT_ACTION
     */
    public static final String OFD_DOCUMENTS = "ofd.documents";

    /**
     * Количество документов, оставшееся к отправке в ОИСМ, int для OFD_SENT_ACTION
     */
    public static final String OISM_DOCUMENTS = "oism.documents";

    /**
     * Событие, отправляемое FNCore2 если код маркировки не прошел проверку
     */
    public static final String MARKING_CODE_FAILED_USER_REQUEST_ACTION =
            "rs.fncore2.marking.failed.user.request";

    /**
     * Наименование товара не прошедшего проверку,
     * String для MARKING_CODE_FAILED_USER_REQUEST_ACTION
     */
    public static final String MARKING_ITEM_NAME = "item.name";

    /**
     * UUID товара не прошедшего проверку,
     * String для MARKING_CODE_FAILED_USER_REQUEST_ACTION, MARKING_CODE_FAILED_USER_RESPONCE_ACTION
     */
    public static final String MARKING_ITEM_UUID = "item.uuid";

    /**
     * Служебное событие тестирования
     */
    public static final String TEST_RESULT = "rs.fncore2.test.info";

    /**
     * Служеюная информация по теcтированию
     */
    public static final String TEST_TIME = "test.time";

    /**
     * Служеюная информация по теcтированию
     */
    public static final String TEST_GOOD_COUNT = "test.count.good";

    /**
     * Служеюная информация по теcтированию
     */
    public static final String TEST_ERROR_COUNT = "test.count.bad";

    /**
     * Служеюная информация по теcтированию
     */
    public static final String TEST_ERROR_BYTES = "test.count.error.bytes";

    /**
     * Служеюная информация по теcтированию
     */
    public static final String TEST_ERROR_INFO = "test.einfo";

    public static final String PRINT_COMPLETE_ACTION = "rs.fncore2.print_done";
    public static final String PRINT_COMPLETE_RESULT = "result";
    public static final String PRINT_COMPLETE_DOCUMENT_NO = "docNo";
}
