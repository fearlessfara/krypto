package com.bok.krypto.utils;

import com.bok.krypto.helper.KryptoHelper;
import com.bok.krypto.helper.TransferHelper;
import com.bok.krypto.model.HistoricalData;
import com.bok.krypto.model.Krypto;
import com.bok.krypto.model.Account;
import com.bok.krypto.model.Wallet;
import com.bok.krypto.repository.*;
import com.bok.krypto.service.interfaces.TransferService;
import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
public class ModelTestUtils {

    public static final Random random = new Random();
    public static final Faker faker = new Faker();
    @Autowired
    HistoricalDataRepository historicalDataRepository;
    @Autowired
    KryptoHelper kryptoHelper;
    @Autowired
    KryptoRepository kryptoRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    TransferHelper transferHelper;
    @Autowired
    TransferService transferService;
    @Autowired
    TransferRepository transferRepository;

    private static Instant between(Instant startInclusive, Instant endExclusive) {
        long startSeconds = startInclusive.getEpochSecond();
        long endSeconds = endExclusive.getEpochSecond();
        long random = ThreadLocalRandom
                .current()
                .nextLong(startSeconds, endSeconds);

        return Instant.ofEpochSecond(random);
    }

    public synchronized void populateDB() {
        kryptoRepository.save(new Krypto("Bitcoin", "BTC", new BigDecimal(50000)));
        kryptoRepository.save(new Krypto("Ethereum", "ETH", new BigDecimal(1800)));
        kryptoRepository.save(new Krypto("Litecoin", "LTC", new BigDecimal(1800)));
        kryptoRepository.save(new Krypto("Cardano", "ADA", new BigDecimal(1800)));
        kryptoRepository.save(new Krypto("DogeCoin", "DOGE", new BigDecimal(1800)));

    }

    public synchronized void clearAll() {
        historicalDataRepository.deleteAll();
        transactionRepository.deleteAll();
        transferRepository.deleteAll();
        walletRepository.deleteAll();
        kryptoRepository.deleteAll();
        accountRepository.deleteAll();

    }

    public void generateHistoricalDataForeachKrypto() {
        List<Krypto> kryptos = kryptoRepository.findAll();
        for (Krypto k : kryptos) {
            generateRandomHistoricalData(k, Instant.parse("2007-12-03T10:15:30.00Z"), Instant.now(), 30);
        }
    }

    public Account createUser(Long userId) {
        Account u = new Account();
        u.setId(userId);
        return accountRepository.save(u);
    }

    public Account createUser() {
        //Id for this class should reflect parent User ids
        Account u = new Account();
        u.setId(randomID());
        log.info("Created user with id: {}", u.getId());
        return accountRepository.save(u);
    }

    public Long randomID() {
        return System.nanoTime();
    }

    public Long randomLong() {
        return random.nextLong();
    }

    public Wallet createWallet(Account account, Krypto krypto, BigDecimal baseAmount) {
        Wallet w = new Wallet();
        w.setKrypto(krypto);
        w.setUser(account);
        w.setAvailableAmount(baseAmount);
        w = walletRepository.saveAndFlush(w);
        w.setStatus(Wallet.Status.CREATED);
        return walletRepository.save(w);
    }

    public Krypto getKrypto(String krypto) {
        return kryptoRepository.findBySymbol(krypto).get();
    }

    public Krypto getRandomKrypto() {
        List<Krypto> kryptoSet = kryptoRepository.findAll();
        long random = ThreadLocalRandom.current().nextLong(0, kryptoSet.size());
        return kryptoSet.get(Math.toIntExact(random));
    }

    public void generateRandomHistoricalData(Krypto krypto, Instant start, Instant end, Integer numberOfRecords) {
        List<HistoricalData> list = new ArrayList<>();
        for (long c = 0; c < numberOfRecords; c++) {
            HistoricalData datum = new HistoricalData();
            datum.setRecordTimestamp(between(start, end));
            datum.setPrice(faker.number().randomDouble(5, 100, 250));
            datum.setKrypto(krypto);
            list.add(datum);
        }
        historicalDataRepository.saveAll(list);
    }

    public void generateDatabaseRandomNoise(Integer numOfKryptos, Integer recordsPerKrypto) {
        for (int c = 0; c < numOfKryptos; c++) {
            //User u = createUser();
            Krypto k = getRandomKrypto();
            generateRandomHistoricalData(k, Instant.EPOCH, Instant.now(), recordsPerKrypto);
            //Wallet w = createWallet(u, k.getSymbol());
        }
    }

    @SneakyThrows
    public void await() {
        int times = 0;
        int maxTimes = 100;
        while (transferRepository.countPendingTransfers() > 0 &&
                walletRepository.countPendingWallets() > 0 &&
                transactionRepository.countPendingTransactions() > 0 &&
                times < maxTimes) {
            Thread.sleep(500);
            times++;
        }
    }


}
