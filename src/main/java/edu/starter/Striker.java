package edu.starter;

public class Striker extends Player {
    public Striker() {
        super(80, 5, "Striker");
        this.attackDamage = 12;
    }

    @Override
    public void receiveRecovery() {
        receiveRecovery(80);
    }
}