import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
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
    private StackPane mainRoot = new StackPane();

    private Pane gamePane = new Pane();
    private Pane menuPane = new Pane();
    private Pane selectionPane = new Pane();
    private Pane musicSelectionPane = new Pane();
    private Pane gameOverPane = new Pane();
    private Pane videoPane = new Pane();

    // --- Variáveis de Jogo ---
    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();

    private int selectedSkinId = 1;
    private String selectedMusicFile = "track1.mp3";

    private Boss boss;
    private List<BossBullet> bossBullets = new ArrayList<>();
    private boolean bossSpawned = false;

    // --- Variáveis de Nível ---
    private int level = 1;
    private boolean isTransitioning = false;
    private Text bigLevelText = new Text();
    private Text levelUiText = new Text();

    // Pontuação mais curta para progredir mais rápido
    private int[] scoreThresholds = {
            0, 500, 1200, 2000, 3000, 999999, 4500, 6000, 8000, 10000, 999999
    };

    // --- Munição ---
    private int maxAmmo = 6;
    private int currentAmmo = 6;
    private Text ammoText = new Text();
    private ImageView ammoStationView;

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

    // --- Mídia ---
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Runnable onVideoFinished;
    private MediaPlayer backgroundMusicPlayer;
    private boolean isVideoPlaying = false;

    // --- Efeitos Sonoros ---
    private AudioClip shootSound;
    private AudioClip reloadSound;
    private AudioClip bossShootSound;
    private AudioClip gameOverSound;

    // --- NOVO: Sons de Dano por Skin ---
    private AudioClip hurtSound1;
    private AudioClip hurtSound2;
    private AudioClip hurtSound3;
    private AudioClip hurtSound4;

    // --- Dados ---
    private String[] songTitles = {
            "Mr. Brightside", "Any Way You Want It", "Like a Prayer", "Golden",
            "Fear of the Dark", "Mr. Blue Sky", "Dancing Queen", "More Than a Feeling"
    };
    private String[] songArtists = {
            "The Killers", "Journey", "Madonna", "HUNTR/X",
            "Iron Maiden", "Eletric Light Orchestra", "ABBA", "Boston"
    };
    private String[] skinNames = {
            "Stan", "Butters", "Kyle", "Kenny"
    };

    @Override
    public void start(Stage primaryStage) {
        root.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        root.setClip(new javafx.scene.shape.Rectangle(LARGURA_TELA, ALTURA_TELA));

        gamePane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        menuPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        selectionPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        musicSelectionPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        gameOverPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        videoPane.setPrefSize(LARGURA_TELA, ALTURA_TELA);
        videoPane.setStyle("-fx-background-color: black;");

        loadSounds();

        // 1. Setup Game Pane
        Image bgImage = new Image(getClass().getResourceAsStream("/art/Background.jpg"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(LARGURA_TELA); bgView.setFitHeight(ALTURA_TELA);
        gamePane.getChildren().add(bgView);

        try {
            Image ammoImg = new Image(getClass().getResourceAsStream("/art/ammo_station.png"));
            ammoStationView = new ImageView(ammoImg);
            ammoStationView.setFitWidth(60); ammoStationView.setFitHeight(60);
            ammoStationView.setX(10); ammoStationView.setY(ALTURA_TELA - 80);
            gamePane.getChildren().add(ammoStationView);
        } catch (Exception e) { System.out.println("Aviso: ammo_station.png não encontrada."); }

        scoreText.setFont(Font.font("Verdana", 20)); scoreText.setFill(Color.WHITE);
        scoreText.setX(LARGURA_TELA - 200); scoreText.setY(30);
        gamePane.getChildren().add(scoreText);

        levelUiText.setFont(Font.font("Verdana", 20)); levelUiText.setFill(Color.YELLOW);
        levelUiText.setX(LARGURA_TELA / 2 - 40); levelUiText.setY(30); levelUiText.setText("");
        gamePane.getChildren().add(levelUiText);

        ammoText.setFont(Font.font("Verdana", FontWeight.BOLD, 20)); ammoText.setFill(Color.CYAN);
        ammoText.setX(20); ammoText.setY(ALTURA_TELA - 90);
        ammoText.setText("BALAS: " + currentAmmo + "/" + maxAmmo);
        gamePane.getChildren().add(ammoText);

        bigLevelText.setFont(Font.font("Verdana", FontWeight.BOLD, 80));
        bigLevelText.setFill(Color.YELLOW); bigLevelText.setStroke(Color.BLACK); bigLevelText.setStrokeWidth(3);
        bigLevelText.setVisible(false);
        gamePane.getChildren().add(bigLevelText);

        // 2. Setup Menu Pane
        Image menuImage = new Image(getClass().getResourceAsStream("/art/menu.png"));
        menuBackgroundView = new ImageView(menuImage);
        menuBackgroundView.setFitWidth(LARGURA_TELA); menuBackgroundView.setFitHeight(ALTURA_TELA);

        Text titleText = new Text("SOUTH PARK\nINVADERS");
        titleText.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 60));
        titleText.setFill(Color.YELLOW); titleText.setStroke(Color.BLACK); titleText.setStrokeWidth(4);
        titleText.setTextAlignment(TextAlignment.CENTER);
        DropShadow ds = new DropShadow(); ds.setOffsetY(5.0f); ds.setColor(Color.color(0.0f, 0.0f, 0.0f));
        titleText.setEffect(ds);

        btnPlay = criarBotaoEstilizado("JOGAR");
        btnPlay.setOnAction(event -> playVideo("intro.mp4", () -> showSelectionScreen()));

        VBox menuLayout = new VBox(50); menuLayout.setAlignment(Pos.CENTER);
        menuLayout.getChildren().addAll(titleText, btnPlay);
        menuLayout.setPrefWidth(LARGURA_TELA); menuLayout.setPrefHeight(ALTURA_TELA);
        menuPane.getChildren().addAll(menuBackgroundView, menuLayout);

        // 3. Setup Outras Telas
        setupSelectionScreenUI();
        setupMusicSelectionScreenUI();

        Image gameOverImage = new Image(getClass().getResourceAsStream("/art/gameover.png"));
        gameOverView = new ImageView(gameOverImage);
        gameOverView.setFitWidth(LARGURA_TELA); gameOverView.setFitHeight(ALTURA_TELA);
        gameOverPane.getChildren().add(gameOverView);

        root.getChildren().addAll(gamePane, gameOverPane, musicSelectionPane, selectionPane, menuPane, videoPane);
        videoPane.setVisible(false);

        mainRoot.getChildren().add(root);
        mainRoot.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(mainRoot, LARGURA_TELA, ALTURA_TELA);

        LetterboxScale scaleLogic = new LetterboxScale(scene, root);
        scene.widthProperty().addListener((obs, oldVal, newVal) -> scaleLogic.updateScale());
        scene.heightProperty().addListener((obs, oldVal, newVal) -> scaleLogic.updateScale());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F11) primaryStage.setFullScreen(!primaryStage.isFullScreen());
            if (videoPane.isVisible()) { if (event.getCode() == KeyCode.ENTER) skipVideo(); return; }
            if (menuPane.isVisible()) { if (event.getCode() == KeyCode.ENTER) playVideo("intro.mp4", () -> showSelectionScreen()); }
            else if (gameOverPane.isVisible()) { if (event.getCode() == KeyCode.ENTER) showMenu(); }
            else if (gamePane.isVisible() && !isTransitioning) { keys.put(event.getCode(), true); }
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

    private Button criarBotaoEstilizado(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(220); btn.setPrefHeight(60);
        String estiloNormal = "-fx-background-color: linear-gradient(#ffcc00, #ff9900); -fx-background-radius: 30; -fx-border-color: white; -fx-border-width: 3; -fx-border-radius: 30; -fx-text-fill: white; -fx-font-family: 'Verdana'; -fx-font-weight: bold; -fx-font-size: 24px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 1);";
        String estiloHover = "-fx-background-color: linear-gradient(#ffdd44, #ffaa00); -fx-background-radius: 30; -fx-border-color: white; -fx-border-width: 3; -fx-border-radius: 30; -fx-text-fill: white; -fx-font-family: 'Verdana'; -fx-font-weight: bold; -fx-font-size: 24px; -fx-effect: dropshadow(three-pass-box, rgba(255,255,255,0.8), 10, 0, 0, 0);";
        btn.setStyle(estiloNormal);
        btn.setOnMouseEntered(e -> { btn.setStyle(estiloHover); btn.setScaleX(1.1); btn.setScaleY(1.1); });
        btn.setOnMouseExited(e -> { btn.setStyle(estiloNormal); btn.setScaleX(1.0); btn.setScaleY(1.0); });
        return btn;
    }

    private static class LetterboxScale {
        private final Scene scene; private final Pane content;
        public LetterboxScale(Scene scene, Pane content) { this.scene = scene; this.content = content; }
        public void updateScale() {
            double width = scene.getWidth(); double height = scene.getHeight();
            if (width == 0 || height == 0) return;
            double scaleFactor = Math.min(width / LARGURA_TELA, height / ALTURA_TELA);
            Scale scale = new Scale(scaleFactor, scaleFactor);
            scale.setPivotX(0); scale.setPivotY(0);
            content.getTransforms().setAll(scale);
            content.setTranslateX((width - LARGURA_TELA * scaleFactor) / 2);
            content.setTranslateY((height - ALTURA_TELA * scaleFactor) / 2);
        }
    }

    private void loadSounds() {
        try {
            shootSound = new AudioClip(getClass().getResource("/sfx/shoot.mp3").toExternalForm());
            reloadSound = new AudioClip(getClass().getResource("/sfx/reload.mp3").toExternalForm());
            bossShootSound = new AudioClip(getClass().getResource("/sfx/boss_shoot.mp3").toExternalForm());
            gameOverSound = new AudioClip(getClass().getResource("/sfx/gameover.mp3").toExternalForm());

            // --- NOVO: Carrega sons de dano das skins ---
            hurtSound1 = new AudioClip(getClass().getResource("/sfx/hurt1.mp3").toExternalForm());
            hurtSound2 = new AudioClip(getClass().getResource("/sfx/hurt2.mp3").toExternalForm());
            hurtSound3 = new AudioClip(getClass().getResource("/sfx/hurt3.mp3").toExternalForm());
            hurtSound4 = new AudioClip(getClass().getResource("/sfx/hurt4.mp3").toExternalForm());

            shootSound.setVolume(0.7); reloadSound.setVolume(1.0);
            bossShootSound.setVolume(0.8); gameOverSound.setVolume(1.0);
        } catch (Exception e) { System.out.println("Aviso: Sons SFX faltando."); }
    }

    private void playBackgroundMusic() {
        try {
            if (backgroundMusicPlayer != null) { backgroundMusicPlayer.stop(); backgroundMusicPlayer.dispose(); }
            String path = getClass().getResource("/music/" + selectedMusicFile).toExternalForm();
            Media media = new Media(path);
            backgroundMusicPlayer = new MediaPlayer(media);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.5);
            backgroundMusicPlayer.play();
        } catch (Exception e) {}
    }
    private void stopBackgroundMusic() { if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop(); }

    private void playVideo(String fileName, Runnable actionAfterVideo) {
        if (isVideoPlaying) return; isVideoPlaying = true;
        try {
            this.onVideoFinished = actionAfterVideo;
            String path = getClass().getResource("/video/" + fileName).toExternalForm();
            Media media = new Media(path);
            if (mediaPlayer != null) mediaPlayer.dispose();
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(LARGURA_TELA); mediaView.setFitHeight(ALTURA_TELA); mediaView.setPreserveRatio(false);
            videoPane.getChildren().clear(); videoPane.getChildren().add(mediaView);
            videoPane.setVisible(true); videoPane.toFront();
            mediaPlayer.setOnEndOfMedia(() -> { if (isVideoPlaying) skipVideo(); });
            mediaPlayer.play();
        } catch (Exception e) {
            System.out.println("Erro vídeo: " + fileName); isVideoPlaying = false; if (onVideoFinished != null) onVideoFinished.run();
        }
    }

    private void skipVideo() {
        if (!isVideoPlaying) return; isVideoPlaying = false;
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.setOnEndOfMedia(null); mediaPlayer.dispose(); mediaPlayer = null; }
        videoPane.setVisible(false);
        if (onVideoFinished != null) { Runnable action = onVideoFinished; onVideoFinished = null; action.run(); }
    }

    private void setupSelectionScreenUI() {
        Image bgSelection = new Image(getClass().getResourceAsStream("/art/Background.jpg"));
        ImageView bgViewSel = new ImageView(bgSelection);
        bgViewSel.setFitWidth(LARGURA_TELA); bgViewSel.setFitHeight(ALTURA_TELA);
        Text title = new Text("ESCOLHA SEU JOGADOR");
        title.setFont(Font.font("Verdana", 40)); title.setFill(Color.WHITE);
        title.setX((LARGURA_TELA - title.getLayoutBounds().getWidth()) / 2); title.setY(100);
        selectionPane.getChildren().addAll(bgViewSel, title);

        int itemWidth = 140; int gapX = 30; int numItems = 4;
        double totalBlockWidth = (numItems * itemWidth) + ((numItems - 1) * gapX);
        double startX = (LARGURA_TELA - totalBlockWidth) / 2;
        int startY = (int) ALTURA_TELA / 2 - 80;

        for (int i = 0; i < 4; i++) {
            final int skinId = i + 1;
            VBox itemBox = new VBox(10); itemBox.setAlignment(Pos.CENTER); itemBox.setPrefWidth(itemWidth);
            Image skinImg = new Image(getClass().getResourceAsStream("/art/skin" + skinId + ".png"));
            ImageView skinView = new ImageView(skinImg);
            skinView.setFitWidth(100); skinView.setFitHeight(100); skinView.setPreserveRatio(true);
            String nome = (i < skinNames.length) ? skinNames[i] : "Skin " + skinId;
            Text nameText = new Text(nome); nameText.setFont(Font.font("Verdana", FontWeight.BOLD, 16)); nameText.setFill(Color.WHITE);
            itemBox.getChildren().addAll(skinView, nameText);
            itemBox.setLayoutX(startX + i * (itemWidth + gapX)); itemBox.setLayoutY(startY);
            itemBox.setStyle("-fx-padding: 15; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 15;");
            itemBox.setOnMouseEntered(e -> { itemBox.setStyle("-fx-padding: 15; -fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 15; -fx-cursor: hand;"); skinView.setFitWidth(110); skinView.setFitHeight(110); });
            itemBox.setOnMouseExited(e -> { itemBox.setStyle("-fx-padding: 15; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 15;"); skinView.setFitWidth(100); skinView.setFitHeight(100); });
            itemBox.setOnMouseClicked(e -> { this.selectedSkinId = skinId; showMusicSelectionScreen(); });
            selectionPane.getChildren().add(itemBox);
        }
    }

    private void setupMusicSelectionScreenUI() {
        Image bgMusic = new Image(getClass().getResourceAsStream("/art/Background.jpg"));
        ImageView bgViewMusic = new ImageView(bgMusic);
        bgViewMusic.setFitWidth(LARGURA_TELA); bgViewMusic.setFitHeight(ALTURA_TELA);
        Text title = new Text("ESCOLHA A TRILHA SONORA");
        title.setFont(Font.font("Verdana", 40)); title.setFill(Color.WHITE);
        title.setX((LARGURA_TELA - title.getLayoutBounds().getWidth()) / 2); title.setY(80);
        musicSelectionPane.getChildren().addAll(bgViewMusic, title);
        int itemWidth = 140; int gapX = 30; int gapY = 30; int cols = 4;
        double totalBlockWidth = (cols * itemWidth) + ((cols - 1) * gapX);
        double startX = (LARGURA_TELA - totalBlockWidth) / 2;
        int startY = 150;
        for (int i = 0; i < 8; i++) {
            final int trackId = i + 1;
            VBox itemBox = new VBox(5); itemBox.setAlignment(Pos.CENTER); itemBox.setPrefWidth(itemWidth);
            Image coverImg;
            try { coverImg = new Image(getClass().getResourceAsStream("/art/cover" + trackId + ".png")); }
            catch (Exception e) { coverImg = new Image(getClass().getResourceAsStream("/art/skin1.png")); }
            ImageView coverView = new ImageView(coverImg);
            coverView.setFitWidth(120); coverView.setFitHeight(120);
            Text songTitle = new Text(songTitles[i]); songTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 14)); songTitle.setFill(Color.WHITE); songTitle.setWrappingWidth(itemWidth); songTitle.setTextAlignment(TextAlignment.CENTER);
            Text artistName = new Text(songArtists[i]); artistName.setFont(Font.font("Verdana", 12)); artistName.setFill(Color.LIGHTGRAY); artistName.setTextAlignment(TextAlignment.CENTER);
            itemBox.getChildren().addAll(coverView, songTitle, artistName);
            int col = i % 4; int row = i / 4;
            itemBox.setLayoutX(startX + col * (itemWidth + gapX)); itemBox.setLayoutY(startY + row * (180 + gapY));
            itemBox.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");
            itemBox.setOnMouseEntered(e -> { itemBox.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; -fx-cursor: hand;"); coverView.setFitWidth(130); coverView.setFitHeight(130); });
            itemBox.setOnMouseExited(e -> { itemBox.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;"); coverView.setFitWidth(120); coverView.setFitHeight(120); });
            itemBox.setOnMouseClicked(e -> { if (!isVideoPlaying) { this.selectedMusicFile = "track" + trackId + ".mp3"; playVideo("prejogo.mp4", () -> startGame()); } });
            musicSelectionPane.getChildren().add(itemBox);
        }
    }

    private void showMenu() {
        stopBackgroundMusic();
        menuPane.setVisible(true); selectionPane.setVisible(false); musicSelectionPane.setVisible(false);
        gamePane.setVisible(false); gameOverPane.setVisible(false); videoPane.setVisible(false);
        menuPane.toFront(); gameTimer.stop();
    }
    private void showSelectionScreen() {
        menuPane.setVisible(false); selectionPane.setVisible(true); musicSelectionPane.setVisible(false);
        gamePane.setVisible(false); gameOverPane.setVisible(false); selectionPane.toFront();
    }
    private void showMusicSelectionScreen() {
        menuPane.setVisible(false); selectionPane.setVisible(false); musicSelectionPane.setVisible(true);
        gamePane.setVisible(false); gameOverPane.setVisible(false); musicSelectionPane.toFront();
    }

    private void startGame() {
        resetGameLogic(); playBackgroundMusic();
        gamePane.setVisible(true); menuPane.setVisible(false); selectionPane.setVisible(false); musicSelectionPane.setVisible(false); gameOverPane.setVisible(false);
        gamePane.toFront(); root.requestFocus(); announceLevel(1); gameTimer.start();
    }

    private void gameOver() {
        stopBackgroundMusic();
        if (gameOverSound != null) gameOverSound.play();
        gameTimer.stop();
        gameOverPane.setVisible(true); gamePane.setVisible(false); menuPane.setVisible(false);
        gameOverPane.toFront();
    }
    private void winGame() { stopBackgroundMusic(); playVideo("final.mp4", () -> gameOver()); }

    private void update(long now) {
        if (isTransitioning) { return; }
        handleInput(); checkReload(); checkLevelProgression();
        if (level != 5 && level != 10) {
            long intervaloSpawn = 2_000_000_000L - (level * 180_000_000L);
            if (intervaloSpawn < 300_000_000L) intervaloSpawn = 300_000_000L;
            if (now - lastEnemySpawnTime > intervaloSpawn) { spawnEnemy(); lastEnemySpawnTime = now; }
            updateEnemies();
        } else { updateBoss(); }
        updateBullets(); updateBossBullets(); checkCollisions(); updateUI();
    }

    private void checkReload() {
        if (player.getX() < 80 && currentAmmo < maxAmmo) {
            currentAmmo = maxAmmo; if (reloadSound != null) reloadSound.play(); updateUI();
        }
    }

    private void checkLevelProgression() {
        if (level == 5 || level == 10) return;
        if (score >= scoreThresholds[level]) { int nextLevel = level + 1; announceLevel(nextLevel); }
    }

    private void announceLevel(int newLevel) {
        level = newLevel; isTransitioning = true;
        for (Enemy e : enemies) gamePane.getChildren().remove(e.getView()); enemies.clear();
        for (Bullet b : bullets) gamePane.getChildren().remove(b.getView()); bullets.clear();
        String txt = "FASE " + level; if (level == 5) txt = "CHEFE!"; if (level == 10) txt = "BATALHA FINAL";
        bigLevelText.setText(txt);
        bigLevelText.setX((LARGURA_TELA - bigLevelText.getLayoutBounds().getWidth()) / 2);
        bigLevelText.setY(ALTURA_TELA / 2);
        bigLevelText.setVisible(true); bigLevelText.toFront();
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> { bigLevelText.setVisible(false); isTransitioning = false; lastEnemySpawnTime = System.nanoTime(); });
        pause.play();
    }

    private void handleInput() {
        if (keys.getOrDefault(KeyCode.LEFT, false) && player.getX() > 0) player.moveLeft();
        if (keys.getOrDefault(KeyCode.RIGHT, false) && player.getX() < LARGURA_TELA - player.getLargura()) player.moveRight();
        if (keys.getOrDefault(KeyCode.SPACE, false)) {
            if (currentAmmo > 0) {
                player.setImage(true); shoot(); if (shootSound != null) shootSound.play();
                currentAmmo--; keys.put(KeyCode.SPACE, false);
                PauseTransition delay = new PauseTransition(Duration.millis(200));
                delay.setOnFinished(e -> player.setImage(false)); delay.play();
            }
        }
    }

    private void updateEnemies() {
        enemies.removeIf(enemy -> {
            enemy.update();
            if (enemy.getY() > ALTURA_TELA) { gamePane.getChildren().remove(enemy.getView()); perderVida(); return true; }
            return false;
        });
    }

    private void updateBoss() {
        if (!bossSpawned) {
            for (Enemy e : enemies) gamePane.getChildren().remove(e.getView()); enemies.clear();
            boolean isSuper = (level == 10);
            boss = new Boss(LARGURA_TELA / 2 - 100, 50, isSuper);
            gamePane.getChildren().add(boss.getView()); bossSpawned = true;
        }
        if (boss != null && boss.getHp() > 0) {
            boss.update();
            int chanceTiro = (level == 10) ? 40 : 60;
            if (random.nextInt(chanceTiro) == 0) shootBossBullet();
        }
        else if (boss != null && boss.getHp() <= 0) {
            gamePane.getChildren().remove(boss.getView());
            if (level == 5) { boss = null; bossSpawned = false; score += 1000; announceLevel(6); }
            else { winGame(); }
        }
    }

    private void shootBossBullet() {
        double tiroX = boss.getX() + boss.getLargura() / 2 - 15;
        double tiroY = boss.getY() + (boss.getAltura() / 2);
        BossBullet bBullet = new BossBullet(tiroX, tiroY);
        bossBullets.add(bBullet); gamePane.getChildren().add(bBullet.getView());
        if (bossShootSound != null) bossShootSound.play();
    }

    private void updateBossBullets() {
        bossBullets.removeIf(b -> {
            b.update(); boolean fora = b.getY() > ALTURA_TELA; if (fora) gamePane.getChildren().remove(b.getView()); return fora;
        });
    }

    private void updateBullets() {
        bullets.removeIf(bullet -> {
            bullet.update(); boolean fora = bullet.getY() < 0; if (fora) gamePane.getChildren().remove(bullet.getView()); return fora;
        });
    }

    private void shoot() {
        Bullet bullet = new Bullet(0, 0);
        bullet.setX(player.getX() + (player.getLargura() / 2) - (bullet.getLargura() / 2));
        bullet.setY(player.getY() - bullet.getAltura() + 5); bullet.render();
        bullets.add(bullet); gamePane.getChildren().add(bullet.getView());
    }

    private void spawnEnemy() {
        double enemySize = 200; double x = random.nextDouble() * (LARGURA_TELA - enemySize); double y = -(random.nextDouble() * 300 + 250);
        Enemy enemy = new Enemy(x, y, random.nextInt(4) + 1);
        enemies.add(enemy); gamePane.getChildren().add(enemy.getView());
    }

    private void checkCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<BossBullet> bossBulletsToRemove = new ArrayList<>();

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
        else if (boss != null && boss.getHp() > 0) {
            for (Bullet bullet : bullets) {
                if (bullet.getView().getBoundsInParent().intersects(boss.getView().getBoundsInParent())) {
                    bulletsToRemove.add(bullet); boss.tomarDano();
                    playExplosion(bullet.getX(), bullet.getY());
                    if (boss.getHp() <= 0) { playExplosion(boss.getX(), boss.getY()); }
                }
            }
        }

        for (BossBullet bb : bossBullets) {
            if (bb.getView().getBoundsInParent().intersects(player.getView().getBoundsInParent())) {
                bossBulletsToRemove.add(bb); playExplosion(player.getX(), player.getY()); perderVida();
            }
        }

        bullets.removeAll(bulletsToRemove); enemies.removeAll(enemiesToRemove); bossBullets.removeAll(bossBulletsToRemove);
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
        levelUiText.setText("");
        ammoText.setText("BALAS: " + currentAmmo + "/" + maxAmmo);
        if (currentAmmo == 0) ammoText.setFill(Color.RED); else ammoText.setFill(Color.CYAN);
    }

    private void perderVida() {
        lives--;
        // --- NOVO: Toca som de dano dependendo da skin ---
        switch (selectedSkinId) {
            case 1: if (hurtSound1 != null) hurtSound1.play(); break;
            case 2: if (hurtSound2 != null) hurtSound2.play(); break;
            case 3: if (hurtSound3 != null) hurtSound3.play(); break;
            case 4: if (hurtSound4 != null) hurtSound4.play(); break;
        }

        if (!lifeIcons.isEmpty()) {
            ImageView icon = lifeIcons.remove(lifeIcons.size() - 1); gamePane.getChildren().remove(icon);
        }
        if (lives <= 0) gameOver();
    }

    private void resetGameLogic() {
        lives = 3; score = 0; currentAmmo = maxAmmo;
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