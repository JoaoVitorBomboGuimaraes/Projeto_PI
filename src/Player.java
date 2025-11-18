import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Player extends Sprite {

    private Image normalImage;
    private Image shootingImage;

    public Player(double x, double y) {
        // Inicializa com uma imagem padrão para o construtor do Sprite
        super("/art/cartman.png", x, y);

        this.normalImage = new Image(getClass().getResourceAsStream("/art/cartman.png"));
        this.shootingImage = new Image(getClass().getResourceAsStream("/art/cartman_atirando.png"));

        // Define o tamanho fixo para o jogador (ex: 100x100)
        setFixedSize(100, 100); // --- MUDANÇA: Novo método para definir tamanho fixo ---

        // Garante que a imagem inicial seja a normal
        setImage(false);
    }

    // --- NOVO MÉTODO: Para definir um tamanho fixo de largura e altura ---
    // Isso garante que o Sprite não mude de tamanho ao trocar de imagem,
    // apenas a imagem dentro daquele 'quadro' fixo.
    public void setFixedSize(double width, double height) {
        this.view.setFitWidth(width);
        this.view.setFitHeight(height);
        this.largura = width; // Atualiza a largura para as colisões
        this.altura = height; // Atualiza a altura para as colisões
    }

    // Método para trocar a imagem (agora usando o tamanho fixo)
    public void setImage(boolean isShooting) {
        if (isShooting) {
            this.view.setImage(shootingImage);
        } else {
            this.view.setImage(normalImage);
        }
        // NÃO precisamos mais ajustar largura/altura aqui, pois setFixedSize já fez isso.
        // A imagem vai se adaptar ao fitWidth/fitHeight do ImageView.
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