package edu.starter;

import java.util.Scanner;

public final class SimulationRunner {

    public static void main(String[] args) {
        run("SCENARIO 1 (POWER)", new Game(), "2\n1\n1\n2\n");
        run("SCENARIO 2 (RECOVERY)", new Game(), "2\n1\n1\n1\n");
        run("SCENARIO 3 (INVALID REWARD THEN POWER, T4)", new Game(), "2\n1\n1\n9\n2\n");
        run("SCENARIO 4 (LOSS, T7)", new Game(), "4\n".repeat(20));
        run("T1: RECOVERY FROM HP 90", new Game(90, 5, 10, 5, 45), "1\n1\n");
        run("T2: RECOVERY AT HP 100", new Game(100, 5, 10, 5, 45), "1\n1\n");
        run("T9: FINISHING ATTACK ON ENEMY 2", new Game(100, 5, 10, 5, 10), "1\n1\n1\n");
        //for lesson 4
        run("GUARDIAN SCENARIO", new Game(new Guardian()), "2\n1\n1\n2\n");
        run("STRIKER SCENARIO", new Game(new Striker()), "2\n1\n1\n2\n");
    }

    private static void run(String title, Game game, String inputs) {
        System.out.println("\n=========================================");
        System.out.println("  " + title);
        System.out.println("=========================================");
        System.out.println("canClaimReward before fight: " + game.canClaimReward());

        Scanner scanner = new Scanner(inputs);
        game.gameStatus();

        while (!game.isGameOver() && !game.isFirstEncounterWon()) {
            if (!scanner.hasNextLine()) {
                System.out.println("Input ended.");
                return;
            }
            act(game, scanner.nextLine().trim());
        }

        if (game.isGameOver()) {
            game.showGameOver();
            System.out.println("No reward offered. canClaimReward: " + game.canClaimReward());
            return;
        }

        if (!game.canClaimReward() || !game.reward(scanner)) {
            System.out.println("Input ended.");
            return;
        }
        System.out.println("canClaimReward after reward: " + game.canClaimReward());

        System.out.println("\n--- Encounter 2 vs Enemy 2 ---");
        game.gameStatus();

        while (!game.isGameOver()) {
            if (!scanner.hasNextLine()) {
                System.out.println("Input ended.");
                return;
            }
            act(game, scanner.nextLine().trim());
        }
        game.showGameOver();
    }

    private static void act(Game game, String line) {
        System.out.println("\nEnter your choice (1-4): " + line);
        switch (line) {
            case "1" -> game.attack();
            case "2" -> {
                if (game.canSpecial()) {
                    game.special();
                } else {
                    System.out.println("Not enough resource for Special (needs 3). Choose another action.");
                }
            }
            case "3" -> game.recover();
            case "4" -> game.defend();
            default -> System.out.println("Invalid choice!");
        }
    }
}