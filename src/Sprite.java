import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Sprite {

    protected ImageView view;
    protected double x, y;
    protected double largura, altura;
    protected boolean isAlive = true;

    public Sprite(String imagePath, double x, double y) {
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        this.view = new ImageView(image);
        this.x = x;
        this.y = y;
        this.view.setX(x);
        this.view.setY(y);

        this.largura = image.getWidth();
        this.altura = image.getHeight();
    }

    protected void setTamanho(double larguraDesejada) {
        this.view.setFitWidth(larguraDesejada);
        this.view.setPreserveRatio(true);
        double ratio = this.view.getImage().getHeight() / this.view.getImage().getWidth();
        this.largura = larguraDesejada;
        this.altura = larguraDesejada * ratio;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public ImageView getView() {
        return view;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLargura() {
        return largura;
    }

    public double getAltura() {
        return altura;
    }

    protected void render() {
        view.setX(x);
        view.setY(y);
    }
}