package com.limagiran.campominadobot;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Representa cada tipo de quadrado diferente possível. (desconsiderei o número
 * 8)
 *
 * @author Vinicius Lima
 */
public enum EnumTile {
    TILE(Color.WHITE),
    _1(Color.BLUE),
    _2(new Color(0, 128, 0)),
    _3(Color.RED),
    _4(new Color(0, 0, 128)),
    _5(new Color(128, 0, 0)),
    _6(new Color(0, 128, 128)),
    _7(Color.BLACK),
    CLICKED(new Color(192, 192, 192)),
    BOMB(Color.WHITE),
    FLAG(new Color(1, 2, 3));
    
    public static final EnumTile[] VALUES = values();

    /**
     * Cor que distingue cada quadradinho
     */
    public final Color color;

    private EnumTile(Color color) {
        this.color = color;
    }

    /**
     * Cor da borda de cada quadradinho do campo minado. Utilizamos esta cor rgb
     * para DESCONSIDERARMOS este pixel na nossa identificação do tipo de
     * quadradinho.
     */
    private static final int SKIP_RGB = new Color(128, 128, 128).getRGB();

    /**
     * Cor de fundo de cada quadradinho.
     */
    private static final int BACKGROUND_TILE_RGB = new Color(192, 192, 192).getRGB();

    /**
     * Identifica cada quadradinho utilizando os pixels da imagem passada por
     * parâmetro.<br>
     * Utiliza um mapa de pixels para identificar quais são as maiores
     * ocorrências de cores, assim podemos então identificar, por exemplo,
     * quando a cor vermelha é a SEGUNDA cor mais encontrada então o número é 3
     * (a cor rgb(192,192,192) é a mais encontrada pois é a cor de fundo).
     *
     * @param tileImg imagem do quadradinho
     * @return tipo de quadradinho de acordo com as condições
     */
    public static EnumTile resolve(final BufferedImage tileImg) {
        Map<Color, Integer> map = new HashMap<>();
        for (int x = 0; x < tileImg.getWidth(); x++) {
            for (int y = 0; y < tileImg.getHeight(); y++) {
                //captura a cor de cada pixel e verifica se não é cor de borda
                Color c = new Color(tileImg.getRGB(x, y), true);
                if (c.getRGB() != SKIP_RGB) {
                    map.put(c, map.getOrDefault(c, 0) + 1);
                }
            }
        }
        //captura as entradas do mapa e ordena de acordo com a quantidade de pixels
        //encontrados para cada cor diferente
        List<Map.Entry<Color, Integer>> list = map.entrySet()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()))
                .collect(Collectors.toList());

        //método de segurança caso a lista esteja vazia
        if (list.isEmpty()) {
            return TILE;
        }
        int whitePixel = map.getOrDefault(Color.WHITE, 0);
        int blackPixel = map.getOrDefault(Color.BLACK, 0);
        //caso haja exatamente 4 pixels brancos e mais do que 8 pixels pretos, 
        //consideramos que o quadrado é uma bomba
        if ((whitePixel == 4) && (blackPixel > 8)) {
            return BOMB;
        }
        int redPixel = map.getOrDefault(Color.BLACK, 0);
        //caso haja mais do que 2 pixels brancos e mais do que 2 pixels vermelhos, 
        //consideramos que o quadrado é uma bandeira
        if ((whitePixel > 2) && (redPixel > 2)) {
            return FLAG;
        }

        //cor mais encontrada
        Color[] c = {list.get(0).getKey()};
        //verifica se a cor mais encontrada é a cor de fundo, se sim, pega a segunda
        //cor mais encontrada
        if ((list.size() > 1) && (c[0].getRGB() == BACKGROUND_TILE_RGB)) {
            c[0] = list.get(1).getKey();
        }
        //verifica qual é o tipo de quadrado de acordo com a cor mais encontrada
        //(desconsiderando a cor de fundo)
        //caso não encontre um tipo, considera-se o quadrado como clicado
        return Stream.of(EnumTile.values())
                .filter(en -> en.color.getRGB() == c[0].getRGB())
                .findFirst()
                .orElse(CLICKED);
    }

    /**
     * Verifica se é um quadrado não clicado
     *
     * @return {@code true} para quadrado não clicado. {@code false} o
     * contrário.
     */
    public boolean isTile() {
        return this == TILE;
    }

    /**
     * Verifica se é um quadrado clicado
     *
     * @return {@code true} para clicado. {@code false} o contrário.
     */
    public boolean isClicked() {
        return this == CLICKED;
    }

    /**
     * Verifica se é uma bomba
     *
     * @return {@code true} para bomba. {@code false} o contrário.
     */
    public boolean isBomb() {
        return this == BOMB;
    }

    /**
     * Verifica se é uma bandeira
     *
     * @return {@code true} para bandeira. {@code false} o contrário.
     */
    public boolean isFlagBomb() {
        return this == FLAG;
    }
}
