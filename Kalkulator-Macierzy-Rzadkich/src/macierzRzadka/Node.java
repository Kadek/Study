package macierzRzadka;

import java.util.Objects;

/**
 *
 * @author kasperoa
 */
public class Node {
    private double x;
    private int index;
    private Node left;
    private Node right;
    
    public Node(double x, int index){
        this.x = x;
        this.index = index;
    }
        
    public double getX(){
        return x;
    }
    
    public int getIndex(){
        return index;
    }
    
    public Node getLeft(){
        return left;
    }
    
    public boolean hasLeft(){
        return left != null;
    }
    
    public Node getRight(){
        return right;
    }
    
    public boolean hasRight(){
        return right != null;
    }   
        
    public void setX(double x){
        this.x = x;
    }
        
    public void setLeft(Node left){
        this.left = left;
    }
        
    public void setRight(Node right){
        this.right = right;
    }
        
    @Override
    public String toString(){
        return "Indeks : " + index + " wartość: " + x;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (!Objects.equals(this.left, other.left)) {
            return false;
        }
        if (!Objects.equals(this.right, other.right)) {
            return false;
        }
        return true;
    }

}
