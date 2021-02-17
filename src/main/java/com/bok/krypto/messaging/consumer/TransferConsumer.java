package com.bok.krypto.messaging.consumer;

import com.bok.krypto.model.Order;
import com.bok.krypto.model.Transfer;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransferConsumer {


    @JmsListener(destination = "${active-mq.transfers-queue}")
    public void onTransferMessage(Transfer transfer) {
        log.info("Received Message: " + transfer.toString());
    }

}