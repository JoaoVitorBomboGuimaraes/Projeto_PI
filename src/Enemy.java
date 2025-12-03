public class Enemy extends Sprite {

    public Enemy(double x, double y, int skinId) {
        super("/art/alien" + skinId + ".png", x, y);
        setTamanho(170);
    }

    public void update() {
        this.y += SouthParkInvaders.VELOCIDADE_INIMIGO;
        render();
    }
}