package com.limagiran.campominadobot;

/**
 *
 * @author Vinicius Lima
 * @param <T>
 * @param <U>
 * @param <R>
 */
@FunctionalInterface
public interface TileForEachConsumer<T extends Object, U extends Object, R extends Object> {

    public void accept(T t, U u, R r);
}
