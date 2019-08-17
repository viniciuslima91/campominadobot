package com.limagiran.campominadobot;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Vinicius Lima
 */
public class Tile implements Comparable<Tile> {

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
    /**
     * Área total da janela do campo minado
     */
    private final Rectangle totalArea;

    public Tile(Rectangle rect, int x, int y, Rectangle totalArea) {
        this.rect = rect;
        this.x = x;
        this.y = y;
        this.totalArea = new Rectangle(totalArea);
    }

    /**
     * Executa o clique do quadrado atual, identificando o tipo do quadrado após
     * clicado.
     *
     * @param tiles grade do campo minado
     * @param resolve resolver o quadrado, ou seja, tirar print e verificar
     * recursivamente o tipo do quadrado.
     */
    public synchronized void click(Tile[][] tiles, boolean resolve) {
        if (!enumTile.isClicked()) {
            Utils.ROBOT.mouseMove((int) rect.getCenterX(), (int) rect.getCenterY());
            Utils.ROBOT.mousePress(InputEvent.BUTTON1_MASK);
            Utils.ROBOT.mouseRelease(InputEvent.BUTTON1_MASK);
            if (resolve) {
                resolve(tiles, null);
            }
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
            final BufferedImage _screenshot = (screenshot != null) ? screenshot : Utils.screenshot(totalArea);
            //pegamos apenas o primeiro quarto da imagem por economia de processamento, 
            //pois é possível realizar a identificação corretamente apenas com o primeiro quarto da imagem
            enumTile = EnumTile.resolve(_screenshot.getSubimage(rect.x - totalArea.x, rect.y - totalArea.y, rect.width / 2, rect.height / 2));
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

    /**
     * Retorna todos os quadrados ao redor deste quadrado
     *
     * @param tiles grade do campo minado
     *
     * @return quadrados ao redor deste quadrado .
     * @see Utils#getAroundAll(int, int, com.limagiran.campominadobot.Tile[][])
     * Utils.getAroundAll(x, y, tiles)
     */
    public List<Tile> getAroundAll(Tile[][] tiles) {
        return Utils.getAroundAll(x, y, tiles);
    }

    /**
     * Verifica se é um quadrado que ainda não foi clicado
     *
     * @return {@code true} para quadrado ainda não clicado. {@code false} o
     * contrário.
     */
    public boolean isNonClicked() {
        return (isTile() || isFlagBomb());
    }

    /**
     * Área total do campo minado
     *
     * @return área total do campo minado
     */
    public Rectangle getTotalArea() {
        return new Rectangle(totalArea);
    }

    /**
     * Alterar o tipo do quadrado manualmente
     *
     * @param enumTile {@link EnumTile}
     */
    public void setEnumTile(EnumTile enumTile) {
        this.enumTile = enumTile;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(rect);
        hash = 73 * hash + x;
        hash = 73 * hash + y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tile) {
            Tile t = (Tile) obj;
            return t.x == x && t.y == y && Objects.equals(t.rect, rect);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "Tile{" + "x=" + x + ", y=" + y + ", enumTile=" + enumTile + '}';
    }

    @Override
    public int compareTo(Tile o) {
        return (o.y == y) ? Integer.compare(o.x, x) : Integer.compare(o.y, y);
    }

}
