import javafx.scene.image.Image;

public class Player extends Sprite {

    private Image normalImage;
    private Image shootingImage;

    // Agora recebemos o ID da skin (1, 2, 3 ou 4)
    public Player(double x, double y, int skinId) {
        // Carrega a imagem baseada no ID
        super("/art/skin" + skinId + ".png", x, y);

        // Carrega as versões normal e atirando daquela skin específica
        this.normalImage = new Image(getClass().getResourceAsStream("/art/skin" + skinId + ".png"));
        this.shootingImage = new Image(getClass().getResourceAsStream("/art/skin" + skinId + "_shoot.png"));

        setFixedSize(80, 113);
        setImage(false);
    }

    public void setFixedSize(double width, double height) {
        this.view.setFitWidth(width);
        this.view.setFitHeight(height);
        this.largura = width;
        this.altura = height;
    }

    public void setImage(boolean isShooting) {
        if (isShooting) {
            this.view.setImage(shootingImage);
        } else {
            this.view.setImage(normalImage);
        }
    }

    public void moveLeft() {
        this.x -= SouthParkInvaders.VELOCIDADE_JOGADOR;
        render();
    }

    public void moveRight() {
        this.x += SouthParkInvaders.VELOCIDADE_JOGADOR;
        render();
    }
}