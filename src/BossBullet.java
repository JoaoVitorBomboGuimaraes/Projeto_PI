public class BossBullet extends Sprite {

    // Você precisa da imagem "tiro_boss.png" na pasta /art/
    public BossBullet(double x, double y) {
        super("/art/tiro_boss.png", x, y);

        // Agora isso vai funcionar porque adicionamos no Sprite.java!
        setFixedSize(30, 60);
    }

    public void update() {
        // Velocidade do tiro do boss (vai para baixo)
        this.y += 7.0;
        render();
    }
}