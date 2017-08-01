package com.limagiran.campominadobot;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.*;
import javax.swing.JOptionPane;

/**
 *
 * @author Vinicius Lima
 */
public class Utils {

    /**
     * Robô utilizado como nosso bot
     */
    public static final Robot ROBOT = createRobot();

    /**
     * Cria um robô ou encerra o programa caso não seja possível instanciar um
     * objeto java.awt.Robot
     *
     * @return instância da classe java.awt.Robot
     */
    private static Robot createRobot() {
        try {
            return new Robot();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Não foi possível inicializar a classe java.awt.Robot");
            System.exit(0);
        }
        return null;
    }

    /**
     * foreach padrão para uma grade de campo minado
     *
     * @param tiles grade do campo minado
     * @param consumer consumer
     */
    public static void forEach(Tile[][] tiles, TileForEachConsumer<Tile, Integer, Integer> consumer) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                consumer.accept(tiles[x][y], x, y);
            }
        }
    }

    /**
     * foreach padrão para uma grade de campo minado
     *
     * @param tiles grade do campo minado
     * @param consumer consumer
     */
    public static void forEach(Tile[][] tiles, BiConsumer<Integer, Integer> consumer) {
        forEach(tiles, (Tile t, Integer x, Integer y) -> consumer.accept(x, y));
    }

    /**
     * foreach padrão para uma grade de campo minado
     *
     * @param tiles grade do campo minado
     * @param consumer consumer
     */
    public static void forEach(Tile[][] tiles, Consumer<Tile> consumer) {
        forEach(tiles, (Tile t, Integer x, Integer y) -> consumer.accept(t));
    }

    /**
     * Delimita a área de cada quadrado da área total jogável do campo minado
     *
     * @param rect área total jogável do campo minado
     * @return grade do campo minado
     */
    public static Rectangle[][] rectToGrid(Rectangle rect) {
        final Rectangle[][] _return = new Rectangle[Math.round(rect.width / 16)][Math.round(rect.height / 16)];
        for (int x = 0; x < _return.length; x++) {
            for (int y = 0; y < _return[x].length; y++) {
                _return[x][y] = new Rectangle(rect.x + x * 16, rect.y + y * 16, 16, 16);
            }
        }
        return _return;
    }

    /**
     * Captura todos os quadrados que ainda não foram clicados ao redor de um
     * quadrado.
     *
     * @param x coordenada x do quadrado central
     * @param y coordenada y do quadrado central
     * @param tiles grade do campo minado
     * @return lista de quadrados que ainda não foram clicados ao redor de um
     * quadrado
     */
    public static List<Tile> getAround(int x, int y, Tile[][] tiles) {
        List<Tile> _return = new ArrayList<>();
        int[] array = {-1, 0, 1};
        for (int _x : array) {
            for (int _y : array) {
                if ((_x == 0) && (_y == 0)) {
                    continue;
                }
                try {
                    Tile t = tiles[x + _x][y + _y];
                    if (isNonClicked(t)) {
                        _return.add(tiles[x + _x][y + _y]);
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return _return;
    }

    /**
     * Verifica se é um quadrado que ainda não foi clicado
     *
     * @param tile quadrado
     * @return {@code true} para quadrado ainda não clicado. {@code false} o
     * contrário.
     */
    public static boolean isNonClicked(Tile tile) {
        return (tile.isTile() || tile.isFlagBomb());
    }

    /**
     * Conta quantos quadrados que ainda não foram clicados há na lista
     *
     * @param tiles lista de quadrados
     * @return {@code true} quantidade de quadrados que ainda não foram
     * clicados. {@code false} o contrário.
     */
    public static long countNonClicked(List<Tile> tiles) {
        return tiles.stream()
                .filter(Utils::isNonClicked)
                .count();
    }

    /**
     * Conta quantos quadrados são 'bandeira' na lista
     *
     * @param tiles lista de quadrados
     * @return {@code true} quantidade de quadrados que são 'bandeira'.
     * {@code false} o contrário.
     */
    public static long countFlagBomb(List<Tile> tiles) {
        return tiles.stream()
                .filter(Tile::isFlagBomb)
                .count();
    }

    /**
     * Gera um número aleatório entre 1 e {@code max}
     *
     * @param max número máximo a ser sorteado
     * @return número aleatório entre 1 e {@code max}. retorna -1 se
     * {@code max < 1}
     */
    public static int random(int max) {
        return ((max < 1) ? -1 : (((int) (Math.random() * 1000000) % max) + 1));
    }

    /**
     * Thread.sleep();
     *
     * @param time tempo em espera
     */
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            //ignore
        }
    }

    /**
     * Conta a quantidade de pixels com a cor {@code rgb} passada por parâmetro
     *
     * @param image imagem
     * @param rgb cor rgb
     * @return quantidade de pixels encontrados da cor {@code rgb}
     */
    public static int countColor(BufferedImage image, int rgb) {
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) == rgb) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Screenshot em tela cheia
     *
     * @return screenshot
     */
    public static BufferedImage screenshot() {
        return ROBOT.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }
}
