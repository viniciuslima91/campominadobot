package com.limagiran.campominadobot;

import static com.limagiran.campominadobot.Utils.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.*;
import javax.swing.*;

/**
 *
 * @author Vinicius Lima
 */
public class CampoMinadoBot {

    /**
     * {@code true} se o bot está em atividade. {@code false} o contrário.
     */
    private static boolean started;

    /**
     * {@code true} para forçar a parada do bot, caso esteja em atividade.
     */
    private static boolean stop;

    /**
     * Armazena a área do jogo que contém os campos e minas
     */
    private static final Rectangle RECTANGLE = new Rectangle();

    static {
        JOptionPane.getRootFrame().setAlwaysOnTop(true);
        startThreadCheckStop();
    }

    /**
     * Inicia uma thread responsável por verificar se a janela do campo minado
     * ainda está visível, se não tiver visível, ele altera a variável
     * {@code stop = true} para forçar o encerramento do bot.<br>
     * A verificação é feita utilizando a imagem SMILE da janela do campo
     * minado, caso não haja pelo menos 24 pixels amarelos na posição onde
     * deveria ter o SMILE da janela do campo minado, o método considera que a
     * janela do campo minado não está visível.
     */
    private static void startThreadCheckStop() {
        final Thread checkStop = new Thread(() -> {
            final Rectangle rect = new Rectangle();
            //cor rgb do smile do campo minado (amarelo)
            final int rgb = new Color(255, 255, 0).getRGB();
            while (true) {
                if (started && (RECTANGLE.x > 0)) {
                    //captura um screenshot do pedaço da tela onde deveria estar
                    //o SMILE da janela do campo minado, capturando uma área 8x12
                    //e precisando conter pelo menos 24 pixels amarelos nessa área
                    //para considerar que a janela ainda está visível
                    rect.setBounds((int) RECTANGLE.getCenterX() - 4, (int) RECTANGLE.getCenterX() - 24, 8, 12);
                    BufferedImage bi = Utils.ROBOT.createScreenCapture(rect);
                    if (Utils.countColor(bi, rgb) < 24) {
                        stop = true;
                    }
                    //realiza o loop a cada 1 segundo
                    Utils.sleep(1000);
                }
            }
        });
        checkStop.setDaemon(true);
        checkStop.start();
    }

    /**
     * Força a parada do bot
     */
    public static void stop() {
        stop = true;
    }

    /**
     * Inicia o bot
     */
    public synchronized static void start() {
        if (started) {
            return;
        }
        started = true;
        stop = false;
        try {
            //captura a área jogável (campos e minas) da janela do campo minado
            Rectangle rect = DiscoverArea.discover();
            if (rect == null) {
                Main.notification("Não foi possível localizar a janela do jogo.");
                return;
            }
            RECTANGLE.setBounds(rect);
            start(rect);
        } finally {
            RECTANGLE.x = -1;
            started = false;
        }
    }

