public class Bullet extends Sprite {

    public Bullet(double x, double y) {
        super("/art/cartman_tiro.png", x, y);
        setTamanho(80);
    }

    public void update() {
        this.y -= SouthParkInvaders.VELOCIDADE_TIRO;
        render();
    }
}