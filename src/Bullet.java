public class Bullet extends Sprite {

    public Bullet(double x, double y) {
        super("/art/tiro.png", x, y);
        setTamanho(6);
    }

    public void update() {
        this.y -= SouthParkInvaders.VELOCIDADE_TIRO;
        render();
    }
}