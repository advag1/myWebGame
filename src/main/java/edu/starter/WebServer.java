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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class WebServer {
    private static final Map<String, Game> games = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", WebServer::handleStaticFiles);
        server.createContext("/api", WebServer::handleApi);
        server.start();

        System.out.println("האתר פועל! פתחי בדפדפן את הכתובת: http://localhost:8080");
    }

    private static Game createGameForId(String gameId, String profile, StringBuilder logOut) {
        synchronized (WebServer.class) {
            Player player = switch (profile.toLowerCase()) {
                case "guardian" -> new Guardian();
                case "striker" -> new Striker();
                default -> new Player();
            };
            Game newGame = new Game(player);
            games.put(gameId, newGame);
            logOut.append(runWithCapturedLogs(() -> {
                System.out.println("=== Welcome to the Battle Adventure! (ID: " + gameId + ", Profile: " + player.getName() + ") ===");
                System.out.println("--- Encounter 1 vs Enemy 1 (30 HP) ---");
                newGame.gameStatus();
            }));
            return newGame;
        }
    }

    private static void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        String contentType = "text/html; charset=UTF-8";
        if (path.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (path.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        }

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
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String gameId = params.getOrDefault("gameId", "default");
        String action = params.getOrDefault("action", "state");
        String profile = params.getOrDefault("profile", "standard");

        StringBuilder logs = new StringBuilder();
        Game game = games.get(gameId);

        if (game == null || "new".equals(action)) {
            game = createGameForId(gameId, profile, logs);
        } else {
            final Game currentGame = game;
            synchronized (currentGame) {
                logs.append(runWithCapturedLogs(() -> executeJavaAction(currentGame, action)));
            }
        }

        Player p = game.getPlayer();
        Enemy e = game.getCurrEnemy();
        int enemyMaxHP = "enemy1".equals(e.getName()) ? 30 : 45;

        int playerMaxHP = (p instanceof Guardian) ? 130 : (p instanceof Striker ? 80 : 100);

        String json = "{"
                + "\"playerHP\":" + p.getHP() + ","
                + "\"playerMaxHP\":" + playerMaxHP + ","
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
                + "\"logs\":\"" + escapeJson(logs.toString()) + "\""
                + "}";

        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static void executeJavaAction(Game game, String action) {
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

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
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