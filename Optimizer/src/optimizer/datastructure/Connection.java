/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer.datastructure;

import java.util.Objects;

/**
 *
 * @author adas
 */
public class Connection {

    private String name;

    @Override
    public String toString() {
        return "Connection: " + this.attach1.getName() + " - " + this.attach2.getName() + " Value: " + this.timeRequired;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.name);
        hash = 43 * hash + Objects.hashCode(this.attach1);
        hash = 43 * hash + Objects.hashCode(this.attach2);
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
        final Connection other = (Connection) obj;
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
        if (!Objects.equals(this.attach1, other.attach1)) {
            return false;
        }
        if (!Objects.equals(this.attach2, other.attach2)) {
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

    public void setAttach1(Node attach1) {
        this.attach1 = attach1;
    }

    public void setAttach2(Node attach2) {
        this.attach2 = attach2;
    }
    private Long timeRequired;
    private Long timeOpen;
    private Long timeClose;
    private boolean open;
    private Node attach1;
    private Node attach2;
    
    public Connection(String name, Long timeRequired, Long timeOpen, Long timeClose, boolean open, Node attach1, Node attach2) {
        
        this.name = name;
        this.timeRequired = timeRequired;
        this.timeOpen = timeOpen;
        this.timeClose = timeClose;
        this.open = true;
        this.attach1 = attach1;
        this.attach2 = attach2;
        
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

    public Node getAttach1() {
        return attach1;
    }

    public Node getAttach2() {
        return attach2;
    }
        
    public Node getNeighbour(Node nodeIndex) {
        if (this.attach1 == nodeIndex) {
            return getAttach2();
        } else {
            return getAttach1();
        }
    }
    
}
