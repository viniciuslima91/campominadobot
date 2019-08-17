package com.limagiran.campominadobot;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Classe responsável por identificar a área do campo minado
 *
 * @author Vinicius Lima
 */
public class DiscoverArea {

    /**
     * Retorna a área jogável de uma janela de campo minado
     *
     * @return área jogavel ou {@code null} para área não identificada
     */
    public static Rectangle discover() {
        List<int[]> pixels = getBorders(Utils.screenshot());

        List<Integer> horizontal = search(pixels, true);
        List<Integer> vertical = search(pixels, false);
        if (!validateList(horizontal, 2) || !validateList(vertical, 3)) {
            return null;
        }
        Rectangle rect = new Rectangle();
        rect.x = horizontal.get(0) + 3;
        rect.y = vertical.get(1) + 3;
        rect.width = horizontal.get(horizontal.size() - 1) + 2 - rect.x;
        rect.height = vertical.get(vertical.size() - 1) + 2 - rect.y;
        return (validateArea(rect) ? rect : null);
    }

    /**
     * Captura todas as coordenadas da tela em que a cor do pixel seja
     * rgb(128,128,128), que é a cor delimitadora de cada quadradinho do campo
     * minado.
     *
     * @param screenshot screenshot da tela
     * @return lista de coordenadas capturadas em que pixel = rgb(128,128,128)
     */
    private static List<int[]> getBorders(BufferedImage screenshot) {
        final int rgbGray = new Color(128, 128, 128).getRGB();
        List<int[]> _return = new ArrayList<>();
        for (int x = 0; x < screenshot.getWidth(); x++) {
            for (int y = 0; y < screenshot.getHeight(); y++) {
                final int rgb = screenshot.getRGB(x, y);
                if (rgb == rgbGray) {
                    _return.add(new int[]{x, y});
                }
            }
        }
        return _return;
    }

    /**
     * Procura todas as coordenadas (x ou y, depende da variável
     * {@code horizontal}) em que há uma sequência de pixels formando uma linha
     * de pelo menos 2 pixels de espessura por 100 pixels de comprimento,
     * considerando então que seja um 'candidato' para ser a área delimitadora
     * do campo minado.<br>
     * Obs.: O comprimento não precisa ser 100 pixels sequenciais, podendo
     * perceber no método 'checkLineExists' que uma distância (separação) de até
     * 2 pixels entre as coordenadas é aceitável, visto que os quadradinhos do
     * campo minado não são TOTALMENTE ligados um ao outro pelo pixel
     * rgb(128,128,128)
     *
     * @param coords lista de coordenadas em que pixel = rgb(128,128,128)
     * @param horizontal {@code true} para procurar linhas na horizontal.
     * {@code false} para procurar na vertical.
     * @return lista de coordenadas (x ou y, depende da variável
     * {@code horizontal}) em que foram encontradas linhas que podem ser a borda
     * do campo minado
     */
    private static List<Integer> search(List<int[]> coords, boolean horizontal) {
        List<Integer> _return = new ArrayList<>();
        //a fim de evitar procurar uma coordenada x/y que já foi procurada,
        //fazemos um distinct de todas as coordenadas
        List<Integer> distinct = new ArrayList<>();
        for (int[] array : coords) {
            final int value = array[horizontal ? 0 : 1];
            if (!distinct.contains(value)) {
                distinct.add(value);
            }
        }
        Collections.sort(distinct);
        for (int i = 0; i < distinct.size() - 1; i++) {
            //verifica se há uma sequência de pixels formando uma linha de pelo
            //menos 2 pixels de espessura por 100 pixels de comprimento
            if (checkLineExists(distinct.get(i), coords, horizontal)
                    && checkLineExists(distinct.get(i + 1), coords, horizontal)) {
                _return.add(distinct.get(i));
            }
        }
        return _return;
    }

    /**
     * Verifica se existe uma linha de pelo menos 100 pixels (coordenadas),
     * aceitando até 2 pixels de distância (separação) entre cada ponto.
     *
     * @param coord coordenada x/y (depende do {@code horizontal})
     * @param coords lista de coordenadas com pixel = rgb(128,128,128)
     * @param horizontal {@code true} para procurar linhas na horizontal.
     * {@code false} para procurar na vertical.
     * @return se {@code coord} contém uma linha de acordo com as condições
     */
    private static boolean checkLineExists(int coord, List<int[]> coords, boolean horizontal) {
        List<Integer> values = new ArrayList<>();
        for (int[] p : coords) {
            if (p[horizontal ? 0 : 1] == coord) {
                values.add(p[horizontal ? 1 : 0]);
            }
        }
        Collections.sort(values);
        int count = 0;
        for (int i = 1; i < values.size(); i++) {
            int x1 = values.get(i - 1);
            int x2 = values.get(i);
            count = (((x1 + 1 == x2) || (x1 + 2 == x2) || (x1 + 3 == x2)) ? (count + 1) : 0);
            if (count >= 100) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se a lista é maior ou igual à quantidade mínima ({@code min}), e
     * se existem pelo menos dois valores diferentes.
     *
     * @param list lista de coordenadas
     * @param min quantidade mínima de objetos na lista
     * @return {@code true} para lista válida. {@code false} o contrário.
     */
    private static boolean validateList(List<Integer> list, int min) {
        return ((list.size() >= min) && !list.isEmpty() && !Objects.equals(list.get(0), list.get(list.size() - 1)));
    }

    /**
     * Verifica se a área capturada é valida.<br>
     * A área precisa ser maior do que 0 (width e height) e precisa ser
     * divisível por 16 (width e height).
     *
     * @param rect área capturada
     * @return {@code true} para área válida. {@code false} o contrário.
     */
    private static boolean validateArea(Rectangle rect) {
        return ((rect.width % 16 == 0) && (rect.height % 16 == 0) && ((rect.width > 0) && (rect.height > 0)));
    }

}
