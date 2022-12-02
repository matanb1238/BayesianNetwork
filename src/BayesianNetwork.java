import java.util.ArrayList;
import java.util.HashMap;

public class BayesianNetwork {
    private ArrayList<Variable> vars;

    public BayesianNetwork(){
        vars = new ArrayList<>();
    }
//    public BayesianNetwork(Variable[] graph) {
//        this.graph = graph;
//        varNames = new ArrayList<>();
//    }
//    public Variable[] getGraph(){return graph;}

    public ArrayList<Variable> getVars(){return vars;}

    public void addVar(Variable var){vars.add(var);}

    public ArrayList<String> getVarsNames(){
        ArrayList<String> varsNames = new ArrayList<>();
        for (Variable var : vars){
            varsNames.add(var.getName());
        }
        return varsNames;
    }

    public boolean checkIfVarExist(String varName){
        boolean bool = false;
        for(int i=0; i<vars.size(); i++){
            if(vars.get(i).getName().equals(varName)){
                bool = true;
            }
        }
        return bool;
    }

    public Variable getVarByName(String name){
        for(int i=0; i<vars.size(); i++){
            if(vars.get(i).getName().equals(name)) {
                return vars.get(i);
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
