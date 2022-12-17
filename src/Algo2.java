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
                    ArrayList<String> commonVars = getCommonVars(factor1, factor2);
                    joinFactors(network.getVarByName(varName), factor1, factor2);
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

    public ArrayList<String> getCommonVars(HashMap<ArrayList<String>,Double> factor1,
                                    HashMap<ArrayList<String>,Double> factor2){
        ArrayList<String> vars1 = new ArrayList<>();
        ArrayList<String> vars2 = new ArrayList<>();
        ArrayList<String> finalVars = new ArrayList<>();
        for (ArrayList<String> cptLine : factor1.keySet()){
            for (String str : cptLine){
                str = str.substring(0, str.indexOf("="));
                vars1.add(str);
            }
            break;
        }
        for (ArrayList<String> cptLine : factor2.keySet()){
            for (String str : cptLine){
                str = str.substring(0, str.indexOf("="));
                vars2.add(str);
            }
            break;
        }

        for (String str1 : vars1){
            for (String str2 : vars2){
                if (str1.equals(str2)){
                    finalVars.add(str1);
                }
            }
        }
        return finalVars;
    }
    public void joinFactors(Variable var, HashMap<ArrayList<String>,Double> factor1,
                                 HashMap<ArrayList<String>,Double> factor2){
        System.out.println("Factor Var: " + var.getName());
        HashMap<ArrayList<String>,Double> newFactor = new HashMap<>();
        // first, let's get the index of the var in the factor lines, we want to delete it
        int index1 = getIndexInFactor(var, factor1);
        int index2 = getIndexInFactor(var, factor2);
        System.out.println(factor1);
        System.out.println(factor2);
        System.out.println("Indexes: " + index1 + ", " + index2);
        for (ArrayList<String> cptLine_factor1 : factor1.keySet()){
            for (ArrayList<String> cptLine_factor2 : factor2.keySet()){
                ArrayList<String> copy_cpt_factor1 = new ArrayList<>(cptLine_factor1);
                ArrayList<String> copy_cpt_factor2 = new ArrayList<>(cptLine_factor2);
                copy_cpt_factor1.remove(index1);
                copy_cpt_factor2.remove(index2);
                if (checkIfLineIsEqual(cptLine_factor1, cptLine_factor2)){
                    System.out.println("These rows are equal: " + factor1 + "\n" + factor2);
                }

            }
        }
    }

    public int getIndexInFactor (Variable var, HashMap<ArrayList<String>,Double> factor){
        int index=0;
        for (ArrayList<String> cptLine : factor.keySet()){
            for (String str : cptLine){
                System.out.println(str.substring(0, str.indexOf("="))+ var.getName());
                if (str.substring(0, str.indexOf("=")).equals(var.getName())){
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    // CPT lines can be equal but in different order so we will check it here (for the elimination part)
    public boolean checkIfLineIsEqual(ArrayList<String> line1, ArrayList<String> line2){
        int count = 0;
        int size = line1.size();
        for (String str1 : line1){
            for (String str2 : line2){
                if (str1.equals(str2)){
                    count++;
                }
            }
        }
        if (count == size){
            System.out.println("True: "+ line1 + "\n" + line2);
            return true;
        }
        return false;
    }
}
