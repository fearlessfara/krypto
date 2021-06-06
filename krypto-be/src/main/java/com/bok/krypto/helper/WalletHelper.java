package com.bok.krypto.helper;

import com.bok.krypto.core.AddressGenerator;
import com.bok.krypto.exception.InvalidRequestException;
import com.bok.krypto.exception.KryptoNotFoundException;
import com.bok.krypto.exception.TransactionException;
import com.bok.krypto.exception.WalletAlreadyExistsException;
import com.bok.krypto.exception.WalletNotFoundException;
import com.bok.krypto.integration.internal.dto.WalletDeleteRequestDTO;
import com.bok.krypto.integration.internal.dto.WalletDeleteResponseDTO;
import com.bok.krypto.integration.internal.dto.WalletInfoDTO;
import com.bok.krypto.integration.internal.dto.WalletRequestDTO;
import com.bok.krypto.integration.internal.dto.WalletResponseDTO;
import com.bok.krypto.integration.internal.dto.WalletsDTO;
import com.bok.krypto.messaging.internal.messages.WalletMessage;
import com.bok.krypto.model.Account;
import com.bok.krypto.model.Wallet;
import com.bok.krypto.repository.WalletRepository;
import com.bok.krypto.service.bank.BankService;
import com.bok.krypto.service.interfaces.MessageService;
import com.bok.parent.integration.message.EmailMessage;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class WalletHelper {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    TransactionHelper transactionHelper;

    @Autowired
    AccountHelper accountHelper;

    @Autowired
    KryptoHelper kryptoHelper;

    @Autowired
    MessageService messageService;

    @Autowired
    MarketHelper marketHelper;

    @Autowired
    BankService bankService;

    @Autowired
    AddressGenerator addressGenerator;


    public Wallet findByPublicId(String publicId) {
        return walletRepository.findByAddress(publicId).orElseThrow(() -> new WalletNotFoundException("wallet not found"));
    }

    public Boolean existsByPublicId(String publicId) {
        return walletRepository.existsByAddress(publicId);
    }

    public Wallet findByAccountIdAndSymbol(Long userId, String symbol) {
        return walletRepository.findByAccount_IdAndKrypto_Symbol(userId, symbol).orElseThrow(() -> new WalletNotFoundException("wallet not found"));

    }

    @Transactional
    public BigDecimal withdraw(Wallet wallet, BigDecimal amount) {
        if (wallet.getAvailableAmount().compareTo(amount) < 0) {
            throw new TransactionException("not enough funds to perform the withdrawal");
        }
        log.info("withdrawing {} from wallet {}", amount, wallet.getAddress());
        BigDecimal newBalance = wallet.getAvailableAmount().subtract(amount);
        wallet.setAvailableAmount(newBalance);
        walletRepository.saveAndFlush(wallet);
        log.info("wallet {} balance: {}", wallet.getAddress(), newBalance);
        return amount;
    }

    @Transactional
    public BigDecimal deposit(Wallet wallet, BigDecimal amount) {
        Wallet w = findByPublicId(wallet.getAddress());
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("Cannot deposit negative amounts");
        }
        log.info("depositing {} {} to wallet {}", amount, w.getKrypto().getSymbol(), wallet.getAddress());
        BigDecimal newBalance = w.getAvailableAmount().add(amount);
        w.setAvailableAmount(newBalance);
        walletRepository.saveAndFlush(w);
        log.info("wallet {} balance: {}", wallet.getAddress(), newBalance);
        return amount;
    }

    public Boolean existsByUserIdAndSymbol(Long userId, String symbol) {
        return walletRepository.existsByAccount_IdAndKrypto_Symbol(userId, symbol);
    }

    public WalletResponseDTO createWallet(Long userId, WalletRequestDTO requestDTO) {
        if (!kryptoHelper.existsBySymbol(requestDTO.symbol)) {
            throw new KryptoNotFoundException("This krypto doesn't exists");
        }
        if (walletRepository.existsByAccount_IdAndKrypto_Symbol(userId, requestDTO.symbol)) {
            throw new WalletAlreadyExistsException("A wallet with the same Krypto exists for this user");
        }
        Wallet w = new Wallet();
        w = walletRepository.save(w);
        WalletMessage walletMessage = new WalletMessage();
        walletMessage.id = w.getId();
        walletMessage.accountId = userId;
        walletMessage.symbol = requestDTO.symbol;
        messageService.sendWallet(walletMessage);
        return new WalletResponseDTO(WalletResponseDTO.Status.ACCEPTED);
    }

    public void handleMessage(WalletMessage walletMessage) {
        Wallet w = walletRepository.findById(walletMessage.id)
                .orElseThrow(() -> new RuntimeException("This wallet should have been pre-persisted."));
        Account account = accountHelper.findById(walletMessage.accountId);
        w.setAccount(account);
        w.setAddress(addressGenerator.generateBitcoinAddress());
        w.setKrypto(kryptoHelper.findBySymbol(walletMessage.symbol));
        walletRepository.save(w);
        messageService.sendEmail(emailWalletCreation(w, account));
    }

    private EmailMessage emailWalletCreation(Wallet wallet, Account account) {
        EmailMessage email = new EmailMessage();
        email.subject = "BOK - Wallet creation";
        email.body = "Your wallet for Krypto " + wallet.getKrypto().getSymbol() + " has been created.";
        email.to = accountHelper.getEmailByAccountId(account.getId());
        return email;
    }

    public Boolean hasSufficientBalance(Long accountId, String symbol, BigDecimal amount) {
        return walletRepository.existsByAccount_IdAndKrypto_SymbolAndAvailableAmountGreaterThanEqual(accountId, symbol, amount);
    }

    public WalletDeleteResponseDTO delete(Long accountId, WalletDeleteRequestDTO deleteRequestDTO) {
        Preconditions.checkArgument(accountHelper.existsById(accountId));
        Preconditions.checkArgument(walletRepository.existsByAccount_IdAndKrypto_Symbol(accountId, deleteRequestDTO.symbol));
        Preconditions.checkNotNull(deleteRequestDTO.destinationIBAN);
        Account account = accountHelper.findById(accountId);
        Wallet wallet = findByAccountIdAndSymbol(accountId, deleteRequestDTO.symbol);
        marketHelper.emptyWallet(account, wallet);
        walletRepository.delete(wallet);
        String to, subject, text;
        to = accountHelper.getEmailByAccountId(account.getId());
        subject = "BOK - Wallet deletion";
        text = "Your wallet " + wallet.getKrypto().getSymbol() + " has been deleted.";
        sendMarketEmail(subject, to, text);
        return new WalletDeleteResponseDTO(wallet.getAddress());
    }

    public void sendMarketEmail(String subject, String email, String text) {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.subject = subject;
        emailMessage.to = email;
        emailMessage.body = text;
        messageService.sendEmail(emailMessage);
    }

    public WalletsDTO wallets(Long accountId) {
        Preconditions.checkArgument(accountHelper.existsById(accountId));
        List<Wallet> wallets = walletRepository.findByAccount_Id(accountId);
        WalletsDTO walletsDTO = new WalletsDTO();
        walletsDTO.wallets = new ArrayList<>();
        for (Wallet w : wallets) {
            walletsDTO.wallets.add(getInfoFromWallet(w));
        }
        return walletsDTO;
    }

    private WalletInfoDTO getInfoFromWallet(Wallet wallet) {
        WalletInfoDTO info = new WalletInfoDTO();
        info.availableAmount = wallet.getAvailableAmount();
        info.symbol = wallet.getKrypto().getSymbol();
        info.creationTimestamp = wallet.getCreationTime();
        info.updateTimestamp = wallet.getUpdateTime();
        return info;
    }

    public WalletInfoDTO info(Long accountId, String symbol, LocalDate startDate, LocalDate endDate) {
        Preconditions.checkArgument(accountHelper.existsById(accountId));

        Wallet wallet = findByAccountIdAndSymbol(accountId, symbol);
        return getInfoFromWallet(wallet, startDate, endDate);
    }

    //TODO complete with balance info
    private WalletInfoDTO getInfoFromWallet(Wallet wallet, LocalDate startDate, LocalDate endDate) {
        WalletInfoDTO info = new WalletInfoDTO();
        info.availableAmount = wallet.getAvailableAmount();
        info.symbol = wallet.getKrypto().getSymbol();
        info.creationTimestamp = wallet.getCreationTime();
        info.updateTimestamp = wallet.getUpdateTime();
        transactionHelper.findByWalletIdAndDateBetween(wallet, startDate, endDate);
        return info;
    }

}
