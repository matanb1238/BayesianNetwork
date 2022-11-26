import java.util.ArrayList;
import java.util.HashMap;

public class BayesianNetwork {
    private ArrayList<Variable> varNames;

    public BayesianNetwork(){
        varNames = new ArrayList<>();
    }
//    public BayesianNetwork(Variable[] graph) {
//        this.graph = graph;
//        varNames = new ArrayList<>();
//    }
//    public Variable[] getGraph(){return graph;}

    public ArrayList<Variable> getVars(){return varNames;}

    public void addVar(Variable var){varNames.add(var);}

    public boolean checkIfVarExist(String varName){
        boolean bool = false;
        for(int i=0; i<varNames.size(); i++){
            if(varNames.get(i).getName().equals(varName)){
                bool = true;
            }
        }
        return bool;
    }

    public Variable getVarByName(String name){
        for(int i=0; i<varNames.size(); i++){
            if(varNames.get(i).getName().equals(name)) {
                return varNames.get(i);
            }
        }
        // It won't get here because we checked before the call if the var exist
        Variable var = new Variable();
        return var;
    }

    public void printNetwork(){
        for(int i=0; i<this.getVars().size(); i++){
            System.out.println("\n");
            this.getVars().get(i).printVar();
        }
    }
}
