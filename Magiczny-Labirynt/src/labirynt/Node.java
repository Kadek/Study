/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labirynt;

import java.util.HashMap;

/**
 *
 * @author adas
 */
public class Node {
    int x;
    int y;
    
    int sourceCost = 0;
    double heuristicCost = 0;
    
    Node predecessor = null;
    
    HashMap<String, Node> sides;
    
    public Node(int x, int y){
        this.x = x;
        this.y = y;
        
        sides = new HashMap<>();
        sides.put("Up", null);
        sides.put("Right", null);
        sides.put("Down", null);
        sides.put("Left", null);
    }
    
    @Override
    public String toString(){
        int res = 0;
        if(sides.get("Up") != null){
            res += 1;
        }
        if(sides.get("Right") != null){
            res += 10;
        }
        if(sides.get("Down") != null){
            res += 100;
        }
        if(sides.get("Left") != null){
            res += 1000;
        }
        String ans = Integer.toString(res);
        ans = "|" + ans + "|";
        return ans;
    }
    
    public String position(){
        return " x: " + x + " y: " + y;
    }
}
