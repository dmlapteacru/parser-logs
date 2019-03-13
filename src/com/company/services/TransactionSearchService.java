package com.company.services;


import java.io.*;
import java.util.*;

import static com.company.enums.DefaultField.*;

public class TransactionSearchService {

    public void searchTranByType(File file, String... tranTypes) throws IOException {
        Map<String, String> opiRequest = new HashMap<>();
        Map<String, String> gatewayWriterMessage = new HashMap<>();
        Map<String, String> gatewayReaderMessage = new HashMap<>();
        Map<String, String> opiResponse = new HashMap<>();
        long line = 0;
        BufferedReader buffReader = new BufferedReader(new FileReader(file));
        String readLine;
        while ((readLine = buffReader.readLine()) != null) {
            line++;
            if (readLine.contains(OPI_REQ.getOpi())) {
                opiRequest = readOpiRequestFromLine(line - 1, file);
                if (tranTypes != null) {
                    if (Arrays.stream(tranTypes).anyMatch(opiRequest.get("TransType")::contains)) {
                      gatewayWriterMessage = readGatewayMessage(line, file, opiRequest.get(SEQUENCE_NO.getOpi()), opiRequest
                                        .get(THREAD.getOpi()), "SMWriter");

                      gatewayReaderMessage = readGatewayMessage(line, file, opiRequest.get(SEQUENCE_NO.getOpi()), opiRequest.get(THREAD.getOpi()), "SMReader");
                      opiResponse = readOpiResponseBySeqNo(line, file, opiRequest.get(SEQUENCE_NO.getOpi()));
                      FormatPrinter.printTransaction(opiRequest, gatewayWriterMessage, gatewayReaderMessage, opiResponse);

                        searchTransByRRN(line, file, opiResponse.get(RRN.getOpi()));
                    }
                } else {
                    gatewayWriterMessage = readGatewayMessage(line, file, opiRequest.get(SEQUENCE_NO.getOpi()), opiRequest.get(THREAD.getOpi()), "SMWriter");
                    gatewayReaderMessage = readGatewayMessage(line, file, opiRequest.get(SEQUENCE_NO.getOpi()),opiRequest.get(THREAD.getOpi()), "SMReader");
                    opiResponse = readOpiResponseBySeqNo(line, file, opiRequest.get(SEQUENCE_NO.getOpi()));

                    FormatPrinter.printTransaction(opiRequest, gatewayWriterMessage, gatewayReaderMessage, opiResponse);
                }
            }
            opiRequest.clear();
            opiResponse.clear();
            gatewayWriterMessage.clear();
            gatewayReaderMessage.clear();
        }
        buffReader.close();
    }

    private Map<String, String> readOpiRequestFromLine(long line, File file) throws IOException {
        Map<String, String> opiRequest = new HashMap<>();
        BufferedReader buffReader = new BufferedReader(new FileReader(file));
        String readLine = buffReader.lines().skip(line).findFirst().orElse("");
        opiRequest.put(TRANS_D_T_BRIDGE.getOpi(), readLine.substring(0, readLine.indexOf('Z')));
        opiRequest.put(T_BRIDGE.getOpi(), readLine.substring(readLine.indexOf(' ') + 1, readLine.indexOf(',')));
        opiRequest.put(THREAD.getOpi(), readLine.substring(readLine.indexOf('['), readLine.indexOf(']') + 1));
        while (!(readLine = buffReader.readLine()).startsWith("</")) {
            try {
                opiRequest.put(readLine.substring(readLine.indexOf('<') + 1, readLine.indexOf('>')),
                        readLine.substring(readLine.indexOf('>') + 1, readLine.indexOf("</")));
            } catch (IndexOutOfBoundsException e) {
                /// TODO empty catch is bad xd, but I need to skip few lines
            }
        }
        buffReader.close();
        return opiRequest;
    }

    private Map<String, String> readOpiResponseBySeqNo(long line, File file, String sequenceNo) throws IOException {
        Map<String, String> opiResponse = new HashMap<>();
        BufferedReader buffReader = new BufferedReader(new FileReader(file));
        String readLine;
        buffReader.lines().skip(line).findFirst().orElse("");
        String initLineMessage;

        while ((readLine = buffReader.readLine()) != null) {
            if (readLine.contains(OPI_RES.getOpi())) {
                initLineMessage = readLine;
                while (!(readLine = buffReader.readLine()).startsWith("</")) {
                    try {
                        opiResponse.put(readLine.substring(readLine.indexOf('<') + 1, readLine.indexOf('>')),
                                readLine.substring(readLine.indexOf('>') + 1, readLine.indexOf("</")));
                    } catch (IndexOutOfBoundsException e) {
                        /// TODO empty catch is bad xd, but I need to skip few lines
                    }
                }
                if (opiResponse.get(SEQUENCE_NO.getOpi()).contains(sequenceNo)) {
                    opiResponse.put(TRANS_D_T_BRIDGE.getOpi(), initLineMessage.substring(0, initLineMessage.indexOf('Z')));
                    opiResponse.put(T_BRIDGE.getOpi(), initLineMessage.substring(initLineMessage.indexOf(' ') + 1, initLineMessage.indexOf(',')));
                    opiResponse.put(THREAD.getOpi(), initLineMessage.substring(initLineMessage.indexOf('['), initLineMessage.indexOf(']') + 1));
                    break;
                } else {
                    opiResponse.clear();
                }
            }
        }
        buffReader.close();
        return opiResponse;
    }

