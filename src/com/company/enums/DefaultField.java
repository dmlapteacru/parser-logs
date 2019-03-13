package com.company.enums;

import java.util.Arrays;
import java.util.List;

public enum DefaultField {
    OPI_RES("TransactionRequest: OPI Response"),
    OPI_REQ("TransactionRequest: OPI Request"),
    THREAD("Thread"),
    T_BRIDGE("TimeBridge"),
    TRANS_D_T_BRIDGE("TranDateTimeBridge"),
    SEQUENCE_NO("SequenceNo", "0007", "0070"),
    RESP_TEXT("RespText", "1004", "1010"),
    HOST_RESP_TEXT("", "0011"),
    TRANS_TYPE("TransType", "0001"),
    RRN("RRN"),
    ORIGINAL_RRN("OriginalRRN");

    String opi;
    String gtw;
    String gtwAdditional;

    DefaultField(String opi) {
        this.opi = opi;
    }

    DefaultField(String opi, String gtw) {
        this.opi = opi;
        this.gtw = gtw;
    }

    DefaultField(String opi, String gtw, String gtwAdditional) {
        this.opi = opi;
        this.gtw = gtw;
        this.gtwAdditional = gtwAdditional;
    }

    public String getOpi() {
        return opi;
    }

    public String getGtw() {
        return gtw;
    }

    public String getGtwAdditional() {
        return gtwAdditional;
    }

    public List<DefaultField> getFieldsForPrint() {
        return Arrays.asList(SEQUENCE_NO, TRANS_TYPE, T_BRIDGE, THREAD, RESP_TEXT);
    }
}
