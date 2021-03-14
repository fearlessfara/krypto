package com.bok.krypto.service;

import com.bok.krypto.helper.ExchangeHelper;
import com.bok.krypto.model.Krypto;
import com.bok.krypto.service.interfaces.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    ExchangeHelper exchangeHelper;

    @Override
    public void transfer(UUID from, UUID to, BigDecimal amount) {
        //exchangeHelper.transfer(from, to, amount);
    }


}