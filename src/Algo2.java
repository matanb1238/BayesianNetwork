import java.nio.file.FileAlreadyExistsException;
import java.util.*;

public class Algo2 {
    private BayesianNetwork network;
    private Set<HashMap<ArrayList<String>,Double>> factors;
    private String query;

    public Algo2(BayesianNetwork network, String query){
        this.network = network;
        this.factors = new HashSet<>();
        this.query = query;
    }
    public Set<HashMap<ArrayList<String>,Double>> getFactors(){return factors;}

    public ArrayList<String> getQueryVars(){
        ArrayList<String> queryVars = new ArrayList<>();
        String[] q = this.query.split(",");
        for (String str : q){
            str = str.substring(0, str.indexOf('='));
            queryVars.add(str);
        }
        System.out.println("dsfdsfdsfdsfs" + queryVars.toString());
        return queryVars;
    }

    public String getVarValue(String var){
        String[] q = this.query.split(",");
        for (String str : q) {
            String currVar = str.substring(0, str.indexOf('='));
            if (currVar.equals(var)){
                System.out.println("Query: " + q + "\n Var: " + var + " Value: " + str.substring(str.indexOf('=')));
                return str.substring(str.indexOf('=') + 1);
            }
        }
        return "";
    }

    public HashMap<ArrayList<String>,Double> fitEvidenceCPT(HashMap<ArrayList<String>,Double> cpt, String var, String value){
        HashMap<ArrayList<String>,Double> factor = new HashMap<>();
        System.out.println("Factor start: " + cpt);
        String varValue = var + "=" + value;
        for (ArrayList<String> cptLine : cpt.keySet()){
            if (cptLine.contains(varValue)){
                factor.put(cptLine, cpt.get(cptLine));
            }
        }
        System.out.println("Factor middle: " + factor);
        for (ArrayList<String> factorLine : factor.keySet()) {
            factorLine.remove(varValue);
        }
        System.out.println("Factor end: " + factor);
        return factor;
    }
    public ArrayList<String> algo2(String q){
        System.out.println("Query: " + q);
        ArrayList<String> finalAnswer = new ArrayList<>();
        ArrayList<String> queryVars = getQueryVars();
        List<String> evidenceVars = queryVars.subList(1, queryVars.size());
        // First - let's initialize the factors to the CPTs
        // If CPT contains evidence variable,
        // We will take only the lines that correspond to the evidence values in the query
        for (Variable var: network.getVars()){
            HashMap<ArrayList<String>,Double> currCpt = new HashMap<>();
            if (evidenceVars.contains(var.getName())){
                currCpt = fitEvidenceCPT(var.getCpt(), var.getName(), getVarValue(var.getName()));
            }
            else{
                currCpt = var.getCpt();
            }
            factors.add(currCpt);
        }
        ArrayList<String> varsNames = network.getVarsNames();
        // Now, we want to eliminate hidden vars ordered by ABC
        Collections.sort(varsNames);
        // Let's check which vars are hidden
        for (String varName : varsNames){
            System.out.println("Query: " + q);
            System.out.println("Var Name: " + varName);
            System.out.println("Factors Size: " + factors.size());
            for (HashMap<ArrayList<String>,Double> factor : factors){
                System.out.println("Factor: " + factor);
            }
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
                if (!checkIfFatherOfQueryVar(this.network.getVarByName(varName), getQueryVars())){
                    System.out.println(varName + " is not a parent of query var !" + q);
                    Set<HashMap<ArrayList<String>, Double>> relevantFactors = getVarFactors(varName);
                    for (HashMap<ArrayList<String>, Double> factor : relevantFactors){
                        factors.remove(factor);
                    }
                    break;
                }
                Set<HashMap<ArrayList<String>, Double>> relevantFactors = getVarFactors(varName);
                while (relevantFactors.size() > 1) {
                    System.out.println("Current Size: " + relevantFactors.size());
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
                        joinFactors(network.getVarByName(varName), factor1, factor2, relevantFactors);
                        for (HashMap<ArrayList<String>,Double> factor : factors){
                            System.out.println("Factor After Join: " + factor);
                        }
                    }
                }
                if (relevantFactors.size() == 1) {
                    for (HashMap<ArrayList<String>, Double> factor : relevantFactors){
                        eliminateFactor(network.getVarByName(varName), factor, relevantFactors);
                    }
                }
                else {
                    System.out.println("ERROR - Factor size is probably 0 - check this");
                }
            }
        }






        //System.out.println(checkExist(q));
        String[]q1 = q.split(",");
        for (HashMap<ArrayList<String>, Double> factor : factors){
            for (ArrayList<String> cptLine : factor.keySet()){
                int count = 0;
                for (String str : q1){
                    if (cptLine.contains(str)){
                        count++;
                    }
                }
                if (count == q1.length){
                    Double sum = 0.0;
                    for (Double ans : factor.values()){
                        sum += ans;
                    }
                    String firstVar = q1[0];
                    System.out.println("First Var: " + firstVar);
                    System.out.println("sum: " + sum);
                    for (ArrayList<String> cptL : factor.keySet()){
                        System.out.println(factor.get(cptL) + "+" + factor.get(cptL)/sum);
                        factor.replace(cptL, factor.get(cptL), factor.get(cptL)/sum);
                    }
                    System.out.println("Final Factor: " + factor);
                    System.out.println("Answer = " + factor.get(cptLine));
                }
            }
        }







        for (HashMap<ArrayList<String>, Double> factor : factors){
            System.out.println(factor.toString() + "\n");

        }
        // Now - we have all the factors initialized to the CPTs
        return finalAnswer;
    }

    public Double checkExist(String query){
        String[]q = query.split(",");
        for (HashMap<ArrayList<String>, Double> factor : factors){
            for (ArrayList<String> cptLine : factor.keySet()){
                int count = 0;
                for (String str : q){
                    if (cptLine.contains(str)){
                        count++;
                    }
                }
                if (count == q.length){
                    return factor.get(cptLine);
                }
            }
        }
        return -1.0;
    }
    public boolean checkIfFatherOfQueryVar2(Variable var, ArrayList<String> queryVars){
        for (String queryVarName : queryVars){
            Variable queryVar = network.getVarByName(queryVarName);
            System.out.println(queryVar.getName() + " Parents: " + queryVar.getParentsNames(queryVar.getParents()).toString());
            if (queryVar.getParents().contains(var)){
                return true;
            }
            else {
                ArrayList<Variable> queryVarParents = queryVar.getParents();
                return checkIfFatherOfQueryVar(var, queryVar.getParentsNames(queryVarParents));
            }
        }
        return false;
    }

    public boolean checkIfFatherOfQueryVar(Variable var, ArrayList<String> queryVars){
        ArrayList<String> newQueryVars = new ArrayList<>();
        for (String queryVarName : queryVars){
            Variable queryVar = network.getVarByName(queryVarName);
            if (queryVar.getParents().contains(var)){
                return true;
            }
            for (Variable parent : queryVar.getParents()){
                newQueryVars.add(parent.getName());
            }
        }
        return checkIfFatherOfQueryVar2(var, newQueryVars);

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

    // Here we check if two factor lines should be multiplied by each other at the join part
    public boolean checkIfJoinLines(ArrayList<String> Line1, ArrayList<String> Line2, ArrayList<String> commonVars){
        int count = 0;
        for (String str1 : Line1){
            for (String str2: Line2){
                if (str1.equals(str2) && commonVars.contains(str1.substring(0, str1.indexOf("=")))){
                    count++;
                }
            }
        }
        return count == commonVars.size();
    }

    public ArrayList<String> getNewLineAfterJoin (ArrayList<String> Line1, ArrayList<String> Line2) {
        ArrayList<String> newLine = new ArrayList<>(Line1);
        for (String str2 : Line2){
            if (!newLine.contains(str2)){
                newLine.add(str2);
            }
        }
        return newLine;
    }
    public void joinFactors(Variable var, HashMap<ArrayList<String>,Double> factor1,
                                 HashMap<ArrayList<String>,Double> factor2, Set<HashMap<ArrayList<String>, Double>> relevantFactors){
        ArrayList<String> commonVars = getCommonVars(factor1, factor2);
        System.out.println(commonVars.toString());
        HashMap<ArrayList<String>,Double> newFactor = new HashMap<>();
        for (ArrayList<String> cptLine_factor1 : factor1.keySet()) {
            for (ArrayList<String> cptLine_factor2 : factor2.keySet()) {
                if (checkIfJoinLines(cptLine_factor1, cptLine_factor2, commonVars)){
                    ArrayList<String> newLine = getNewLineAfterJoin(cptLine_factor1, cptLine_factor2);
                    Double p1 = factor1.get(cptLine_factor1);
                    Double p2 = factor2.get(cptLine_factor2);
                    Double newP = p1*p2;
                    newFactor.put(newLine, newP);
                }
            }
        }
        this.factors.add(newFactor);
        this.factors.remove(factor1);
        this.factors.remove(factor2);
        relevantFactors.add(newFactor);
        relevantFactors.remove(factor1);
        relevantFactors.remove(factor2);
        //    System.out.println("Factor Var: " + var.getName());
        // first, let's get the index of the var in the factor lines, we want to delete it
//        int index1 = getIndexInFactor(var, factor1);
//        int index2 = getIndexInFactor(var, factor2);
//        System.out.println(factor1);
//        System.out.println(factor2);
//        System.out.println("Indexes: " + index1 + ", " + index2);
//        for (ArrayList<String> cptLine_factor1 : factor1.keySet()){
//            for (ArrayList<String> cptLine_factor2 : factor2.keySet()){
//                ArrayList<String> copy_cpt_factor1 = new ArrayList<>(cptLine_factor1);
//                ArrayList<String> copy_cpt_factor2 = new ArrayList<>(cptLine_factor2);
//                copy_cpt_factor1.remove(index1);
//                copy_cpt_factor2.remove(index2);
//                if (checkIfLineIsEqual(cptLine_factor1, cptLine_factor2)){
//                    System.out.println("These rows are equal: " + factor1 + "\n" + factor2);
//                }
//
//            }
//        }
    }

    public void eliminateFactor (Variable var, HashMap<ArrayList<String>,Double> factor, Set<HashMap<ArrayList<String>, Double>> relevantFactors){
        int varIndex = getIndexInFactor(var, factor);
        HashMap<ArrayList<String>,Double> newFactor = new HashMap<>();
        for (ArrayList<String> cptLine_factor1 : factor.keySet()){
            for (ArrayList<String> cptLine_factor2 : factor.keySet()) {
                if (cptLine_factor1 != cptLine_factor2){
                    ArrayList<String> copy_cpt_factor1 = new ArrayList<>(cptLine_factor1);
                    ArrayList<String> copy_cpt_factor2 = new ArrayList<>(cptLine_factor2);
                    copy_cpt_factor1.remove(varIndex);
                    copy_cpt_factor2.remove(varIndex);
//                    System.out.println(var.getName());
                    if (checkIfLineIsEqual(copy_cpt_factor1, copy_cpt_factor2)){ Double ans = factor.get(cptLine_factor1)+ factor.get(cptLine_factor2);
                        System.out.println(copy_cpt_factor1.toString() + copy_cpt_factor2.toString());
                        newFactor.put(copy_cpt_factor1, ans);
                    }
                }
            }
        }
        this.factors.remove(factor);
        this.factors.add(newFactor);
        relevantFactors.remove(factor);
        relevantFactors.add(newFactor);
    }
    public int getIndexInFactor (Variable var, HashMap<ArrayList<String>,Double> factor){
        int index=0;
        for (ArrayList<String> cptLine : factor.keySet()){
            for (String str : cptLine){
//                System.out.println(str.substring(0, str.indexOf("="))+ var.getName());
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
            return true;
        }
        return false;
    }
}
