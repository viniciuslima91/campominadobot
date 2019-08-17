package com.limagiran.campominadobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author Vinicius Silva
 */
public class AdvancedSearch1 {

    /**
     * Procura um clique seguro baseado em lógicas avançadas.<br>
     * Por exemplo, fazendo força bruta das possíveis posições de bomba e
     * vefificando se há somente uma configuração válida para determinado
     * conjunto de quadrados.
     *
     * @param _tiles grade do campo minado
     * @return quadrado com clique seguro encontrado ou {@code null} para não
     * encontrado.
     */
    public static Tile search(Tile[][] _tiles) {
        final Tile[][] tiles = Utils.clone(_tiles);
        try {
            final Map<Tile, List<Tile>> mapAroundCache = new HashMap<>();
            final List<List<Tile>> candidateGroups = new ArrayList<>();
            final HashSet<String> candidateGroupsAlreadyAddedID = new HashSet<>();
            Utils.forEach(tiles, t -> {
                if (!t.isNumber()) {
                    return;
                }
                List<Tile> aroundTemp = t.getAround(tiles);
                mapAroundCache.put(t, aroundTemp);
                if (!Utils.anyIsTile(aroundTemp)) {
                    return;
                }
                final TreeSet<Tile> candidates = new TreeSet<>();
                fillRecursiveIsNumberAndAnyTileAround(Arrays.asList(t), tiles, candidates, mapAroundCache);
                if (candidates.size() <= 1) {
                    return;
                }
                StringBuilder sbHash = new StringBuilder(candidates.size() * 12);
                for (Tile tCandidate : candidates) {
                    sbHash.append(tCandidate.x).append(',').append(tCandidate.y).append(';');
                }
                String hash = sbHash.toString();
                if (candidateGroupsAlreadyAddedID.contains(hash)) {
                    return;
                }
                candidateGroupsAlreadyAddedID.add(hash);
                candidateGroups.add(new ArrayList<>(candidates));
            });
            candidateGroups.sort((l1, l2) -> Integer.compare(l2.size(), l1.size()));
            mapAroundCache.forEach((tile, around) -> {
                final int countBomb = Utils.countFlagBomb(around);
                if (countBomb > 0) {
                    tile.setEnumTile(EnumTile.VALUES[tile.getEnumTile().ordinal() - countBomb]);
                }
                around.removeIf(t -> !t.isTile());
            });
            mapAroundCache.forEach((tile, around) -> around.removeIf(t -> !t.isTile()));
            final List<Tile> allAroundDistinct = new ArrayList<>(128);
            for (List<Tile> group : candidateGroups) {
                allAroundDistinct.clear();
                group.forEach(t -> {
                    mapAroundCache.get(t).forEach(t2 -> {
                        if (!allAroundDistinct.contains(t2)) {
                            allAroundDistinct.add(t2);
                        }
                    });
                });
                if (allAroundDistinct.size() > 10) {
                    continue;
                }
                final int n = (int) Math.pow(2, allAroundDistinct.size());
                Tile candidate, _return = null;
                for (int binary = 0; binary < n; binary++) {
                    candidate = null;
                    boolean flag;
                    for (int i = 0; i < allAroundDistinct.size(); i++) {
                        flag = (((binary >>> i) & 1) != 0);
                        allAroundDistinct.get(i).setEnumTile(flag ? EnumTile.FLAG : EnumTile.TILE);
                        if (!flag && candidate == null) {
                            candidate = allAroundDistinct.get(i);
                        }
                    }
                    boolean valid = true;
                    for (Tile t : group) {
                        int countFlagBomb = Utils.countFlagBomb(mapAroundCache.get(t));
                        if (t.getNumber() != countFlagBomb) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        if (_return != null) {
                            _return = null;
                            break;
                        }
                        _return = candidate;
                    }
                }
                if (_return == null) {
                    continue;
                }
                for (Tile[] row : _tiles) {
                    for (Tile t : row) {
                        if (t.x == _return.x && t.y == _return.y) {
                            return t;
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void fillRecursiveIsNumberAndAnyTileAround(List<Tile> tiles, Tile[][] grid, TreeSet<Tile> toFill, Map<Tile, List<Tile>> mapAroundCache) {
        for (Tile t : tiles) {
            if (toFill.contains(t)) {
                continue;
            }
            if (!t.isNumber()) {
                continue;
            }
            List<Tile> around = t.getAround(grid);
            mapAroundCache.put(t, around);
            if (!Utils.anyIsTile(around)) {
                continue;
            }
            toFill.add(t);
            fillRecursiveIsNumberAndAnyTileAround(t.getAroundAll(grid), grid, toFill, mapAroundCache);
        }
    }
}
