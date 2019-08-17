package com.limagiran.campominadobot;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
     * Objeto para gerar números aleatórios quando necessário
     */
    public static final Random RANDOM = new Random();

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

    private static final int[][] AROUND_ARR_AUX = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

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
        final List<Tile> _return = new ArrayList<>(16);

        int _x, _y;
        Tile t;
        for (int[] xy : AROUND_ARR_AUX) {
            _x = xy[0] + x;
            if (_x < 0 || _x >= tiles.length) {
                continue;
            }
            _y = xy[1] + y;
            if (_y < 0 || _y >= tiles[_x].length) {
                continue;
            }
            t = tiles[_x][_y];
            if (t.isNonClicked()) {
                _return.add(t);
            }
        }
        return _return;
    }

    /**
     * Captura todos os quadrados ao redor de um quadrado.
     *
     * @param x coordenada x do quadrado central
     * @param y coordenada y do quadrado central
     * @param tiles grade do campo minado
     * @return lista de quadrados ao redor de um quadrado
     */
    public static List<Tile> getAroundAll(int x, int y, Tile[][] tiles) {
        final List<Tile> _return = new ArrayList<>(16);
        int _x, _y;
        for (int[] xy : AROUND_ARR_AUX) {
            _x = xy[0] + x;
            if (_x < 0 || _x >= tiles.length) {
                continue;
            }
            _y = xy[1] + y;
            if (_y < 0 || _y >= tiles[_x].length) {
                continue;
            }
            _return.add(tiles[_x][_y]);
        }
        return _return;
    }

    /**
     * Conta quantos quadrados que ainda não foram clicados há na lista
     *
     * @param tiles lista de quadrados
     * @return quantidade de quadrados que ainda não foram clicados.
     */
    public static int countNonClicked(List<Tile> tiles) {
        int count = 0;
        for (Tile t : tiles) {
            if (t.isNonClicked()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Converte uma lista de {@link Tile} para {@link EnumTile}
     *
     * @param tiles lista de quadrados
     * @return lista mapeada para {@link EnumTile}.
     */
    public static List<EnumTile> mapEnumTile(List<Tile> tiles) {
        final List<EnumTile> _return = new ArrayList<>(tiles.size() * 2);
        for (Tile t : tiles) {
            _return.add(t.getEnumTile());
        }
        return _return;
    }

    /**
     * Conta quantos quadrados são 'bandeira' na lista
     *
     * @param tiles lista de quadrados
     * @return quantidade de quadrados que são 'bandeira'.
     */
    public static int countFlagBomb(List<Tile> tiles) {
        int count = 0;
        for (Tile t : tiles) {
            if (t.isFlagBomb()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gera um número aleatório entre 1 e {@code max}
     *
     * @param max número máximo a ser sorteado
     * @return número aleatório entre 1 e {@code max}. retorna -1 se
     * {@code max < 1}
     */
    public static int random(int max) {
        return (max < 1) ? -1 : RANDOM.nextInt(max) + 1;
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
     * Screenshot da tela
     *
     * @param area área da captura
     * @return screenshot
     */
    public static BufferedImage screenshot(Rectangle area) {
        return ROBOT.createScreenCapture(area);
    }

    /**
     * Screenshot da tela
     *
     * @return screenshot
     */
    public static BufferedImage screenshot() {
        return screenshot(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    /**
     * Exportar o arquivo winmine.exe
     *
     * @throws Exception
     */
    public static void exportWinmine() throws Exception {
        final String url = "/com/limagiran/campominadobot/winmine.exe";
        try (InputStream is = Utils.class.getResourceAsStream(url);
                FileOutputStream fos = new FileOutputStream("winmine.exe")) {
            int i;
            byte buf[] = new byte[2048];
            while ((i = is.read(buf)) >= 0) {
                fos.write(buf, 0, i);
            }
        }
    }

    /**
     * Exportar o arquivo e executá-lo
     */
    public static void exportAndOpenWinmine() {
        try {
            exportWinmine();
            Desktop.getDesktop().open(new File("winmine.exe"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Verifica se pelo menos um objeto é {@link EnumTile#TILE}
     *
     * @param aroundTemp
     * @return {@code true} para {@link EnumTile#TILE}. {@code false} o
     * contrário.
     */
    public static boolean anyIsTile(List<Tile> aroundTemp) {
        for (Tile tAround : aroundTemp) {
            if (tAround.isTile()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clona cada objeto do array
     *
     * @param tiles quadrados do jogo
     * @return array com a cópia de cada um dos objetos
     */
    public static Tile[][] clone(Tile[][] tiles) {
        final Tile[][] _clone = new Tile[tiles.length][tiles[0].length];
        final Rectangle totalArea = tiles[0][0].getTotalArea();
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                Tile t = tiles[x][y];
                Tile newTile = new Tile(t.rect, t.x, t.y, totalArea);
                newTile.setEnumTile(t.getEnumTile());
                _clone[x][y] = newTile;
            }
        }
        return _clone;
    }

    /**
     *
     * @param x coordenada x de uma área da tela do winmine.exe
     * @param y coordenada y de uma área da tela do winmine.exe
     */
    public static void restartGame(int x, int y) {
        ROBOT.mouseMove(x, y);
        ROBOT.mousePress(InputEvent.BUTTON2_MASK);
        ROBOT.mouseRelease(InputEvent.BUTTON2_MASK);
        ROBOT.keyPress(KeyEvent.VK_F2);
        ROBOT.keyRelease(KeyEvent.VK_F2);
        sleep(50);
    }
}
