import java.nio.file.FileAlreadyExistsException;
import java.util.*;

public class Algo2 {
    private BayesianNetwork network;
    private Set<HashMap<ArrayList<String>,Double>> factors;

    public Algo2(BayesianNetwork network){
        this.network = network;
        this.factors = new HashSet<>();
    }
    public Set<HashMap<ArrayList<String>,Double>> getFactors(){return factors;}

    public ArrayList<String> algo2(String q){
        ArrayList<String> finalAnswer = new ArrayList<>();
        // First - let's initialize the factors to the CPTs
        for (Variable var: network.getVars()){
            HashMap<ArrayList<String>,Double> currCpt = var.getCpt();
            factors.add(currCpt);
        }
        ArrayList<String> varsNames = network.getVarsNames();
        Collections.sort(varsNames);
        for (String varName : varsNames){
            System.out.println(varName);
            Set<HashMap<ArrayList<String>,Double>> relevantFactors = new HashSet<>();
            for (HashMap<ArrayList<String>,Double> factor : this.factors){
                //System.out.println(factor.keySet());
                //List<String> cptLines = new ArrayList<>(factor.keySet());
                for (ArrayList<String> cptLine : factor.keySet()){
                    //System.out.println(cptLine);
                    boolean bool = false;
                    for (String value : cptLine){
                        if (!bool) {
                            if (value.contains(varName))
                                System.out.println("Value: " + value);
                                relevantFactors.add(factor);
                                System.out.println("Factor: " + factor);
                                bool = true;
                        }
                        else {
                            break;
                        }
                    }
                    if (bool){
                        break;
                    }
                }
            }
        }
        // Now - we have all the factors initialized to the CPTs
        return finalAnswer;
    }

    public void eliminateFactors(Variable var){

    }
}
