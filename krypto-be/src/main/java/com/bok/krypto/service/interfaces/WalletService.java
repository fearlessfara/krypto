package com.bok.krypto.service.interfaces;

import com.bok.krypto.integration.internal.dto.WalletDeleteRequestDTO;
import com.bok.krypto.integration.internal.dto.WalletDeleteResponseDTO;
import com.bok.krypto.integration.internal.dto.WalletInfoDTO;
import com.bok.krypto.integration.internal.dto.WalletsDTO;
import com.bok.krypto.integration.internal.dto.WalletRequestDTO;
import com.bok.krypto.integration.internal.dto.WalletResponseDTO;

public interface WalletService {

    WalletResponseDTO create(Long userId, WalletRequestDTO walletRequestDTO);

    WalletDeleteResponseDTO delete(Long userId, WalletDeleteRequestDTO walletDeleteRequestDTO);

    WalletInfoDTO info(Long userId, String walletId);

    WalletsDTO wallets(Long userId);
}
