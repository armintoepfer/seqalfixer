/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.saf.utils;

/**
 * Parallel Loop Body Interface
 */
public interface LoopBody<T> {

    void run(T p);
}

