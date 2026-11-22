package edu.starter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class WebServer {
    private static Game game;

    public static void main(String[] args) throws IOException {
        startNewGame();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", WebServer::handleStaticFiles);
        server.createContext("/api", WebServer::handleApi);
        server.start();

        System.out.println("האתר פועל! פתחי בדפדפן את הכתובת: http://localhost:8080");
    }

    private static String startNewGame() {
        return runWithCapturedLogs(() -> {
            System.out.println("=== Welcome to the Battle Adventure! ===");
            game = new Game();
            System.out.println("--- Encounter 1 vs Enemy 1 (30 HP) ---");
            game.gameStatus();
        });
    }

    private static void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        String contentType = path.endsWith(".css") ? "text/css; charset=UTF-8" : "text/html; charset=UTF-8";
        byte[] content = readStaticFile(path.substring(1));

        if (content == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }

    private static byte[] readStaticFile(String fileName) throws IOException {
        Path filePath = Paths.get("src/main/resources/static", fileName);
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        try (InputStream is = WebServer.class.getResourceAsStream("/static/" + fileName)) {
            return is != null ? is.readAllBytes() : null;
        }
    }

    private static void handleApi(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String action = (query != null && query.startsWith("action=")) ? query.substring(7) : "state";
        String logs = "";

        if ("new".equals(action)) {
            logs = startNewGame();
        } else {
            logs = runWithCapturedLogs(() -> executeJavaAction(action));
        }

        Player p = game.getPlayer();
        Enemy e = game.getCurrEnemy();
        int enemyMaxHP = "enemy1".equals(e.getName()) ? 30 : 45;

        String json = "{"
                + "\"playerHP\":" + p.getHP() + ","
                + "\"playerMaxHP\":" + Combatant.MAX_HP + ","
                + "\"playerResource\":" + p.getResource() + ","
                + "\"playerMaxResource\":" + Player.MAX_RESOURCE + ","
                + "\"playerAttack\":" + p.getAttackDamage() + ","
                + "\"enemyName\":\"" + escapeJson(e.getName()) + "\","
                + "\"enemyHP\":" + e.getHP() + ","
                + "\"enemyMaxHP\":" + enemyMaxHP + ","
                + "\"isFirstEncounterWon\":" + game.isFirstEncounterWon() + ","
                + "\"canClaimReward\":" + game.canClaimReward() + ","
                + "\"canSpecial\":" + game.canSpecial() + ","
                + "\"isGameOver\":" + game.isGameOver() + ","
                + "\"playerLost\":" + p.isHpIllegal() + ","
                + "\"logs\":\"" + escapeJson(logs) + "\""
                + "}";

        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static void executeJavaAction(String action) {
        switch (action) {
            case "attack":
                if (!game.isGameOver() && !game.canClaimReward()) game.attack();
                break;
            case "special":
                if (!game.isGameOver() && !game.canClaimReward() && game.canSpecial()) game.special();
                break;
            case "recover":
                if (!game.isGameOver() && !game.canClaimReward()) game.recover();
                break;
            case "defend":
                if (!game.isGameOver() && !game.canClaimReward()) game.defend();
                break;
            case "reward1":
                if (game.canClaimReward()) game.reward(new Scanner("1\n"));
                break;
            case "reward2":
                if (game.canClaimReward()) game.reward(new Scanner("2\n"));
                break;
        }
        if (game.isGameOver()) {
            game.showGameOver();
        }
    }

    private static String runWithCapturedLogs(Runnable task) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            task.run();
        } finally {
            System.setOut(originalOut);
        }
        String output = buffer.toString(StandardCharsets.UTF_8);
        originalOut.print(output);
        return output;
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\r", "")
                   .replace("\n", "\\n");
    }
}