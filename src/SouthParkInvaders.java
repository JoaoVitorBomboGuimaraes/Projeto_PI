import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SouthParkInvaders extends Application {

    // --- Constantes ---
    public static final double LARGURA_TELA = 800;
    public static final double ALTURA_TELA = 600;
    public static final double VELOCIDADE_JOGADOR = 5.0;
    public static final double VELOCIDADE_TIRO = 8.0;
    public static final double VELOCIDADE_INIMIGO = 2.0;

    // --- Camadas ---
    private Pane root = new Pane();
    private Pane gamePane = new Pane();
    private Pane menuPane = new Pane();
    private Pane selectionPane = new Pane();
    private Pane gameOverPane = new Pane();

    // --- Variáveis ---
    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private int selectedSkinId = 1;

    private Boss boss;
    private List<BossBullet> bossBullets = new ArrayList<>();
    private boolean bossSpawned = false;

    private int level = 1;
    private Text levelText = new Text();

    private List<ImageView> lifeIcons = new ArrayList<>();
    private int lives = 3;
    private int score = 0;
    private Text scoreText = new Text();

    private long lastEnemySpawnTime = 0;
    private Random random = new Random();
    private HashMap<KeyCode, Boolean> keys = new HashMap<>();
    private AnimationTimer gameTimer;

    private ImageView gameOverView;
    private ImageView menuBackgroundView;
    private Button btnPlay;

    @Override
    public void start(Stage primaryStage) {
        root.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        gamePane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        menuPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        selectionPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        gameOverPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);

        // 1. Setup Game Pane
        Image bgImage = new Image(getClass().getResourceAsStream("/art/Background.jpg"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(LARGURA_TELA); bgView.setFitHeight(ALTURA_TELA);
        gamePane.getChildren().add(bgView);

        // UI Textos
        scoreText.setFont(Font.font("Verdana", 20)); scoreText.setFill(Color.WHITE);
        scoreText.setX(LARGURA_TELA - 150); scoreText.setY(30);
        gamePane.getChildren().add(scoreText);

        levelText.setFont(Font.font("Verdana", 20)); levelText.setFill(Color.YELLOW);
        levelText.setX(LARGURA_TELA / 2 - 40); levelText.setY(30); levelText.setText("Fase: 1");
        gamePane.getChildren().add(levelText);

        // 2. Setup Menu Pane
        Image menuImage = new Image(getClass().getResourceAsStream("/art/menu.png"));
        menuBackgroundView = new ImageView(menuImage);
        menuBackgroundView.setFitWidth(LARGURA_TELA); menuBackgroundView.setFitHeight(ALTURA_TELA);

        btnPlay = new Button("JOGAR");
        btnPlay.setPrefWidth(200); btnPlay.setPrefHeight(60);
        btnPlay.setLayoutX(LARGURA_TELA / 2 - 100); btnPlay.setLayoutY(ALTURA_TELA / 2 + 100);
        btnPlay.setStyle("-fx-background-color: #ff9900; -fx-text-fill: white; -fx-font-size: 24px; -fx-background-radius: 10;");
        btnPlay.setOnAction(event -> showSelectionScreen());

        menuPane.getChildren().addAll(menuBackgroundView, btnPlay);

        // 3. Setup Selection Pane
        setupSelectionScreenUI();

        // 4. Setup Game Over
        Image gameOverImage = new Image(getClass().getResourceAsStream("/art/gameover.png"));
        gameOverView = new ImageView(gameOverImage);
        gameOverView.setFitWidth(LARGURA_TELA); gameOverView.setFitHeight(ALTURA_TELA);
        gameOverPane.getChildren().add(gameOverView);

        root.getChildren().addAll(gamePane, gameOverPane, selectionPane, menuPane);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            if (menuPane.isVisible()) { if (event.getCode() == KeyCode.ENTER) showSelectionScreen(); }
            else if (gameOverPane.isVisible()) { if (event.getCode() == KeyCode.ENTER) showMenu(); }
            else if (gamePane.isVisible()) { keys.put(event.getCode(), true); }
        });

        scene.setOnKeyReleased(event -> {
            if (gamePane.isVisible()) {
                keys.put(event.getCode(), false);
                if (event.getCode() == KeyCode.SPACE) player.setImage(false);
            }
        });

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) { update(now); }
        };

        showMenu();
        primaryStage.setTitle("South Park Invaders");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupSelectionScreenUI() {
        Image bgSelection = new Image(getClass().getResourceAsStream("/art/Background.jpg"));
        ImageView bgViewSel = new ImageView(bgSelection);
        bgViewSel.setFitWidth(LARGURA_TELA); bgViewSel.setFitHeight(ALTURA_TELA);

        Text title = new Text("ESCOLHA SEU JOGADOR");
        title.setFont(Font.font("Verdana", 40)); title.setFill(Color.WHITE);
        title.setX(LARGURA_TELA / 2 - 250); title.setY(100);

        selectionPane.getChildren().addAll(bgViewSel, title);

        for (int i = 1; i <= 4; i++) {
            final int skinId = i;
            Image skinImg = new Image(getClass().getResourceAsStream("/art/skin" + i + ".png"));
            ImageView skinView = new ImageView(skinImg);

            skinView.setFitWidth(100); skinView.setFitHeight(100); skinView.setPreserveRatio(true);
            skinView.setX(100 + (i * 150)); skinView.setY(ALTURA_TELA / 2 - 50);

            skinView.setOnMouseEntered(e -> {
                skinView.setFitWidth(120); skinView.setFitHeight(120);
                skinView.setX(skinView.getX() - 10); skinView.setY(skinView.getY() - 10);
            });
            skinView.setOnMouseExited(e -> {
                skinView.setFitWidth(100); skinView.setFitHeight(100);
                skinView.setX(skinView.getX() + 10); skinView.setY(skinView.getY() + 10);
            });
            skinView.setOnMouseClicked(e -> {
                this.selectedSkinId = skinId;
                startGame();
            });
            selectionPane.getChildren().add(skinView);
        }
    }

    private void showMenu() {
        menuPane.setVisible(true); selectionPane.setVisible(false);
        gamePane.setVisible(false); gameOverPane.setVisible(false);
        menuPane.toFront(); gameTimer.stop();
    }

    private void showSelectionScreen() {
        menuPane.setVisible(false); selectionPane.setVisible(true);
        gamePane.setVisible(false); gameOverPane.setVisible(false);
        selectionPane.toFront();
    }

    private void startGame() {
        resetGameLogic();
        gamePane.setVisible(true); menuPane.setVisible(false);
        selectionPane.setVisible(false); gameOverPane.setVisible(false);
        gamePane.toFront();
        root.requestFocus();
        gameTimer.start();
    }

    private void gameOver() {
        gameTimer.stop();
        gameOverPane.setVisible(true); gamePane.setVisible(false); menuPane.setVisible(false);
        gameOverPane.toFront();
    }
    private void winGame() { gameOver(); }

    private void update(long now) {
        handleInput();
        checkLevelProgression();

        // --- LÓGICA DE FASES ATUALIZADA ---
        // Se NÃO estamos na Fase 5 E NÃO estamos na Fase 10, spawna inimigos
        if (level != 5 && level != 10) {
            // A velocidade aumenta conforme o nível. No nível 9 será insano!
            long intervaloSpawn = 1_000_000_000L - (level * 90_000_000L);
            // Limite de segurança para não spawnar rápido demais
            if (intervaloSpawn < 200_000_000L) intervaloSpawn = 200_000_000L;

            if (now - lastEnemySpawnTime > intervaloSpawn) {
                spawnEnemy();
                lastEnemySpawnTime = now;
            }
            updateEnemies();
        }
        else {
            // Se for Fase 5 ou Fase 10, roda lógica do Boss
            updateBoss();
        }

        updateBullets();
        updateBossBullets();
        checkCollisions();
        updateUI();
    }

    private void checkLevelProgression() {
        // Bloqueia progressão automática durante os chefões
        if (level == 5 || level == 10) return;

        // A cada 500 pontos sobe de nível
        int targetLevel = (score / 500) + 1;

        // Se passou do nível atual e não é boss, sobe
        if (targetLevel > level) {
            level = targetLevel;
            // Limita o nível máximo a 10
            if (level > 10) level = 10;
        }
    }

    private void handleInput() {
        if (keys.getOrDefault(KeyCode.LEFT, false) && player.getX() > 0) player.moveLeft();
        if (keys.getOrDefault(KeyCode.RIGHT, false) && player.getX() < LARGURA_TELA - player.getLargura()) player.moveRight();
        if (keys.getOrDefault(KeyCode.SPACE, false)) {
            player.setImage(true); shoot(); keys.put(KeyCode.SPACE, false);
            PauseTransition delay = new PauseTransition(Duration.millis(200));
            delay.setOnFinished(e -> player.setImage(false)); delay.play();
        }
    }

    private void updateEnemies() {
        enemies.removeIf(enemy -> {
            enemy.update();
            if (enemy.getY() > ALTURA_TELA) {
                gamePane.getChildren().remove(enemy.getView()); perderVida(); return true;
            }
            return false;
        });
    }

    private void updateBoss() {
        if (!bossSpawned) {
            // Limpa inimigos da tela antes de trazer o boss
            for (Enemy e : enemies) gamePane.getChildren().remove(e.getView()); enemies.clear();

            // --- CRIAÇÃO DO BOSS ---
            // Se for nível 10, é Super Boss (true). Se for 5, é normal (false).
            boolean isSuper = (level == 10);
            boss = new Boss(LARGURA_TELA / 2 - 100, 50, isSuper);

            gamePane.getChildren().add(boss.getView());
            bossSpawned = true;
        }

        if (boss != null && boss.getHp() > 0) {
            boss.update();
            // Super Boss atira mais rápido (chance 1 em 40) vs Normal (1 em 60)
            int chanceTiro = (level == 10) ? 40 : 60;
            if (random.nextInt(chanceTiro) == 0) shootBossBullet();
        }
        else if (boss != null && boss.getHp() <= 0) {
            // Boss Morreu!
            gamePane.getChildren().remove(boss.getView());

            if (level == 5) {
                // Passou do primeiro boss, vai para fase 6
                level = 6;
                boss = null;
                bossSpawned = false;
                score += 1000; // Bônus por matar o boss
            } else {
                // Passou do Super Boss (Fase 10), Fim de Jogo (Vitória)
                winGame();
            }
        }
    }

    private void shootBossBullet() {
        BossBullet bBullet = new BossBullet(boss.getX() + boss.getLargura()/2 - 15, boss.getY() + boss.getAltura());
        bossBullets.add(bBullet); gamePane.getChildren().add(bBullet.getView());
    }

    private void updateBossBullets() {
        bossBullets.removeIf(b -> {
            b.update();
            boolean fora = b.getY() > ALTURA_TELA;
            if (fora) gamePane.getChildren().remove(b.getView());
            return fora;
        });
    }

    private void updateBullets() {
        bullets.removeIf(bullet -> {
            bullet.update();
            boolean fora = bullet.getY() < 0;
            if (fora) gamePane.getChildren().remove(bullet.getView());
            return fora;
        });
    }

    private void shoot() {
        Bullet bullet = new Bullet(0, 0);
        bullet.setX(player.getX() + (player.getLargura() / 2) - (bullet.getLargura() / 2));
        bullet.setY(player.getY() - bullet.getAltura() + 5); bullet.render();
        bullets.add(bullet); gamePane.getChildren().add(bullet.getView());
    }

    private void spawnEnemy() {
        double w = 85; double x = random.nextDouble() * (LARGURA_TELA - w); double y = -(random.nextDouble() * 100 + 50);
        Enemy enemy = new Enemy(x, y, random.nextInt(4) + 1);
        enemies.add(enemy); gamePane.getChildren().add(enemy.getView());
    }

    private void checkCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<BossBullet> bossBulletsToRemove = new ArrayList<>();

        // Lógica: Inimigos Normais (Fases que NÃO são 5 nem 10)
        if (level != 5 && level != 10) {
            for (Bullet bullet : bullets) {
                for (Enemy enemy : enemies) {
                    if (bullet.getView().getBoundsInParent().intersects(enemy.getView().getBoundsInParent())) {
                        bulletsToRemove.add(bullet); enemiesToRemove.add(enemy);
                        playExplosion(enemy.getX(), enemy.getY()); score += 100;
                    }
                }
            }
        }
        // Lógica: Boss (Fase 5 ou 10)
        else if (boss != null && boss.getHp() > 0) {
            for (Bullet bullet : bullets) {
                if (bullet.getView().getBoundsInParent().intersects(boss.getView().getBoundsInParent())) {
                    bulletsToRemove.add(bullet); boss.tomarDano();
                    playExplosion(bullet.getX(), bullet.getY());
                    if (boss.getHp() <= 0) {
                        playExplosion(boss.getX(), boss.getY());
                        // Pontos são dados no updateBoss quando ele morre
                    }
                }
            }
        }

        for (BossBullet bb : bossBullets) {
            if (bb.getView().getBoundsInParent().intersects(player.getView().getBoundsInParent())) {
                bossBulletsToRemove.add(bb);
                playExplosion(player.getX(), player.getY());
                perderVida();
            }
        }

        bullets.removeAll(bulletsToRemove); enemies.removeAll(enemiesToRemove);
        bossBullets.removeAll(bossBulletsToRemove);

        bulletsToRemove.forEach(b -> gamePane.getChildren().remove(b.getView()));
        enemiesToRemove.forEach(e -> gamePane.getChildren().remove(e.getView()));
        bossBulletsToRemove.forEach(b -> gamePane.getChildren().remove(b.getView()));
    }

    private void playExplosion(double x, double y) {
        Image img = new Image(getClass().getResourceAsStream("/art/explosion.GIF"));
        ImageView view = new ImageView(img); view.setX(x); view.setY(y);
        gamePane.getChildren().add(view);
        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(event -> gamePane.getChildren().remove(view)); delay.play();
    }

    private void setupUI() {
        for (ImageView icon : lifeIcons) gamePane.getChildren().remove(icon); lifeIcons.clear();
        Image img = new Image(getClass().getResourceAsStream("/art/Vida.png"));
        for (int i = 0; i < lives; i++) {
            ImageView view = new ImageView(img); view.setFitWidth(30); view.setFitHeight(30);
            view.setX(10 + (i * 35)); view.setY(10); lifeIcons.add(view); gamePane.getChildren().add(view);
        }
        updateUI();
    }

    private void updateUI() {
        scoreText.setText("Pontos: " + score);
        String labelFase = "Fase: " + level;
        if (level == 5) labelFase += " - BOSS";
        if (level == 10) labelFase += " - SUPER BOSS";
        levelText.setText(labelFase);
    }

    private void perderVida() {
        lives--;
        if (!lifeIcons.isEmpty()) {
            ImageView icon = lifeIcons.remove(lifeIcons.size() - 1); gamePane.getChildren().remove(icon);
        }
        if (lives <= 0) gameOver();
    }

    private void resetGameLogic() {
        lives = 3; score = 0; level = 1;

        if (boss != null) gamePane.getChildren().remove(boss.getView()); boss = null; bossSpawned = false;
        for (BossBullet bb : bossBullets) gamePane.getChildren().remove(bb.getView()); bossBullets.clear();

        for (Enemy e : enemies) gamePane.getChildren().remove(e.getView()); enemies.clear();
        for (Bullet b : bullets) gamePane.getChildren().remove(b.getView()); bullets.clear();

        if (player != null) gamePane.getChildren().remove(player.getView());

        player = new Player(0, 0, selectedSkinId);

        player.setX(LARGURA_TELA / 2 - player.getLargura() / 2);
        player.setY(ALTURA_TELA - player.getAltura() - 10);
        player.render();
        gamePane.getChildren().add(player.getView());

        setupUI();
    }

    public static void main(String[] args) { launch(args); }
}