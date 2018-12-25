/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maszyna;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WnioskowanieXML {
        DefaultTableModel regulyModel;
    
        public WnioskowanieXML(DefaultTableModel regulyModel){
            this.regulyModel = regulyModel;
        }

	public void generateXML() {

	  try {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// graphml elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("graphml");
		doc.appendChild(rootElement);

		// graph elements
		Element graph = doc.createElement("graph");
                rootElement.appendChild(graph);
                
                // edgedefault="undirected"
                Attr attr = doc.createAttribute("edgedefault");
                attr.setValue("undirected");
		graph.setAttributeNode(attr);
                
                // data schema
                Element key = doc.createElement("key");
                graph.appendChild(key);
                attr = doc.createAttribute("id");
                attr.setValue("name");
                key.setAttributeNode(attr);
                
                attr = doc.createAttribute("for");
                attr.setValue("node");
                key.setAttributeNode(attr);
                
                attr = doc.createAttribute("attr.name");
                attr.setValue("name");
                key.setAttributeNode(attr);
                
                attr = doc.createAttribute("attr.type");
                attr.setValue("string");
                key.setAttributeNode(attr);
                
                // create nodes from reguly
                HashMap<Integer, Integer> indeksLiczb = new HashMap<>();
                int j = 0;
                for(int i = 0; i < regulyModel.getRowCount(); i++){
                    String value = (String)regulyModel.getValueAt(i, 2);
                    if(!value.equals("?")){
                        String lewa = (String)regulyModel.getValueAt(i, 0) + " => " +
                                (String)regulyModel.getValueAt(i,1);
                        Element node = doc.createElement("node");
                        attr = doc.createAttribute("id");
                        attr.setValue(Integer.toString(j));
                        node.setAttributeNode(attr);
                        graph.appendChild(node);
                        
                        Element lewaNode = doc.createElement("data");
                        attr = doc.createAttribute("key");
                        attr.setValue("name");
                        lewaNode.setAttributeNode(attr);
                        lewaNode.appendChild(doc.createTextNode(lewa));
                        node.appendChild(lewaNode);
                        
                        indeksLiczb.put(j++, i);
                    }
                }

                for(int i = 0; i < indeksLiczb.size(); i++){
                    String prawa = (String)regulyModel.getValueAt(indeksLiczb.get(i), 1);
                    for(j = 0; j < indeksLiczb.size(); j++){
                        String lewa = (String)regulyModel.getValueAt(indeksLiczb.get(j), 0);
                        if(lewa.contains(prawa)){
                            System.out.println(lewa);
                            Element edge = doc.createElement("edge");
                            attr = doc.createAttribute("source");
                            attr.setValue(Integer.toString(i));
                            edge.setAttributeNode(attr);
                            
                            attr = doc.createAttribute("target");
                            attr.setValue(Integer.toString(j));
                            edge.setAttributeNode(attr);
                            
                            graph.appendChild(edge);
                        }
                    }
                }
                
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("/home/adam/NetBeansProjects/Maszyna/reguly.xml"));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		System.out.println("File saved!");

	  } catch (ParserConfigurationException | TransformerException pce) {
	  }
	}
}

