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

    // --- Constantes do Jogo ---
    public static final double LARGURA_TELA = 800;
    public static final double ALTURA_TELA = 600;
    public static final double VELOCIDADE_JOGADOR = 5.0;
    public static final double VELOCIDADE_TIRO = 8.0;
    public static final double VELOCIDADE_INIMIGO = 2.0;

    // --- "Camadas" da aplicação ---
    private Pane root = new Pane();
    private Pane gamePane = new Pane();
    private Pane menuPane = new Pane();
    private Pane gameOverPane = new Pane();

    // --- Variáveis de Jogo ---
    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();

    private List<ImageView> lifeIcons = new ArrayList<>();

    private int lives = 3;
    private int score = 0;
    private Text scoreText = new Text();

    private long lastEnemySpawnTime = 0;
    private Random random = new Random();
    private HashMap<KeyCode, Boolean> keys = new HashMap<>();
    private AnimationTimer gameTimer;

    // --- Elementos das Telas ---
    private ImageView gameOverView;
    private ImageView menuBackgroundView;

    @Override
    public void start(Stage primaryStage) {
        root.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        gamePane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        menuPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        gameOverPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);

        // --- 1. Configura o GAME PANE (O Jogo) ---
        Image bgImage = new Image(getClass().getResourceAsStream("/art/Background.jpg"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(LARGURA_TELA);
        bgView.setFitHeight(ALTURA_TELA);
        gamePane.getChildren().add(bgView);

        player = new Player(0, 0);
        player.setX(LARGURA_TELA / 2 - player.getLargura() / 2);
        player.setY(ALTURA_TELA - player.getAltura() - 10);
        player.render();
        gamePane.getChildren().add(player.getView());

        scoreText.setFont(Font.font("Verdana", 20));
        scoreText.setFill(Color.WHITE);
        scoreText.setX(LARGURA_TELA - 150);
        scoreText.setY(30);
        gamePane.getChildren().add(scoreText);

        // --- 2. Configura o MENU PANE ---
        Image menuImage = new Image(getClass().getResourceAsStream("/art/menu.jpg"));
        menuBackgroundView = new ImageView(menuImage);
        menuBackgroundView.setFitWidth(LARGURA_TELA);
        menuBackgroundView.setFitHeight(ALTURA_TELA);

        menuPane.getChildren().add(menuBackgroundView);

        // --- 3. Configura o GAME OVER PANE ---
        Image gameOverImage = new Image(getClass().getResourceAsStream("/art/gameover.jpg"));
        gameOverView = new ImageView(gameOverImage);
        gameOverView.setFitWidth(LARGURA_TELA);
        gameOverView.setFitHeight(ALTURA_TELA);

        gameOverPane.getChildren().add(gameOverView);

        // --- 4. Configura o Loop e a Cena ---
        root.getChildren().addAll(gamePane, gameOverPane, menuPane);

        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            if (menuPane.isVisible()) {
                if (event.getCode() == KeyCode.ENTER) {
                    startGame();
                }
            }
            else if (gameOverPane.isVisible()) {
                if (event.getCode() == KeyCode.ENTER) {
                    showMenu();
                }
            }
            else if (gamePane.isVisible()) {
                keys.put(event.getCode(), true);
            }
        });

        scene.setOnKeyReleased(event -> {
            if (gamePane.isVisible()) {
                keys.put(event.getCode(), false);
                // --- NOVO: Voltar para a imagem normal do Cartman ao soltar o SPACE ---
                if (event.getCode() == KeyCode.SPACE) {
                    player.setImage(false);
                }
            }
        });

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
            }
        };

        showMenu();

        primaryStage.setTitle("South Park Invaders");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showMenu() {
        menuPane.setVisible(true);
        gamePane.setVisible(false);
        gameOverPane.setVisible(false);

        menuPane.toFront();
        gameTimer.stop();
    }

    private void startGame() {
        resetGameLogic();

        gamePane.setVisible(true);
        menuPane.setVisible(false);
        gameOverPane.setVisible(false);

        gamePane.toFront();
        gameTimer.start();
    }

    private void gameOver() {
        gameTimer.stop();

        gameOverPane.setVisible(true);
        gamePane.setVisible(false);
        menuPane.setVisible(false);

        gameOverPane.toFront();
    }

    private void update(long now) {
        handleInput();

        if (now - lastEnemySpawnTime > 1_000_000_000L) {
            spawnEnemy();
            lastEnemySpawnTime = now;
        }

        bullets.removeIf(bullet -> {
            bullet.update();
            boolean foraDaTela = bullet.getY() < 0;
            if (foraDaTela) {
                gamePane.getChildren().remove(bullet.getView());
            }
            return foraDaTela;
        });

        enemies.removeIf(enemy -> {
            enemy.update();

            if (enemy.getY() > ALTURA_TELA) {
                gamePane.getChildren().remove(enemy.getView());
                perderVida();
                return true;
            }
            return false;
        });

        checkCollisions();
        updateUI();
    }

    private void handleInput() {
        if (keys.getOrDefault(KeyCode.LEFT, false) && player.getX() > 0) {
            player.moveLeft();
        }
        if (keys.getOrDefault(KeyCode.RIGHT, false) && player.getX() < LARGURA_TELA - player.getLargura()) {
            player.moveRight();
        }
        if (keys.getOrDefault(KeyCode.SPACE, false)) {
            // --- MUDANÇA: Troca a imagem do Cartman e atira ---
            player.setImage(true); // Cartman na pose de atirar
            shoot();
            keys.put(KeyCode.SPACE, false); // Limpa o "pressionado" para atirar uma vez por toque

            // --- NOVO: Timer para voltar a imagem do Cartman ao normal ---
            PauseTransition delay = new PauseTransition(Duration.millis(200)); // 0.2 segundos
            delay.setOnFinished(e -> player.setImage(false)); // Volta para a imagem normal
            delay.play();
        }
    }

    // ... (código anterior) ...

    private void shoot() {
        Bullet bullet = new Bullet(0, 0);

        // Centraliza a bala no X do player
        bullet.setX(player.getX() + (player.getLargura() / 2) - (bullet.getLargura() / 2));

        // --- MUDANÇA: Ajuste fino na posição Y do tiro ---
        // Este valor (- bullet.getAltura() + X) vai precisar de ajuste manual
        // para sua imagem específica de "Cartman Atirando".
        // Tente valores como +0, +5, -5, +10, -10, etc., até que pareça certo.
        bullet.setY(player.getY() - bullet.getAltura() + 5); // Exemplo de ajuste: +5 (para sair um pouco mais abaixo do topo do Cartman)

        bullet.render();

        bullets.add(bullet);
        gamePane.getChildren().add(bullet.getView());
    }

    // ... (resto do código) ...
    private void spawnEnemy() {
        double enemyWidth = 85;
        double x = random.nextDouble() * (LARGURA_TELA - enemyWidth);

        // --- MUDANÇA: Inimigos surgem acima da tela ---
        // Posição Y inicial negativa (-50 a -150 pixels acima)
        double y = -(random.nextDouble() * 100 + 50); // Entre -50 e -150

        int skinId = random.nextInt(4) + 1;

        Enemy enemy = new Enemy(x, y, skinId); // Cria na nova posição Y
        enemies.add(enemy);
        gamePane.getChildren().add(enemy.getView());
    }

    private void checkCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            for (Enemy enemy : enemies) {
                if (bullet.getView().getBoundsInParent().intersects(enemy.getView().getBoundsInParent())) {
                    bulletsToRemove.add(bullet);
                    enemiesToRemove.add(enemy);
                    playExplosion(enemy.getX(), enemy.getY());
                    score += 100;
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
        enemies.removeAll(enemiesToRemove);

        bulletsToRemove.forEach(b -> gamePane.getChildren().remove(b.getView()));
        enemiesToRemove.forEach(e -> gamePane.getChildren().remove(e.getView()));
    }

    private void playExplosion(double x, double y) {
        Image explosionImage = new Image(getClass().getResourceAsStream("/art/explosion.GIF"));
        ImageView explosionView = new ImageView(explosionImage);
        explosionView.setX(x);
        explosionView.setY(y);

        gamePane.getChildren().add(explosionView);

        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(event -> gamePane.getChildren().remove(explosionView));
        delay.play();
    }

    private void setupUI() {
        for (ImageView icon : lifeIcons) {
            gamePane.getChildren().remove(icon);
        }
        lifeIcons.clear();

        Image lifeIconImg = new Image(getClass().getResourceAsStream("/art/Vida.png"));
        for (int i = 0; i < lives; i++) {
            ImageView lifeView = new ImageView(lifeIconImg);
            lifeView.setFitWidth(30);
            lifeView.setFitHeight(30);
            lifeView.setX(10 + (i * 35));
            lifeView.setY(10);

            lifeIcons.add(lifeView);
            gamePane.getChildren().add(lifeView);
        }

        scoreText.setText("Pontos: " + score);
    }

    private void updateUI() {
        scoreText.setText("Pontos: " + score);
    }

    private void perderVida() {
        lives--;

        if (!lifeIcons.isEmpty()) {
            ImageView iconToRemove = lifeIcons.remove(lifeIcons.size() - 1);
            gamePane.getChildren().remove(iconToRemove);
        }

        if (lives <= 0) {
            gameOver();
        }
    }

    private void resetGameLogic() {
        lives = 3;
        score = 0;

        for (Enemy enemy : enemies) {
            gamePane.getChildren().remove(enemy.getView());
        }
        enemies.clear();

        for (Bullet bullet : bullets) {
            gamePane.getChildren().remove(bullet.getView());
        }
        bullets.clear();

        // --- MUDANÇA: Garante que o Cartman comece com a imagem normal ---
        player.setImage(false);
        player.setX(LARGURA_TELA / 2 - player.getLargura() / 2);
        player.setY(ALTURA_TELA - player.getAltura() - 10);
        player.render();

        setupUI();
    }

    public static void main(String[] args) {
        launch(args);
    }
}