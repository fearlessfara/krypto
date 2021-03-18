package com.bok.krypto.model;

import javax.persistence.*;

@Entity
@DiscriminatorValue("transaction")
public class Transaction extends Activity {

    @ManyToOne
    private Wallet wallet;

    @Column
    @Enumerated(EnumType.STRING)
    private Type type;

    public Transaction() {
        //hibernate
    }

    public Transaction(Type type, Status status) {
        super();
        this.type = type;
        this.status = status;
    }


    public enum Type {
        BUY,
        SELL
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