    /**
     * Inicia a execução do bot na área passada por parâmetro.
     *
     * @param rect área jogável da janela do campo minado
     */
    private static void start(Rectangle rect) {
        try {
            //captura a área de cada quadradinho do campo minado
            final Rectangle[][] tilesRect = Utils.rectToGrid(rect);
            //cria os objetos que representam cada quadradinho do campo minado
            final Tile[][] tiles = new Tile[tilesRect.length][tilesRect[0].length];
            for (int x = 0; x < tiles.length; x++) {
                for (int y = 0; y < tiles[0].length; y++) {
                    tiles[x][y] = new Tile(tilesRect[x][y], x, y);
                }
            }
            //escolhe um quadradinho aleatoriamente para iniciar o jogo
            final int xRandom = Utils.random(tiles.length) - 1;
            final int yRandom = Utils.random(tiles[0].length) - 1;
            //aqui damos 2 cliques pois o primeiro clique apenas transferirá o foco
            //para a janela do campo minado, o segundo irá efetivamente iniciar o jogo
            tiles[xRandom][yRandom].click(tiles);
            tiles[xRandom][yRandom].click(tiles);

            //loop enquanto não há nada forçando a parada do bot
            //os métodos 'check' e 'execute' podem lançar exception, e é assim que
            //o código sairá do loop para encerrar o bot
            while (!stop) {
                //marca as bandeiras
                markFlagBomb(tiles);
                //executa um clique
                execute(tiles);
                //verifica se alguma bomba foi clicada
                check(tiles);
            }
        } catch (CampoMinadoAIException ex) {
            //Jogo finalizado 'normalmente' através da exception personalizada
            Main.notification(ex.getMessage());
        } catch (Exception ex) {
            //Exceção inesperada
            String text = "EXCEPTION\n"
                    + ex.getMessage()
                    + '\n' + Stream.of(ex.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));
            JTextArea ta = new JTextArea(text, 10, 64);
            ta.setCaretPosition(0);
            JOptionPane.showMessageDialog(null, new JScrollPane(ta));
        }
    }

    /**
     * Marca as bandeiras nos quadrados em que se tem certeza de que são bombas,
     * utilizando a lógica simples da quantidade de quadrados não clicados em
     * volta de um número.
     *
     * @param tiles grade do campo minado
     */
    private static void markFlagBomb(Tile[][] tiles) {
        //verifica todos os quadrados que são números (ou seja, já foram clicados)
        //e verifica se a quantidade de quadrados não clicados em volta é igual
        //ao próprio número central, se sim, marca todos os quadrados não clicados
        //em volta com bandeira (ou seja, com certeza há uma bomba)
        Utils.forEach(tiles, t -> {
            if (!t.isNumber()) {
                return;
            }
            List<Tile> around = t.getAround(tiles);
            if (Utils.countNonClicked(around) == t.getEnumTile().ordinal()) {
                around.forEach(Tile::markFlagBomb);
            }
        });
    }

    /**
     * Executa todos os cliques 'seguros' (onde se tem certeza de que não tem
     * bomba, de acordo com as bandeiras marcadas). Caso não seja encontrado
     * nenhum clique 'seguro', o método 'notFound' será executado.
     *
     * @param tiles grade do campo minado
     */
    private static void execute(Tile[][] tiles) {
        //array para poder ser utilizado em classe anônima
        boolean[] hack = {false};
        //verifica todos os quadrados que são números (ou seja, já foram clicados)
        //e verifica se a quantidade de bandeiras marcadas em volta do número central
        //é igual ao número central
        //se sim, verifica se há quadrados em volta desse número que ainda não foram
        //clicados e não são bombas, e então clica nesses quadrados, pois com certeza
        //não são bombas
        Utils.forEach(tiles, t -> {
            if (!t.isNumber()) {
                return;
            }
            List<Tile> around = t.getAround(tiles);
            long flags = countFlagBomb(around);
            if ((flags == t.getEnumTile().ordinal()) && (flags < around.size())) {
                hack[0] = true; //ou seja, pelo menos uma jogada foi realizada
                click(around, tiles);
            }
        });
        //se nenhuma clique foi realizado nessa execução, chama o método 'notFound'
        if (!hack[0]) {
            notFound(tiles);
        }
    }

    /**
     * Executa um clique nos quadrados passados por parâmetro.
     *
     * @param around quadrados a serem clicados
     * @param tiles grade do campo minado
     */
    private static void click(List<Tile> around, Tile[][] tiles) {
        for (Tile t : around) {
            //executa o clique apenas se o quadrado não estiver marcado com uma bandeira
            if (!t.isFlagBomb()) {
                t.click(tiles);
            }
        }
    }

    /**
     * Método executado quando não se encontra uma jogada 'segura'.<br>
     * Este método irá procurar o local com a maior CHANCE de 'descobrir' um
     * quadrado sem bomba, desconsiderando dedução/exclusão, simplesmente
     * calculando a quantidade de quadrados e bandeiras ao redor de um número.
     *
     * @param tiles grade do campo minado
     */
    private static void notFound(Tile[][] tiles) {
        List<Tile> founds = new ArrayList<>();
        //array para poder ser utilizado em classe anônima
        double[] rate = {1d};
        Utils.forEach(tiles, t -> {
            if (!t.isNumber()) {
                return;
            }

            List<Tile> around = t.getAround(tiles);
            long flags = Utils.countFlagBomb(around);
            double nonClicked = around.size() - flags;
            double remainingBombs = t.getNumber() - flags;

            //verifica qual é a chance de clicar em um quadrado que não seja bomba
            //e se essa chance é a menor já encontrada
            //se sim, armazena os valores
            if ((remainingBombs / nonClicked) < rate[0]) {
                rate[0] = remainingBombs / nonClicked;
                founds.clear();
                around.stream().filter(Tile::isTile).forEach(founds::add);
            }
        });
        //caso não tenha sido encontrado um local para 'tentar a sorte'
        //capturamos todos os quadrados que ainda não foram clicados
        if (founds.isEmpty()) {
            Utils.forEach(tiles, t -> {
                if (t.isTile()) {
                    founds.add(t);
                }
            });
        }
        //caso tenhamos quadrados para 'tentar a sorte', clicamos em um aleatoriamente
        if (!founds.isEmpty()) {
            founds.get(Utils.random(founds.size()) - 1).click(tiles);
        } else {
            //caso não tenha sido encontrado pelo menos um quadrado disponível para 
            //ser clicado, damos o jogo por finalizado
            throw new CampoMinadoAIException("Jogo finalizado!");
        }
    }

    /**
     * Verifica se alguma bomba foi clicada e/ou se tem algum quadrado ainda
     * disponível para ser clicado
     *
     * @param tiles grade do campo minado
     */
    private static void check(Tile[][] tiles) {
        boolean[] flag = {false};
        Utils.forEach(tiles, t -> {
            if (t.getEnumTile().isBomb()) {
                throw new CampoMinadoAIException("Oh! Desculpe! Perdemos o jogo!");
            }
            if (t.isTile()) {
                flag[0] = true;
            }
        });
        if (!flag[0]) {
            throw new CampoMinadoAIException("Jogo finalizado!");
        }
    }
}

/**
 * Exception personalizada para encerrar o bot
 *
 * @author Vinicius Silva
 */
class CampoMinadoAIException extends RuntimeException {

    public CampoMinadoAIException(String string) {
        super(string);
    }

}
