package labirynt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

public class VGrid extends javax.swing.JPanel {

    private static int prevN = 0;
    private Dimension preferredSize;

    private final ArrayList<ArrayList<Node>> grid;
    int nRows;
    int nColumns;
    int nodeHeight;
    int nodeWidth;
    
    boolean selectionEnabled = true;
    int startX = -1;
    int startY = -1;
    
    int endX = -1;
    int endY = -1;
    
    boolean updatePath = false;
    PriorityQueue<Node> openSet = null;
    HashSet<Node> closedSet = null;
    HashSet<Node> newNodes = null;
    Node currentNode = null;

    public VGrid(ArrayList<ArrayList<Node>> grid) {
        
        this.grid = grid;
        this.nRows = grid.size();
        this.nColumns = grid.get(0).size();
        
        double rowsToColumnsRatio = (double)nRows/nColumns;
        
        if(rowsToColumnsRatio > 1.0){
            preferredSize = new Dimension( (int)(600/rowsToColumnsRatio), 600);
        }else if(rowsToColumnsRatio < 1.0){
            preferredSize = new Dimension( 600, (int)(600*rowsToColumnsRatio));            
        }else{
            preferredSize = new Dimension(600, 600);             
        }
        System.out.println(preferredSize.toString());
        updateNodeSize();

        addMouseWheelListener((MouseWheelEvent e) -> {
            updatePreferredSize(e.getWheelRotation(), e.getPoint());
        });
        
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addStartEndPoints(evt);
            }
        });

    }

    private void updatePreferredSize(int n, Point p) {

        if (n == 0) // ideally getWheelRotation() should never return 0. 
        {
            n = -1 * prevN;     // but sometimes it returns 0 during changing of zoom 
        }                            // direction. so if we get 0 just reverse the direction.

        double d = (double) n * 1.08;
        d = (n > 0) ? 1 / d : -d;

        int w = (int) (getWidth() * d);
        int h = (int) (getHeight() * d);
        preferredSize.setSize(w, h);
        updateNodeSize();

        int offX = (int) (p.x * d) - p.x;
        int offY = (int) (p.y * d) - p.y;
        getParent().setLocation(getParent().getLocation().x - offX, getParent().getLocation().y - offY);
        //in the original code, zoomPanel is being shifted. here we are shifting containerPanel

        getParent().doLayout();             // do the layout for containerPanel
        getParent().getParent().doLayout(); // do the layout for jf (JFrame)

        prevN = n;
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    @Override
    public void paint(Graphics g) {
        
        if(!updatePath){
            super.paint(g);
            g.setColor(Color.black);

            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nColumns; j++) {
                    int y = (int) (i * nodeHeight);
                    int x = (int) (j * nodeWidth);
                    Node node = grid.get(i).get(j);
                    if (node.sides.get("Up") == null) {
                        g.drawLine(x, y, x + nodeWidth, y);
                    }
                    if (node.sides.get("Right") == null) {
                        g.drawLine(x + nodeWidth, y, x + nodeWidth, y + nodeHeight);
                    }
                    if (node.sides.get("Down") == null) {
                        g.drawLine(x, y + nodeHeight, x + nodeWidth, y + nodeHeight);
                    }
                    if (node.sides.get("Left") == null) {
                        g.drawLine(x, y, x, y + nodeWidth);
                    }
                }
            }           
        }

        
        
        if(newNodes != null){
            g.setColor(Color.blue);
            Iterator it = newNodes.iterator();
            while(it.hasNext()){
                Node node = (Node)it.next();
                int nodeX = node.y;
                int nodeY = node.x;
                g.fillRect(nodeX * nodeWidth + nodeWidth/4, nodeY * nodeHeight + nodeHeight/4, nodeWidth/2, nodeWidth/2);
            }
        }
        
        if(closedSet != null){
            g.setColor(Color.yellow);
            Iterator it = closedSet.iterator();
            while(it.hasNext()){
                Node node = (Node)it.next();
                int nodeX = node.y;
                int nodeY = node.x;
                g.fillRect(nodeX * nodeWidth + nodeWidth/4, nodeY * nodeHeight + nodeHeight/4, nodeWidth/2, nodeWidth/2);
            }
        }
        
        if(currentNode != null){
            g.setColor(Color.red);
            Node tmpNode = currentNode;
            while(tmpNode != null){
                int tmpNodeX = tmpNode.y;
                int tmpNodeY = tmpNode.x;
                g.fillRect(tmpNodeX * nodeWidth + nodeWidth/4, tmpNodeY * nodeHeight + nodeHeight/4, nodeWidth/2, nodeWidth/2);
                tmpNode = tmpNode.predecessor;
            }
        }
        
        if(!updatePath){
            if(startX != -1){
                g.setColor(Color.red);
                g.fillOval(startX * nodeWidth + nodeWidth/4, startY * nodeHeight + nodeHeight/4, nodeWidth/2, nodeWidth/2);
            }
            if(endX != -1){
                g.setColor(Color.green);
                g.fillOval(endX * nodeWidth + nodeWidth/4, endY * nodeHeight + nodeHeight/4, nodeWidth/2, nodeWidth/2);            
            }
        }
        
        updatePath = false;
    }

    private void updateNodeSize(){
        this.nodeHeight = preferredSize.height / nRows;
        this.nodeWidth = preferredSize.width / nColumns;        
    }
    
    public void updateSets(PriorityQueue<Node> openSet, HashSet<Node> closedSet){
        
        updatePath = true;
        
        if(newNodes == null){
            this.openSet = openSet;
            newNodes = new HashSet<>(this.openSet);
        }else{
            HashSet<Node> tmpNewNodes = new HashSet<>(openSet);
            tmpNewNodes.removeAll(newNodes); 
            newNodes = tmpNewNodes;
            this.openSet = openSet;
        }
        
        
        this.closedSet = closedSet;
        
        currentNode = openSet.peek();
    }
    
    private void addStartEndPoints(java.awt.event.MouseEvent evt){
        
        if(!selectionEnabled){return;}
        
        int x = evt.getX();
        int y = evt.getY();

        x = x / nodeWidth;
        y = y / nodeHeight;

        //Punkt początkowe i końcowe są dodawane w kolejności 
        // 1. początek 
        // 2. koniec
        if((startX == -1) && ((endX != x) || ( endY != y))){
            startX = x;
            startY = y;
        }else if((endX == -1) && ((startX != x) || ( startY != y))){
            endX = x;
            endY = y;
        }else{
            if((startX == x) && (startY == y)){
                startX = -1;
                startY = -1;
            }else if((endX == x) && (endY == y)){
                endX = -1;
                endY = -1;
            }
        }        
       
        update(getGraphics());
    }
    
    public boolean hasSelectedStartEnd(){
        return (startX != -1) &&
                (startY != -1) &&
                (endX != -1) &&
                (endY != -1);
    }
    
    public void blockSelectionOfStartEnd(){
        selectionEnabled = false;
    }
    
    public int[] getStartEnd(){
        int[] res = new int[4];
        res[0] = startX;
        res[1] = startY;
        res[2] = endX;
        res[3] = endY;
        
        // zamiana z systemu koordynatów graficznych na system koordynatów macierzy
        int tmp = res[0];
        res[0] = res[1];
        res[1] = tmp;
        
        tmp = res[2];
        res[2] = res[3];
        res[3] = tmp;
        
        return res;
    }
    
}
