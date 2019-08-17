package com.limagiran.campominadobot;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vinicius Silva
 */
public class SearchLucky {

    /**
     * Método executado quando não se encontra uma jogada 'segura'.<br>
     * Este método irá procurar o local com a maior CHANCE de 'descobrir' um
     * quadrado sem bomba, desconsiderando dedução/exclusão, simplesmente
     * calculando a quantidade de quadrados e bandeiras ao redor de um número.
     *
     * @param tiles grade do campo minado
     * @return retorna a melhor opção encontrada para aumentar a sorte
     * (probabilidade)
     */
    public static Tile search(Tile[][] tiles) {
        List<Tile> founds = new ArrayList<>(128);
        //array para poder ser utilizado em classe anônima
        double[] rate = {1d};
        Utils.forEach(tiles, t -> {
            if (!t.isNumber()) {
                return;
            }

            List<Tile> around = t.getAround(tiles);
            int flags = Utils.countFlagBomb(around);
            double nonClicked = around.size() - flags;
            double remainingBombs = t.getNumber() - flags;

            //verifica qual é a chance de clicar em um quadrado que não seja bomba
            //e se essa chance é a menor já encontrada
            //se sim, armazena os valores
            if ((remainingBombs / nonClicked) < rate[0]) {
                rate[0] = remainingBombs / nonClicked;
                founds.clear();
                for (Tile tile : around) {
                    if (tile.isTile()) {
                        founds.add(tile);
                    }
                }
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
        return founds.isEmpty() ? null : founds.get(Utils.random(founds.size()) - 1);
    }
}
