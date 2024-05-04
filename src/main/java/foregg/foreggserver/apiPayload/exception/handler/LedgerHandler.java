package foregg.foreggserver.apiPayload.exception.handler;

import foregg.foreggserver.apiPayload.code.BaseErrorCode;
import foregg.foreggserver.apiPayload.exception.GeneralException;

public class LedgerHandler extends GeneralException {

    public LedgerHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }

}
