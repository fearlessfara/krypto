package com.bok.krypto.service;

import com.bok.integration.krypto.dto.TransferInfoDTO;
import com.bok.integration.krypto.dto.TransferRequestDTO;
import com.bok.integration.krypto.dto.TransferResponseDTO;
import com.bok.krypto.helper.TransferHelper;
import com.bok.krypto.service.interfaces.TransferService;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.ValidationUtils;

@Service
public class TransferServiceImpl implements TransferService {

    @Autowired
    TransferHelper transferHelper;

    @Override
    public TransferResponseDTO transfer(Long accountId, TransferRequestDTO transferRequestDTO) {
        Preconditions.checkNotNull(accountId);
        Preconditions.checkNotNull(transferRequestDTO, "Request body was empty");
        Preconditions.checkNotNull(transferRequestDTO.symbol, "Symbol cannot be null");
        Preconditions.checkNotNull(transferRequestDTO.source, "Source wallet cannot be null");
        Preconditions.checkNotNull(transferRequestDTO.destination, "Destination wallet cannot be null");
        Preconditions.checkNotNull(transferRequestDTO.amount, "Amount cannot be null");
        return transferHelper.transfer(accountId, transferRequestDTO);
    }

    @Override
    public TransferInfoDTO transferInfo(Long accountId, String transferId) {
        return transferHelper.getTransferInfo(accountId, transferId);
    }
}
