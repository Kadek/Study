/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drzewo.decyzyjne;

import com.sun.corba.se.impl.orbutil.GetPropertyAction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author adas
 */
public class DrzewoDecyzyjne {

    public void training() {
        
        System.out.println("------------------------------------------");
        System.out.println("TRENING");
        System.out.println("------------------------------------------");    
        InputInterface[] options = {new ConsoleInput(), new AdvancedInput()};
        
        for(int i = 0; i < options.length; i++){
            System.out.println(i + ". " + options[i].getTitle());
        }
        
        System.out.println("Powyżej przedstawiona została lista systemów do wczytywania danych");
        System.out.println("Wpisz numerek systemu którym chcesz się posłużyć");
        Scanner sc = new Scanner(System.in);
        int opt = -1;
        do{
            opt = sc.nextInt();
            if(opt == 0){
                System.out.println("Wybrano opcję: " + options[opt].getTitle());
                break;
            }else if(opt == 1){
                System.out.println("Wybrano opcję: " + options[opt].getTitle());
                break;
            }
            System.out.println("Nastąpił błąd. Proszę wybrać system ponownie");
        }while(1==1);
        
        System.out.println("Proszę podać nazwę pliku z danymi treningowymi");
        String fileName = "";
        do{
            sc.nextLine();
            fileName = sc.nextLine();
            File f = new File(fileName);
            if(f.exists() && !f.isDirectory()) { 
                System.out.println("Plik znaleziony");
                break;
            }
            System.out.println("Plik nie znaleziony. Proszę powtórzyć wybór");
        }while(1==1);
        
        System.out.println("Rozpoczynam tworzenie drzewa.");
        AlgorytmDrzewa algorytm = new AlgorytmDrzewa(options[opt]);
        DecisionTreeNode rootDecisionTreeNode = algorytm.train(fileName);
        System.out.println("Trening gotowy!");
        System.out.println("Podaj nazwę pliku do którego mam zapisać drzewo.");
        do{
            fileName = sc.nextLine();
            if(saveTree(rootDecisionTreeNode, fileName)){
                System.out.println("Zapis dokonany !");
                System.out.println("Dziękuję i życzę miłego dnia.");
                break;
            }else{
                System.out.println("Zapis nieudany !");
                System.out.println("Podaj inną nazwę pliku.");
            }
        }while(1==1);
        
    }
    public void predict(){
        System.out.println("------------------------------------------");
        System.out.println("PREDYKCJA");
        System.out.println("------------------------------------------");     
        
        Scanner sc = new Scanner(System.in);
        String fileName;
        DecisionTreeNode root;
        System.out.println("Podaj plik z drzewem decyzyjnym");
        do{
            fileName = sc.nextLine();
            File f = new File(fileName);
            if(f.exists() && !f.isDirectory()) { 
                System.out.println("Plik znaleziony");
                root = readDecisionTree(fileName);
                if(root != null){
                    System.out.println("Drzewo poprawnie załadowane");
                    break;
                }else{
                    System.out.println("Nie udało się załadować drzewa");
                }
            }else{
                System.out.println("Plik nie znaleziony. Proszę powtórzyć wybór");
            }
        }while(1==1);
        
        System.out.println("Proszę wpisać linijkę danych zgodnie z formatem kolumna:wartosc kolumna:wartosc ...");
        System.out.println("Proszę wpisać KONIEC, żeby zakończyć proces predykcji.");
        do{
            String currentLine = sc.nextLine();
            currentLine = currentLine.toLowerCase();
            if(currentLine.equals("koniec")){
                break;
            }
            String value = researchTree(currentLine, root);
            if(value == null){
                System.out.println("Nie udało sie znaleźć odpowiedzi.");
                System.out.println("Sprawdź czy na pewno podałeś dane w poprawnej formie.");
            }else{
                System.out.println(value);                    
            }
        }while(1==1);
        
        System.out.println("Dziękuję i życzę miłego dnia");
    }
    
    
    private String researchTree(String data, DecisionTreeNode root){
        ArrayList<ArrayList<String>> values = extractValues(data);
        if(values == null){return null;}
        while(!values.isEmpty()){
            if(root.getChildren().size() == 0){
                break;
            }
            
            // nazwa kolumny którą posiadają dzieci obecnego korzenia
            String currentColumn = root.getChildren().get(0).getColumn();
            
            // znajdź indeks data w której opisana jest ta kolumna
            int index = -1;
            for(int i = 0; i < values.size(); i++){
                if(values.get(i).get(0).equals(currentColumn)){
                    index = i;
                    break;
                }
            }
            if(index == -1){return null;}
            
            // znajdź dziecko korzenia z daną wartościa i ustaw go jako korzeń
            String currentValue = values.get(index).get(1);
            values.remove(index);
            for(DecisionTreeNode child : root.getChildren()){
                if(child.getValue().equals(currentValue)){
                    root = child;
                    break;
                }
            }
        }
        return formatOutput(root.getOptions());
    }
    