    private Map<String, String> readGatewayMessage(long line, File file, String sequenceNo, String thread, String messageType) throws IOException {
        BufferedReader buffReader = new BufferedReader(new FileReader(file));
        String readLine;
        String initLineMessage;
        Map<String, String> gatewayMessage = new HashMap<>();
        buffReader.lines().skip(line - 1).findFirst().orElse("");
        while ((readLine = buffReader.readLine()) != null) {
            if (readLine.contains(messageType) && readLine.contains(thread)) {
                initLineMessage = readLine;
                while (!(readLine = buffReader.readLine()).matches("(^\\d{4},)*")) {
                    try {
                        gatewayMessage.put(readLine.substring(0, readLine.indexOf(',')),
                                readLine.substring(readLine.indexOf(',') + 1));
                    } catch (IndexOutOfBoundsException e) {
                        if (readLine.startsWith("java")) {
                            gatewayMessage.put(RESP_TEXT.getGtw(), readLine);
                            gatewayMessage.put(TRANS_D_T_BRIDGE.getOpi(), initLineMessage.substring(0, initLineMessage.indexOf('Z')));
                            gatewayMessage.put(T_BRIDGE.getOpi(), initLineMessage.substring(initLineMessage.indexOf(' ') + 1, initLineMessage.indexOf(',')));
                            gatewayMessage.put(THREAD.getOpi(), initLineMessage.substring(initLineMessage.indexOf('['), initLineMessage.indexOf(']') + 1));
                            buffReader.close();
                            return gatewayMessage;
                        }
                    }
                }
                try {
                    if (gatewayMessage.get(SEQUENCE_NO.getGtw()).matches(".*(" + sequenceNo + ")$")
                            || gatewayMessage.get(SEQUENCE_NO.getGtwAdditional()).matches(".*(" + sequenceNo + ")$")) {
                        gatewayMessage.put(TRANS_D_T_BRIDGE.getOpi(), initLineMessage.substring(0, initLineMessage.indexOf('Z')));
                        gatewayMessage.put(T_BRIDGE.getOpi(), initLineMessage.substring(initLineMessage.indexOf(' ') + 1, initLineMessage.indexOf(',')));
                        gatewayMessage.put(THREAD.getOpi(), initLineMessage.substring(initLineMessage.indexOf('['), initLineMessage.indexOf(']') + 1));
                        break;
                    } else {
                        gatewayMessage.clear();
                    }
                } catch (NullPointerException e) {
                    gatewayMessage.clear();
                }
            }
        }
        buffReader.close();
        return gatewayMessage;
    }

    private void searchTransByRRN(long line, File file, String rrnNumber) throws IOException {
        Map<String, String> opiRequest = new HashMap<>();
        Map<String, String> gatewayWriterMessage = new HashMap<>();
        Map<String, String> gatewayReaderMessage = new HashMap<>();
        Map<String, String> opiResponse = new HashMap<>();
        BufferedReader buffReader = new BufferedReader(new FileReader(file));
        long newLine = line;
        buffReader.lines().skip(newLine - 1).findFirst().orElse("");
        String readLine;
        while ((readLine = buffReader.readLine()) != null) {
            if (rrnNumber == null) {
                break;
            }
            newLine++;
            if (readLine.contains(OPI_REQ.getOpi())) {
                opiRequest = readOpiRequestFromLine(newLine - 1, file);
                    if (opiRequest.get(ORIGINAL_RRN.getOpi()) != null && opiRequest.get(ORIGINAL_RRN.getOpi()).contains(rrnNumber)) {
                        gatewayWriterMessage = readGatewayMessage(newLine, file, opiRequest.get(SEQUENCE_NO.getOpi()), opiRequest.get(THREAD.getOpi()), "SMWriter");
                        gatewayReaderMessage = readGatewayMessage(newLine, file, opiRequest.get(SEQUENCE_NO.getOpi()), opiRequest.get(THREAD.getOpi()), "SMReader");
                        opiResponse = readOpiResponseBySeqNo(newLine, file, opiRequest.get(SEQUENCE_NO.getOpi()));

                        FormatPrinter.printTransaction(opiRequest, gatewayWriterMessage, gatewayReaderMessage, opiResponse);
                        break;
                    }
            }
            opiRequest.clear();
            opiResponse.clear();
            gatewayWriterMessage.clear();
            gatewayReaderMessage.clear();
        }
        buffReader.close();
    }
}
