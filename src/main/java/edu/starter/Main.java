package edu.starter;

import java.util.Scanner;

public final class Main {
    public static void main(String[] args) {
        System.out.println("=== Welcome to the Battle Adventure! ===");

        Scanner scanner = new Scanner(System.in);
        Game game = new Game();

        System.out.println("\n--- Encounter 1 vs Enemy 1 (30 HP) ---");
        game.gameStatus();

        while (!game.isGameOver() && !game.isFirstEncounterWon()) {
            printMenu();

            int choice = ConsoleChoice.read(scanner, 1, 4);
            if (choice == -1) {
                if (!scanner.hasNextLine()) {
                    System.out.println("\nInput ended.");
                    return;
                }
                System.out.println("Invalid input! Please enter a number between 1 and 4.");
                continue;
            }

            executeAction(game, choice);
        }

        if (game.isGameOver()) {
            game.showGameOver();
            System.out.println("Thanks for playing!");
            return;
        }

        // Client responsibility: check the precondition before calling reward().
        if (!game.canClaimReward()) {
            System.out.println("Thanks for playing!");
            return;
        }

        Player p = game.getPlayer();
        int hpBefore = p.getHP();
        int resBefore = p.getResource();
        int atkBefore = p.getAttackDamage();

        if (!game.reward(scanner)) {
            System.out.println("\nInput ended.");
            return;
        }

        // Client verifies the reward postcondition.
        boolean recovered = p.getHP() == Math.min(Combatant.MAX_HP, hpBefore + 25)
                && p.getAttackDamage() == atkBefore;
        boolean powered = p.getHP() == hpBefore
                && p.getAttackDamage() == atkBefore + 2;
        Checks.that(recovered || powered, "reward postcondition violated");
        Checks.that(p.getResource() == resBefore, "reward must not change resource");
        Checks.that(p.isRewardClaimed(), "reward must be consumed");

        System.out.println("\n--- Encounter 2 vs Enemy 2 (45 HP) ---");
        game.gameStatus();

        while (!game.isGameOver()) {
            printMenu();

            int choice = ConsoleChoice.read(scanner, 1, 4);
            if (choice == -1) {
                if (!scanner.hasNextLine()) {
                    System.out.println("\nInput ended.");
                    return;
                }
                System.out.println("Invalid input! Please enter a number between 1 and 4.");
                continue;
            }

            executeAction(game, choice);
        }

        game.showGameOver();
        System.out.println("Thanks for playing!");
    }

    private static void printMenu() {
        System.out.println("\nChoose your action:");
        System.out.println("1. Attack");
        System.out.println("2. Special (Costs 3 resources, deals 15 damage)");
        System.out.println("3. Recover (Restores resources)");
        System.out.println("4. Defend (Take damage)");
        System.out.print("Enter your choice (1-4): ");
    }

    private static void executeAction(Game game, int choice) {
        switch (choice) {
            case 1:
                game.attack();
                break;
            case 2:
                if (game.canSpecial()) {
                    game.special();
                } else {
                    System.out.println("Not enough resource for Special (needs 3). Choose another action.");
                }
                break;
            case 3:
                game.recover();
                break;
            case 4:
                game.defend();
                break;
            default:
                System.out.println("Invalid choice!");
                break;
        }
        checkInvariants(game);
    }

    /** Client-side check of the class invariants after every action. */
    private static void checkInvariants(Game game) {
        Player p = game.getPlayer();
        Checks.that(p.getHP() >= 0 && p.getHP() <= Combatant.MAX_HP, "HP out of bounds");
        Checks.that(p.getResource() >= 0 && p.getResource() <= Player.MAX_RESOURCE,
                "resource out of bounds");
    }
}