import javafx.scene.image.Image;

public class Boss extends Sprite {

    private int hp;
    private double speedX = 3.0; // Velocidade lateral

    // --- MUDANÇA: Construtor aceita saber se é Super Boss ---
    public Boss(double x, double y, boolean isSuperBoss) {
        // Se for Super, carrega imagem do super, senão normal
        super(isSuperBoss ? "/art/super_boss.png" : "/art/boss.png", x, y);

        // Define tamanho
        if (isSuperBoss) {
            setFixedSize(250, 250); // Super Boss é maior
            this.hp = 100; // Super Boss tem o dobro de vida
            this.speedX = 4.5; // E é mais rápido
        } else {
            setFixedSize(200, 200); // Boss Normal
            this.hp = 50;
            this.speedX = 3.0;
        }
    }

    public void update() {
        this.x += speedX;

        // Bate nas paredes laterais e volta
        if (this.x <= 0 || this.x >= SouthParkInvaders.LARGURA_TELA - this.largura) {
            speedX *= -1; // Inverte a direção
        }

        render();
    }

    public void tomarDano() {
        this.hp--;
    }

    public int getHp() {
        return hp;
    }
}