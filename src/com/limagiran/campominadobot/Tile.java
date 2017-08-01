package com.limagiran.campominadobot;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 *
 * @author Vinicius Lima
 */
public class Tile {

    /**
     * Área do quadrado
     */
    public final Rectangle rect;

    /**
     * Coordenada x do quadrado na grade do campo minado
     */
    public final int x;

    /**
     * Coordenada y do quadrado na grade do campo minado
     */
    public final int y;

    /**
     * Tipo do quadrado (se está clicado, se é bandeira, etc...)
     */
    private EnumTile enumTile = EnumTile.TILE;

    public Tile(Rectangle rect, int x, int y) {
        this.rect = rect;
        this.x = x;
        this.y = y;
    }

    /**
     * Executa o clique do quadrado atual, identificando o tipo do quadrado após
     * clicado.
     *
     * @param tiles grade do campo minado
     */
    public synchronized void click(Tile[][] tiles) {
        if (!enumTile.isClicked()) {
            Utils.ROBOT.mouseMove((int) rect.getCenterX(), (int) rect.getCenterY());
            Utils.ROBOT.mousePress(InputEvent.BUTTON1_MASK);
            Utils.ROBOT.mouseRelease(InputEvent.BUTTON1_MASK);
            resolve(tiles, null);
        }
    }

    /**
     * Identifica o tipo do quadrado
     *
     * @param tiles grade do campo minado
     * @param screenshot screenshot da tela. Caso seja {@code null}, uma
     * screenshot será tirada durante a execução do método. Este método será
     * chamado recursivamente e reutilizará a screenshot em todas as chamadas.
     */
    public synchronized void resolve(Tile[][] tiles, BufferedImage screenshot) {
        if (!enumTile.isClicked()) {
            final BufferedImage _screenshot = (screenshot != null) ? screenshot : Utils.screenshot();
            //pegamos apenas o primeiro quarto da imagem por economia de processamento, 
            //pois é possível realizar a identificação corretamente apenas com o primeiro quarto da imagem
            enumTile = EnumTile.resolve(_screenshot.getSubimage(rect.x, rect.y, rect.width / 2, rect.height / 2));
            if (enumTile == EnumTile.CLICKED) {
                getAround(tiles).forEach(t -> t.resolve(tiles, _screenshot));
            }
        }
    }

    /**
     * Retorna o tipo de quadrado
     *
     * @return tipo de quadrado
     */
    public EnumTile getEnumTile() {
        return enumTile;
    }

    /**
     * Retorna se o quadrado é uma 'bandeira'
     *
     * @return {@code true} se é bandeira. {@code false} o contrário.
     */
    public boolean isFlagBomb() {
        return enumTile.isFlagBomb();
    }

    /**
     * Retorna se o quadrado é uma bomba
     *
     * @return {@code true} se é bomba. {@code false} o contrário.
     */
    public boolean isBomb() {
        return enumTile.isBomb();
    }

    /**
     * Retorna se o quadrado já foi clicado.
     *
     * @return {@code true} para quadrado clicado. {@code false} o contrário.
     */
    public boolean isClicked() {
        return enumTile.isClicked();
    }

    /**
     * Retorna se o quadrado ainda não foi clicado.
     *
     * @return {@code true} para quadrado ainda não clicado. {@code false} o
     * contrário.
     */
    public boolean isTile() {
        return enumTile.isTile();
    }

    /**
     * Retorna se o quadrado é um número (de 1 a 7)
     *
     * @return {@code true} se é um número. {@code false} o contrário.
     */
    public boolean isNumber() {
        return ((enumTile.ordinal() >= 1) && (enumTile.ordinal() <= 7));
    }

    /**
     * Retorna o número do quadrado (quantidade de bombas ao redor).
     *
     * @return número do quadrado (quantidade de bombas ao redor)
     */
    public int getNumber() {
        return enumTile.ordinal();
    }

    /**
     * Executa o clique do mouse com o botão direito para realizar a marcação de
     * uma bomba no quadrado.
     */
    public synchronized void markFlagBomb() {
        if (!isFlagBomb()) {
            enumTile = EnumTile.FLAG;
            Utils.ROBOT.mouseMove((int) rect.getCenterX(), (int) rect.getCenterY());
            Utils.ROBOT.mousePress(InputEvent.BUTTON3_MASK);
            Utils.ROBOT.mouseRelease(InputEvent.BUTTON3_MASK);
        }
    }

    /**
     * Retorna os quadrados ao redor deste quadrado
     *
     * @param tiles grade do campo minado
     *
     * @return quadrados ao redor deste quadrado que ainda não foram clicados.
     * @see Utils#getAround(int, int, Tile[][]) Utils.getAround(x, y, tiles)
     */
    public List<Tile> getAround(Tile[][] tiles) {
        return Utils.getAround(x, y, tiles);
    }
}
