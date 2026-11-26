package edu.starter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StarterTest {
    
    @Test void clampingIsCorrect() { assertEquals(10, Checks.within(12, 0, 10)); }
    @Test void missingNumberIsRejected() { assertEquals(-1, ConsoleChoice.read(new java.util.Scanner("abc\n"), 1, 4)); }

    @Test
    void testStandardPlayerInitialValues() {
        Player p = new Player();
        assertEquals(100, p.getHP());
        assertEquals(5, p.getResource());
        assertEquals(10, p.getAttackDamage());
        assertEquals(5, p.getDefendBlock());
        assertFalse(p.isRewardClaimed());
    }

    @Test
    void testGuardianInitialValues() {
        Guardian g = new Guardian();
        assertEquals(130, g.getHP());
        assertEquals(5, g.getResource());
        assertEquals(8, g.getAttackDamage());
        assertEquals(7, g.getDefendBlock());
    }

    @Test
    void testStrikerInitialValues() {
        Striker s = new Striker();
        assertEquals(80, s.getHP());
        assertEquals(5, s.getResource());
        assertEquals(12, s.getAttackDamage());
        assertEquals(5, s.getDefendBlock());
    }

    @Test
    void testGuardianDefendsHit() {
        Guardian g = new Guardian();
        Game game = new Game(g);
        game.defend();
        assertEquals(127, g.getHP());
    }

    @Test
    void testStrikerDefendsHit() {
        Striker s = new Striker();
        Game game = new Game(s);
        game.defend();
        assertEquals(75, s.getHP());
    }

    @Test
    void testTakeDamageBoundaryZero() {
        Guardian g = new Guardian();
        g.takeDamage(129);
        assertEquals(1, g.getHP());
        assertFalse(g.isHpIllegal());

        g.takeDamage(1);
        assertEquals(0, g.getHP());
        assertTrue(g.isHpIllegal());

        g.takeDamage(200);
        assertEquals(0, g.getHP());
    }

    @Test
    void testStrikerHealBoundaryCap() {
        Striker s = new Striker();
        s.takeDamage(1);
        assertEquals(79, s.getHP());

        s.receiveRecovery();
        assertEquals(80, s.getHP());
        assertTrue(s.isRewardClaimed());
    }

    @Test
    void testRewardPowerUpgrade() {
        Guardian g = new Guardian();
        int initialAtk = g.getAttackDamage();
        g.receivePower();
        assertEquals(initialAtk + 2, g.getAttackDamage());
        assertTrue(g.isRewardClaimed());
    }

    @Test
    void testSpecialAttackResourceLegality() {
        Game game = new Game();
        assertTrue(game.canSpecial());
        game.special();
        assertEquals(2, game.getPlayer().getResource());
        assertEquals(15, game.getCurrEnemy().getHP());

        assertFalse(game.canSpecial());
    }

    @Test
    void testIndependentEnemies() {
        Game game = new Game();
        game.basicAttack(30);
        assertTrue(game.isFirstEncounterWon());
        game.startSecondEncounter();
        assertEquals(45, game.getCurrEnemy().getHP());
    }

    @Test
    void testResourceMaxCap() {
        Player p = new Player(100, 8);
        p.recover();
        assertEquals(10, p.getResource());
    }

    @Test
    void testGuardianRecoveryCap() {
        Guardian g = new Guardian();
        g.takeDamage(50);
        g.receiveRecovery();
        assertEquals(105, g.getHP());
        assertTrue(g.isRewardClaimed());
    }

    @Test
    void testGameIntegrationEncounterFlow() {
        Game game = new Game();
        assertFalse(game.isFirstEncounterWon());
        assertFalse(game.canClaimReward());
        
        game.basicAttack(10);
        game.basicAttack(10);
        game.basicAttack(10);
        
        assertTrue(game.isFirstEncounterWon());
        assertTrue(game.canClaimReward());

        game.getPlayer().applyReward(Reward.POWER);
        assertFalse(game.canClaimReward());
    }

    @Test
    void testMetamorphicPowerUpgrade() {
        Guardian before = new Guardian();
        Guardian upgraded = new Guardian();

        int maxHpBefore = before.getHP();
        upgraded.receivePower();

        assertEquals(before.getAttackDamage() + 2, upgraded.getAttackDamage());
        assertEquals(maxHpBefore, upgraded.getHP());
    }
}