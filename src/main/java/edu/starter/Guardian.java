package edu.starter;

public class Guardian extends Player {
    public Guardian() {
        super(130, 5, "Guardian");
        this.attackDamage = 8;
    }

    @Override
    public int getDefendBlock() {
        return 7;
    }

    @Override
    public void receiveRecovery() {
        receiveRecovery(130);
    }
}