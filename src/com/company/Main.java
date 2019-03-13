package com.company;

import com.company.services.TransactionSearchService;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        TransactionSearchService transactionSearchService = new TransactionSearchService();
        String[] types;
        if (args.length > 1) {
            types = new String[args.length - 1];
            System.arraycopy(args, 1, types, 0, args.length - 1);
        } else {
            types = null;
        }
        try {
            long start = System.currentTimeMillis();
            File file = new File(args[0]);
            System.out.printf("%-22s%-22s%-12s%-15s%-22s%-22s%-22s%-22s%-22s%-22s%n",
                    "SequenceNo",
                    "TranType",
                    "Time",
                    "Duration",
                    "Status-1004",
                    "Status-1010",
                    "Error-0011",
                    "Thread",
                    "RRN",
                    "Comment");
            transactionSearchService.searchTranByType(file, types);
            System.err.println((System.currentTimeMillis() - start) / 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
