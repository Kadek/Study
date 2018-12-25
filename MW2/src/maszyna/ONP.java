/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

import java.util.Stack;

/**
 *
 * @author adam
 */
public class ONP {
    public ONP(){
    }
    
    protected String transformToONP(String lewa){
        String tmp, ret = "";
        Stack<String> stos = new Stack();
        
        for(int i = 0; i < lewa.length(); i ++){
            tmp = lewa.substring(i, i+1);
            switch(tmp){
                case "T":
                    ret = ret + tmp;
                    break;
                case "F":
                    ret = ret + tmp;
                    break;
                case "!":
                    stos.push(tmp);
                    break;
                case "&":
                    stos.push(tmp);
                    i++;
                    break;
                case "|":
                    stos.push(tmp);
                    i++;
                    break;
                case "(":
                    stos.push(tmp);
                    break;
                case ")":
                    while(!stos.peek().equals("(")){
                        ret = ret + stos.pop();
                    }
                    stos.pop();
                case " ":
                    break;
                default:
                    break;
            }
        }
        
        while(!stos.empty()){
            ret = ret + stos.pop();
        }
        return ret;
    }
        
    protected String computeONP(String lewa){
        Stack<String> stos = new Stack();
        String a,b;
        System.out.println(lewa);
        
        for(int i = 0; i < lewa.length(); i++){
            String tmp = lewa.substring(i, i +1);
            switch(tmp){
                case "T":
                    stos.push(tmp);
                    break;
                case "F":
                    stos.push(tmp);
                    break;
                case "&":
                    a = stos.pop();
                    b = stos.pop();
                    if(a.equals("F") || b.equals("F")){
                        stos.push("F");
                    }else{
                        stos.push("T");
                    }
                    break;
                case "|":
                    a = stos.pop();
                    b = stos.pop();
                    if(a.equals("T") || b.equals("T")){
                        stos.push("T");
                    }else{
                        stos.push("F");
                    }
                    break;
                case "!":
                    a = stos.pop();
                    if(a.equals("T")){a = "F";}else{a = "T";};
                    stos.push(a);
                    break;
                default:
                    break;
            }
        }
        
        return stos.pop();
    }
}
