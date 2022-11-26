import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class Variable {
    private String name;
    private ArrayList<Variable> parents;
    private ArrayList<String> values;
    private HashMap<ArrayList<String>,Double> cpt;

    public Variable(){
        name = "";
        parents = new ArrayList<>();
        values = new ArrayList<>();
        cpt = new HashMap<>();
    }

    public Variable(String name) {
        this.name = name;
        parents = new ArrayList<>();
        values = new ArrayList<>();
        cpt = new HashMap<>();
    }

    public Variable(Variable other){
        this.name = other.name;
        this.parents = other.parents;
        this.values = other.values;
        this.cpt = other.cpt;
    }
    public String getName(){
        return name;
    }
    public ArrayList<String> getValues(){
        return values;
    }
    public void setValues(ArrayList<String> values) {
        this.values = values;
    }
    public ArrayList<Variable> getParents(){
        return parents;
    }
    public void setParents(ArrayList<Variable> parents) {
        this.parents = parents;
    }
    public HashMap<ArrayList<String>,Double> getCpt(){return cpt;}
    public void setCpt(String[] values){
        int parentsListSize = this.parents.size();
        ArrayList<Variable> reversedParents = new ArrayList<>();
        // Reversed loop on the parents
        for(int index=parentsListSize-1; index>=0; index--){
            reversedParents.add(this.getParents().get(index));
            System.out.println(this.getParents().get(index).getName());
        }
        Hashtable<Variable, String> tableOfLastValues = new Hashtable<>();
        // Set firstly the table with every var and its first value
        tableOfLastValues.put(this, this.getValues().get(0));
        for (int j=0; j<reversedParents.size(); j++){
            Variable parent = reversedParents.get(j);
            tableOfLastValues.put(parent, parent.getValues().get(0));
        }
        int count = 0;
        for (int i=0; i<values.length; i++){
            String curr_value = values[i];
            System.out.println(curr_value);
            ArrayList<String> currentCPTLine = new ArrayList<>();
            currentCPTLine.add(this.getName() + "=" + tableOfLastValues.get(this));
            if (curr_value.equals("0.5")){
                System.out.println(currentCPTLine);
            }
            count++;
            int indexOfLastNodeValue = this.getValues().indexOf(tableOfLastValues.get(this));
            tableOfLastValues.remove(this);
            if (indexOfLastNodeValue == this.getValues().size()-1){
                tableOfLastValues.put(this, this.getValues().get(0));
            }
            else{
                tableOfLastValues.put(this, this.getValues().get(indexOfLastNodeValue+1));
            }
            for (int j=0; j<reversedParents.size(); j++){
                Variable parent = reversedParents.get(j);
                currentCPTLine.add(parent.getName() + "=" + tableOfLastValues.get(parent));
                int valuesCount = this.getValues().size();
                // Every parent switcing value every multiplying of values count of all the previous
                // if it's the first parent in the reversed list so switching is equal to the node number of values
                if (j!=0){
                    for (int x=0; x<j; x++) {
                        valuesCount *= reversedParents.get(x).getValues().size();
                    }
                }

                if (count % valuesCount == 0){
                    // Get the index of the last value
                    int indexOfLastValue = parent.getValues().indexOf(tableOfLastValues.get(parent));
                    // Check if it's the last value
                    if (indexOfLastValue==parent.getValues().size()-1){
                        // removing and putting the first value
                        tableOfLastValues.remove(parent);
                        tableOfLastValues.put(parent, parent.getValues().get(0));
                    }
                    else{
                        // removing and putting next value
                        tableOfLastValues.remove(parent);
                        tableOfLastValues.put(parent, parent.getValues().get(indexOfLastValue+1));
                    }
                }
            }
            this.cpt.put(currentCPTLine, Double.parseDouble(values[i]));
        }
    }

    public void printCpt(){
        for (ArrayList<String> line : cpt.keySet()){
            String key = line.toString();
            String value = cpt.get(line).toString();
            System.out.println(key + "" + value);
        }
    }
    public ArrayList<String> getParentsNames(ArrayList<Variable> parents){
        ArrayList<String> parentsNames = new ArrayList<>();
        for(int i=0; i<parents.size(); i++){
            parentsNames.add(parents.get(i).getName());
        }
        return parentsNames;
    }
    public void printVar(){
        System.out.println("Name: " + this.getName() + '\n' + "Values: " + this.getValues()
                            + '\n' + "Parents: " + this.getParentsNames(this.getParents()));
    }
}