    private String formatOutput(HashMap<String, Double> output){
        
        //posortuj HashMape według wartości
        List<Map.Entry<String, Double>> list = new LinkedList<>(output.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>(){
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2){
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        
        String res = "";
        for(Map.Entry<String, Double> entry : list){
            if(entry.getValue() == 0.0){break;}
            res += entry.getKey() + " : " + entry.getValue() + " \n"; 
        }
        return res;
        
    }
    
    private ArrayList<ArrayList<String>> extractValues(String data){
        try{
            String[] splitData = data.split(" ");
            ArrayList<ArrayList<String>> res = new ArrayList<>();

            for(String oneSplit : splitData){
                String[] oneData = oneSplit.split(":");
                res.add(new ArrayList<>());
                res.get(res.size()-1).add(oneData[0]);
                res.get(res.size()-1).add(oneData[1]);
            }
            return res;
        }catch(Exception e){
            return null;
        }
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="wczytywanie drzewa z pliku">
    private DecisionTreeNode readDecisionTree(String fileName){        
        BufferedReader br = null;
        FileReader fr = null;
        DecisionTreeNode root;
                
        try {
            String currentLine;
            
            br = new BufferedReader(new FileReader(fileName));
            currentLine = br.readLine();
            HashMap<String,String> splitLine = processLine(currentLine);
            root = createDecisionTreeNode(splitLine, null);

            while ((currentLine = br.readLine()) != null) {
                splitLine = processLine(currentLine);
                DecisionTreeNode newNode = createDecisionTreeNode(splitLine, root);
                root = newNode;
            }
        
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                return null;
            }
        }
        
        root = getRootOfTree(root);
        return root;
    }
    
    private DecisionTreeNode createDecisionTreeNode(HashMap<String, String> values, DecisionTreeNode root){
        DecisionTreeNode newNode = new DecisionTreeNode(Double.parseDouble(values.get("entropy")), values.get("column"), values.get("value"));
        newNode.nTabs = Integer.parseInt(values.get("nTabs"));
        values.remove("nTabs");
        
        values.remove("entropy");
        values.remove("column");
        values.remove("value");
        
        for(String key : values.keySet()){
            newNode.addOption(key, Double.parseDouble(values.get(key)));
        }
        
        if(root != null){
            while(newNode.nTabs - root.nTabs != 1){
                root = root.getPredecessor();
            }
            newNode.setPredecessor(root);
            root.addChild(newNode);
        }
        
        return newNode;
    }
    
    private void showTree(DecisionTreeNode root){
        LinkedList<DecisionTreeNode> fifo = new LinkedList<>();
        fifo.add(root);
        while(!fifo.isEmpty()){
            DecisionTreeNode tmp = fifo.pollFirst();
            System.out.println(tmp.toString());
            ArrayList<DecisionTreeNode> children = tmp.getChildren();
            for(int i = 0; i < children.size(); i++){
                fifo.add(children.get(i));
            }
        }
    }
    
    private DecisionTreeNode getRootOfTree(DecisionTreeNode node){
        while(node.getPredecessor() != null){
            node = node.getPredecessor();
        }
        return node;
    }
    
    private HashMap<String,String> processLine(String currentLine){
        HashMap<String, String> res = new HashMap<>();
        res.put("nTabs", Integer.toString(currentLine.length() - currentLine.replaceAll("\t", "").length()));
        
        Pattern p = Pattern.compile("^[\t]*([a-zA-Z0-9-_]*):(.*) = .*$");
        Matcher m = p.matcher(currentLine);
        m.find();
        res.put("column", m.group(1));
        res.put("value", m.group(2));
        
        p = Pattern.compile("= ([\\d\\.\\d]*)");
        m = p.matcher(currentLine);
        m.find();
        res.put("entropy", m.group(1));
                
        p = Pattern.compile("(\\w*)=(\\d\\.\\d*)");
        m = p.matcher(currentLine);
        while(m.find()){
            res.put(m.group(1), m.group(2));
        }
        
        return res;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="zapisywanie gotowego drzewa">
    public boolean saveTree(DecisionTreeNode decisionTreeNode, String fileName){	
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            String content = "";
            content = formatTree(decisionTreeNode, content, 0);

            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);
            bw.write(content);

            System.out.println("Done");

        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                return false;
            }
        }
        
        return true;
    }
    
    private String formatTree(DecisionTreeNode decisionTreeNode, String content, int nTabs){
        for(int i = 0; i < nTabs; i++){
            content += "\t";
        }
        content += decisionTreeNode.toString();
        content += "\n";
        ArrayList<DecisionTreeNode> children = decisionTreeNode.getChildren();
        for(int i = 0; i < children.size(); i++){
            content = formatTree(children.get(i), content, nTabs + 1);
        }
        
        return content;
    }
    // </editor-fold>
}
