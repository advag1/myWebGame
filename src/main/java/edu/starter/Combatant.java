package edu.starter;

public abstract class Combatant {
    public static final int MAX_HP = 100;
    protected int HP;
    protected String name;

    public Combatant() {
        this.HP = Combatant.MAX_HP;
        this.name = "A";
    }

    public Combatant(String name) {
        this.HP = Combatant.MAX_HP;
        this.name = name;
    }

    public Combatant(int HP) {
        this.HP = HP;
        this.name = "";
    }

    public Combatant(String name, int HP) {
        this.HP = HP;
        this.name = name;
    }

    public Combatant(int HP, String name) {
        this.HP = HP;
        this.name = name;
    }

    public int getHP() {
        return this.HP;
    }

    /**
     * Reduces HP by damage, never below 0.
     *
     * @pre 0 <= damage
     * @post getHP() == Math.max(0, $old.getHP() - damage)
     */
    public void takeDamage(int damage) {
        this.HP = Math.max(0, this.HP - damage);
    }

    /**
     * Restores HP by amount, never above MAX_HP.
     *
     * @pre 0 <= amount
     * @post getHP() == Math.min(MAX_HP, $old.getHP() + amount)
     */
    public void heal(int amount) {
        this.HP = Math.min(Combatant.MAX_HP, this.HP + amount);
    }

    public boolean isHpIllegal() {
        return this.HP <= 0;
    }

    public void combatantStatus() {
        System.out.println("Name: " + this.name + " | HP left: " + this.HP);
    }
}