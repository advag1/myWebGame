package edu.starter;

import java.util.Scanner;

public final class Main {
    public static void main(String[] args) {
        System.out.println("=== Welcome to the Battle Adventure! ===");
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose your character profile:");
        System.out.println("1. Standard (100 HP, Attack: 10, Defend: 5)");
        System.out.println("2. Guardian (130 HP, Attack: 8, Defend: 7)");
        System.out.println("3. Striker  (80 HP, Attack: 12, Defend: 5)");
        System.out.print("Enter choice (1-3): ");

        int profileChoice = ConsoleChoice.read(scanner, 1, 3);
        Player selectedPlayer;
        switch (profileChoice) {
            case 2 -> selectedPlayer = new Guardian();
            case 3 -> selectedPlayer = new Striker();
            default -> selectedPlayer = new Player();
        }

        Game game = new Game(selectedPlayer);

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
        System.out.println("4. Defend");
        System.out.print("Enter your choice (1-4): ");
    }

    private static void executeAction(Game game, int choice) {
        switch (choice) {
            case 1 -> game.attack();
            case 2 -> {
                if (game.canSpecial()) game.special();
                else System.out.println("Not enough resource for Special (needs 3).");
            }
            case 3 -> game.recover();
            case 4 -> game.defend();
            default -> System.out.println("Invalid choice!");
        }
        checkInvariants(game);
    }

    private static void checkInvariants(Game game) {
        Player p = game.getPlayer();
        Checks.that(p.getHP() >= 0 && p.getHP() <= 130, "HP out of bounds");
        Checks.that(p.getResource() >= 0 && p.getResource() <= Player.MAX_RESOURCE, "resource out of bounds");
    }
}