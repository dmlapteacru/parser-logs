package com.company.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.company.enums.DefaultField.*;

class FormatPrinter {

    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    private FormatPrinter() {}

    private static void printOpiMessageFormatted(LocalDateTime previousOperationTime, Map<String, String> transaction, String comment) {
        String duration = previousOperationTime == null ? "-" : parseDuration(Duration.between(previousOperationTime,
                LocalDateTime.parse(transaction.get(TRANS_D_T_BRIDGE.getOpi()), timeFormatter)));

        String printFormatter = "%-22s%-22s%-12s%-15s%-22s%-22s%-22s%-22s%-22s%-22s%n";
        System.out.printf(printFormatter,
                transaction.get("SequenceNo"),
                transaction.get("TransType") + " - OPI TT",
                transaction.get("TimeBridge"),
                duration,
                transaction.get("RespText") == null ? "-" : transaction.get("RespText"),
                transaction.get("RespText") == null ? "-" : transaction.get("RespText"),
                '-',
                transaction.get("Thread"),
                transaction.get(RRN.getOpi()) == null ? "-" : transaction.get(RRN.getOpi()),
                comment);
    }

    private static void printGatewayMessageFormat(LocalDateTime previousOperationTime,
                                                  Map<String, String> transaction, String comment) {
        String printFormatter = "%-22s%-22s%-12s%-15s%-22s%-22s%-22s%-22s%-22s%-22s%n";
        System.out.printf(printFormatter,
                transaction.get("0007"),
                transaction.get("0001") + " - Gateway API TT",
                transaction.get("TimeBridge"),
                parseDuration(Duration.between(previousOperationTime,
                        LocalDateTime.parse(transaction.get("TranDateTimeBridge"), timeFormatter))),
                transaction.get("1004") == null ? '-' : transaction.get("1004"),
                transaction.get(RESP_TEXT.getGtwAdditional()) == null ? '-' : transaction.get(RESP_TEXT.getGtwAdditional()),
                transaction.get(HOST_RESP_TEXT.getGtw()) == null ? '-' : transaction.get(HOST_RESP_TEXT.getGtw()),
                transaction.get("Thread"),
                "-",
                comment);
    }

    private static String parseDuration(Duration duration) {
        return duration.toString().substring(2).toLowerCase().replace("h", "h ").replace("m", "m ");
    }

    static void printTransaction(Map<String, String> request,
                                 Map<String, String> gtwWrite,
                                 Map<String, String> gtwRead,
                                 Map<String, String> response) {
        FormatPrinter.printOpiMessageFormatted(null, request, "OPI Request");
        FormatPrinter.printGatewayMessageFormat(LocalDateTime.parse(request.get("TranDateTimeBridge"), timeFormatter),
                gtwWrite, "SMWriter");
        FormatPrinter.printGatewayMessageFormat(LocalDateTime.parse(gtwWrite.get("TranDateTimeBridge"), timeFormatter),
                gtwRead, "SMReader");
        FormatPrinter.printOpiMessageFormatted(LocalDateTime.parse(gtwRead.get("TranDateTimeBridge"), timeFormatter),
                response, "OPI Response");
        System.out.println("\n");
    }
}
