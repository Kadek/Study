/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer.datastructure;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author adas
 */
public class Node implements Comparable{

    @Override
    public String toString() {
        return "Node{" + "name=" + name + ", timeRequired=" + timeRequired + ", timeOpen=" + timeOpen + ", timeClose=" + timeClose + ", open=" + open + ", x=" + x + ", y=" + y + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.x);
        hash = 71 * hash + Objects.hashCode(this.y);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (this.open != other.open) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.timeRequired, other.timeRequired)) {
            return false;
        }
        if (!Objects.equals(this.timeOpen, other.timeOpen)) {
            return false;
        }
        if (!Objects.equals(this.timeClose, other.timeClose)) {
            return false;
        }
        if (!Objects.equals(this.x, other.x)) {
            return false;
        }
        if (!Objects.equals(this.y, other.y)) {
            return false;
        }
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeRequired(Long timeRequired) {
        this.timeRequired = timeRequired;
    }

    public void setTimeOpen(Long timeOpen) {
        this.timeOpen = timeOpen;
    }

    public void setTimeClose(Long timeClose) {
        this.timeClose = timeClose;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public void setY(Long y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public Long getTimeRequired() {
        return timeRequired;
    }

    public Long getTimeOpen() {
        return timeOpen;
    }

    public Long getTimeClose() {
        return timeClose;
    }

    public boolean isOpen() {
        return open;
    }

    public Long getX() {
        return x;
    }

    public Long getY() {
        return y;
    }
    public boolean isVisited(){
        return visited;
    }
    public void setVisited(boolean bool){
        this.visited = bool;
    }
    public int whatColor(){
        return color;
    }
    public void setColor(int color){
        this.color = color;
    }
    private String name;
    private Long timeRequired;
    private Long timeOpen;
    private Long timeClose;
    private boolean open;
    private boolean visited;
    private int color;
    private Long x;
    private Long y;
    public ArrayList<Connection> adjacencies;
    public ArrayList<Connection> adjacencies_tasked;
    public double minDistance = Double.POSITIVE_INFINITY;
    public Node previous;
    public Node(String name, Long timeRequired, Long timeOpen, Long timeClose, Long x, Long y, boolean open) {
        
        this.name = name;
        this.timeRequired = timeRequired;
        this.timeOpen = timeOpen;
        this.timeClose = timeClose;
        this.open = open;
        this.x = x;
        this.y = y;
        this.color=-1;
        this.adjacencies = new ArrayList<>();
        this.adjacencies_tasked = new ArrayList<>();
    }

    @Override
    public int compareTo(Object o) {
         return Integer.compare(this.hashCode(), o.hashCode());
    }

    
}
