/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer.datastructure;

/**
 *
 * @author adas
 */
public class Pair<E1, E2> {

    private E1 first;
    private E2 second;
    
    public E1 getFirst() {
        return this.first;
    }

    public E2 getSecond() {
        return this.second;
    }

    public void setFirst(E1 first) {
        this.first = first;
    }

    public void setSecond(E2 second) {
        this.second = second;
    }
    
}
