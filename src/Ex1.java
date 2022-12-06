import org.w3c.dom.Document;

import javax.xml.parsers.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class Ex1 {
    public static void main(String[] args) {
        try {
            File myObj = new File("input.txt"); //Find the file
            Scanner myReader = new Scanner(myObj);
            int count = 0;
            BayesianNetwork graph = new BayesianNetwork();
            while (myReader.hasNextLine()) { //Read every line
                String data = myReader.nextLine();
                // If first line
                if(++count == 1){
                    File xmlFile = new File(data);
                    // Reading the xml file
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(xmlFile);

                    System.out.println(doc.getDocumentElement()); // root (should be NETWORK)
                    //We will start adding vars according to the 'VARIABLE' tag in the file
                    NodeList varNodes = doc.getElementsByTagName("VARIABLE");
                    for(int i=0; i<varNodes.getLength(); i++)
                    {
                        Node varNode = varNodes.item(i);
                        if(varNode.getNodeType() == Node.ELEMENT_NODE)
                        {
                            // Getting var name
                            Element varElement = (Element) varNode;
                            // Get content under the tag 'NAME'
                            String name = varElement.getElementsByTagName("NAME").item(0).getTextContent();
                            // Creating the variable and adding it to the netwrok
                            Variable var = new Variable(name);
                            graph.addVar(var);
                            // Print check
                            System.out.println("name = " + var.getName());

                            // Getting the values of the var into an arraylist under tag 'OUTCOME'
                            int lengthOfValues = varElement.getElementsByTagName("OUTCOME").getLength();
                            ArrayList<String> valuesList = new ArrayList<String>();
                            for(int j=0; j<lengthOfValues; j++) {
                                String value = varElement.getElementsByTagName("OUTCOME").item(j).getTextContent();
                                valuesList.add(value);
                            }
                            var.setValues(valuesList);
                            // Print check
                            System.out.println("values = " + var.getValues());
                        }
                    }
                    // Getting the var's parents under tag 'DEFINITION' where we also create the cpt from
                    NodeList CPTVarNodes = doc.getElementsByTagName("DEFINITION");
                    for(int i=0; i<CPTVarNodes.getLength(); i++) {
                        Node varNode = CPTVarNodes.item(i);
                        if(varNode.getNodeType() == Node.ELEMENT_NODE) {
                            // Getting var name
                            Element varElement = (Element) varNode;
                            String name = varElement.getElementsByTagName("FOR").item(0).getTextContent();
                            Variable var = new Variable();
                            // If not in network
                            if (!graph.checkIfVarExist(name)) {
                                System.out.println("Variable " + name + "is not exist");
                            }
                            // Get var by name
                            else {
                                var = graph.getVarByName(name);
                            }
                            // Here we are searching for its parent under tag 'GIVEN'
                            int lengthOfParents = varElement.getElementsByTagName("GIVEN").getLength();
                            ArrayList<Variable> parents = new ArrayList<Variable>();
                            for(int j=0; j<lengthOfParents; j++) {
                                String parent = varElement.getElementsByTagName("GIVEN").item(j).getTextContent();
                                System.out.println("parent: "+ parent);

                                // If parent not in network
                                if(!graph.checkIfVarExist(parent)) {
                                    System.out.println("Parent " + name + "is not exist");
                                }
                                // Add the parent to our list
                                else {
                                    Variable parentVar = graph.getVarByName(parent);
                                    parents.add(parentVar);
                                }

                            }
                            var.setParents(parents);

                            // Now we are going to create the CPT of each var
                            String cptValues = varElement.getElementsByTagName("TABLE").item(0).getTextContent();
                            String[] cptValuesSplit = cptValues.split(" ");
                            var.setCpt(cptValuesSplit);
                            var.printCpt();
                        }
                    }
                    // Printing all our vars' data
                    graph.printNetwork();

                }
                // Be aware that first line isn't a query
                Character algo = data.charAt(data.length()-1); // Algo's number
                if (data.indexOf('|')!=-1){ // If its equals to -1 so the string doesn't contains this char
                    // Fitting the string for our comfort (it still the same one, of course)
                    data = data.replace('|', ',');
                    data = data.substring(2, data.length()-3);
                }
                if (algo == '1'){
                    Algo1 algo1 = new Algo1(graph);
                    Double existAns = algo1.checkExist(data);
                    if (existAns!=-1.0){
                        System.out.println(existAns);
                    }
                    else{
                        ArrayList<String> answer = algo1.algo1(data);
                        DecimalFormat df = new DecimalFormat("#.#####");
                        String ans = df.format(Double.parseDouble(answer.get(0)));
                        System.out.println(ans + ", " + answer.get(1) + ", " + answer.get(2));
                    }
                }
                else if (algo == '2'){
                    Algo2 algo2 = new Algo2(graph);
                    algo2.algo2(data);
                }


            }
        } catch (FileNotFoundException | ParserConfigurationException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}







