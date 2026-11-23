package edu.starter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StarterTest {
    
    @Test void clampingIsCorrect(){assertEquals(10,Checks.within(12,0,10));}
    @Test void missingNumberIsRejected(){assertEquals(-1,ConsoleChoice.read(new java.util.Scanner("abc\n"),1,4));}


    @Test
    void testStandardPlayerInitialValues() {
        Player p = new Player();
        assertEquals(100, p.getHP());
        assertEquals(5, p.getResource());
        assertEquals(10, p.getAttackDamage());
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
    void testTakeDamageBoundaryZero() {
        Player p = new Player(10, 5);
        p.takeDamage(15);
        assertEquals(0, p.getHP());
        assertTrue(p.isHpIllegal());
    }

    @Test
    void testHealBoundaryCap() {
        Player p = new Player(90, 5);
        p.heal(25);
        assertEquals(100, p.getHP());
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
    void testRewardPowerUpgrade() {
        Player p = new Player();
        int initialAtk = p.getAttackDamage();
        p.receivePower();
        assertEquals(initialAtk + 2, p.getAttackDamage());
        assertTrue(p.isRewardClaimed());
    }


    @Test
    void testGameIntegrationEncounterFlow() {
        Game game = new Game();
        assertFalse(game.isFirstEncounterWon());
        
        game.basicAttack(10);
        game.basicAttack(10);
        game.basicAttack(10);
        
        assertTrue(game.isFirstEncounterWon());
        assertTrue(game.canClaimReward());
    }


    @Test
    void testMetamorphicPowerUpgrade() {
        Player basePlayer = new Player(100, 5);
        Player upgradedPlayer = new Player(100, 5);

        int initialAtk = basePlayer.getAttackDamage();
        upgradedPlayer.receivePower();

        assertEquals(initialAtk + 2, upgradedPlayer.getAttackDamage());
        assertEquals(basePlayer.getAttackDamage() + 2, upgradedPlayer.getAttackDamage());
    }
}