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
        System.out.println("Query: " + q);
        ArrayList<String> finalAnswer = new ArrayList<>();
        // First - let's initialize the factors to the CPTs
        for (Variable var: network.getVars()){
            HashMap<ArrayList<String>,Double> currCpt = var.getCpt();
            factors.add(currCpt);
        }
        ArrayList<String> varsNames = network.getVarsNames();
        // Now, we want to eliminate hidden vars ordered by ABC
        Collections.sort(varsNames);
        // Let's check which vars are hidden
        for (String varName : varsNames){
            //System.out.println("Varname: " + varName);
            boolean isHidden = true;
            String[] query = q.split(",");
            // for var in the query
            for (String str : query){
                str = str.substring(0, str.indexOf('='));
                //System.out.println("str: " + str);
                if (str.equals(varName)){
                    isHidden = false;
                }
            }
            if (isHidden) {
                Set<HashMap<ArrayList<String>, Double>> relevantFactors = getVarFactors(varName);
                //System.out.println(relevantFactors.size());
                for (HashMap<ArrayList<String>, Double> factor : relevantFactors){
                    //System.out.println(factor);
                }
                if (relevantFactors.size() == 1){
                    System.out.println("Only One Factor");
                }
                else{
                    Hashtable<HashMap<ArrayList<String>, Double>, Integer> factorAndSize = getAllFactorsSizes(relevantFactors);
                    Hashtable<HashMap<ArrayList<String>, Double>, Integer> twoMinFactors = getTwoMinFactors(factorAndSize);
                    HashMap<ArrayList<String>, Double> factor1 = new HashMap<>();
                    HashMap<ArrayList<String>, Double> factor2 = new HashMap<>();
                    int index = 0;
                    for (HashMap<ArrayList<String>, Double> factor : twoMinFactors.keySet()){
                        if (index == 0){
                            factor1 = factor;
                        }
                        else if (index == 1){
                            factor2 = factor;
                        }
                        index++;
                    }
                    eliminateFactors(network.getVarByName(varName), factor1, factor2);
                }
            }
            else{
            }
        }
        // Now - we have all the factors initialized to the CPTs
        return finalAnswer;
    }

    public Hashtable<HashMap<ArrayList<String>, Double>, Integer>  getAllFactorsSizes(Set<HashMap<ArrayList<String>, Double>> relevantFactors){
        int factorSize = 0;
        Hashtable<HashMap<ArrayList<String>, Double>, Integer> factorAndSize = new Hashtable<>();
        for (HashMap<ArrayList<String>, Double> factor : relevantFactors){
            for(ArrayList<String> string : factor.keySet()){
                //System.out.println("Factor: " + factor);
                //System.out.println("check " + string + " size " + string.size());
                factorSize = string.size() * factor.size();
                factorAndSize.put(factor, factorSize);
                //System.out.println("factorSize " + factorSize);
                break;
            }
        }
        return factorAndSize;
    }

    public Hashtable<HashMap<ArrayList<String>, Double>, Integer> getTwoMinFactors(Hashtable<HashMap<ArrayList<String>, Double>, Integer> factorAndSize){
        int min = 999999;
        Hashtable<HashMap<ArrayList<String>, Double>, Integer> twoMinFactors = new Hashtable<>();
        HashMap<ArrayList<String>, Double> minFactor = new HashMap<>();
        for (HashMap<ArrayList<String>, Double> factor : factorAndSize.keySet()){
            int size = factorAndSize.get(factor);
            if (size < min) {
                min = size;
                minFactor = factor;
            }
        }
        twoMinFactors.put(minFactor, min);
        int secondMin = 999999;
        HashMap<ArrayList<String>, Double> secondMinFactor = new HashMap<>();
        for (HashMap<ArrayList<String>, Double> factor : factorAndSize.keySet()){
            int size = factorAndSize.get(factor);
            if (size < secondMin && factor != minFactor){
                secondMin = size;
                secondMinFactor = factor;
                //System.out.println("First Factor: " + minFactor + "\nSecond Factor: " + secondMinFactor);
            }
        }
        twoMinFactors.put(secondMinFactor, secondMin);
        return twoMinFactors;
    }

    // Function which returns the factors that contains a specific var
    public Set<HashMap<ArrayList<String>,Double>> getVarFactors(String varName){
        System.out.println("Var factors: " + varName);
        Set<HashMap<ArrayList<String>,Double>> relevantFactors = new HashSet<>();
        for (HashMap<ArrayList<String>,Double> factor : this.factors){
            for (ArrayList<String> cptLine : factor.keySet()){
                boolean bool = false;
                for (String value : cptLine){
                    if (!bool) {
                        if (value.contains(varName)) {
                            //System.out.println("Value: " + value);
                            relevantFactors.add(factor);
                            //System.out.println("Factor: " + factor);
                            bool = true;
                        }
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
        return relevantFactors;
    }
    public void eliminateFactors(Variable var, HashMap<ArrayList<String>,Double> factor1,
                                 HashMap<ArrayList<String>,Double> factor2){
        System.out.println(var.getName());
        for (ArrayList<String> arr : factor1.keySet()){
            System.out.println(arr);
        }
        for (ArrayList<String> arr : factor2.keySet()){
            System.out.println(arr);
        }
    }
}
