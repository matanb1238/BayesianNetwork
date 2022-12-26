import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.text.DecimalFormat;
import java.util.*;

public class Algo3 {
    private BayesianNetwork network;
    private Set<HashMap<ArrayList<String>,Double>> factors;
    private String query;
    private int plusCount;
    private  int multCount;

    public Algo3(BayesianNetwork network, String query){
        this.network = network;
        this.factors = new HashSet<>();
        this.query = query;
        this.plusCount = 0;
        this.multCount = 0;
    }
    public Set<HashMap<ArrayList<String>,Double>> getFactors(){return factors;}

    public ArrayList<String> algo3(String q){
        System.out.println("\n------Starting ALGO 3--------");
        System.out.println("Query is: " + q);
        ArrayList<String> finalAnswer = new ArrayList<>();
        ArrayList<String> queryVars = getQueryVars();
        List<String> evidenceVars = queryVars.subList(1, queryVars.size());
        // First - let's initialize the factors to the CPTs
        // If CPT contains evidence variable,
        // We will take only the lines that correspond to the evidence values in the query
        System.out.println("\n------First Step - Initializing the factors------");
        for (Variable var: network.getVars()){
            System.out.println("Var is " + var.getName() + ", it's CPT is: " + var.getCpt());
        }
        // For every var we will check if we need to take it's CPT and fit it if we need
        // (for example if it contains evidence variables we will take only the lines which correspond to their query values)
        System.out.println("\n");
        System.out.println("Evidence vars: " + evidenceVars.toString());
        for (Variable var: network.getVars()){
            System.out.println("Current Var: " + var.getName() + ", current CPT: " + var.getCpt());
            HashMap<ArrayList<String>,Double> varCpt = var.getCpt();
            HashMap<ArrayList<String>,Double> currCpt = new HashMap<>();
            // If it's an evidence var and it's CPT contains only him we can ignore it
            if (evidenceVars.contains(var.getName()) && var.getCpt().size()==var.getValues().size()){
                System.out.println("This CPT has only one variable and it's an evidence so ignore");
                continue;
            }
            // If cpt contains evidence variables
            if (whichEvidenceVarsContains(varCpt, evidenceVars).size()>0){
                System.out.println("This CPT contains evidence variables");
                currCpt = fitEvidenceCPT(varCpt, whichEvidenceVarsContains(varCpt, evidenceVars));
            }

            // If is not and ancestor of query/evidence var so we don't take it
            if (!getQueryVars().contains(var.getName()) && !checkIfFatherOfQueryVar(var, getQueryVars())){
                System.out.println("The current variable " + var.getName() + " is not an ancestor of query/evidence var so ignore");
                continue;
            }
            else if (whichEvidenceVarsContains(varCpt, evidenceVars).size()==0){
                System.out.println("This CPT has no evidence variables");
                currCpt = var.getCpt();
            }
            factors.add(currCpt);
        }

        // Now we are done with step 1 - initializing the factors
        System.out.println("\n------Checking factors after initialize------");
        for (HashMap<ArrayList<String>,Double> factor : factors) {
            System.out.println("Factor: " + factor);
        }
        // In Algo3 we will use a heuristic order - by the smallest CPT
        ArrayList<String> varsNames = network.getVarsNames();
        System.out.println("Vars order before sort: " + varsNames.toString());

        Hashtable<String, Integer> varAndCPTSSize = new Hashtable<>(); // each var and the size of all the CPT's that contains it
        for (String varName : varsNames){
            Set<HashMap<ArrayList<String>, Double>> relevantCPTs = getVarFactors(varName);
            int relevantCPTsSize = 0;
            for (HashMap<ArrayList<String>, Double> cpt : relevantCPTs){
                int CPTSize;
                for (ArrayList<String> cptLine : cpt.keySet()){
                    CPTSize = cpt.size() * cptLine.size();
                    relevantCPTsSize += CPTSize;
                }
            }
            varAndCPTSSize.put(varName, relevantCPTsSize);
        }

        ArrayList<String> visited = new ArrayList<>();
        for (int i=0; i < varAndCPTSSize.size(); i++) {
            int min = 999999999;
            String minVar = "";
            for (String varName : varAndCPTSSize.keySet()) {
                if (!visited.contains(varName)) {
                    int size = varAndCPTSSize.get(varName);
                    if (size < min) {
                        min = size;
                        minVar = varName;
                    }
                }
            }
            System.out.println(minVar);
            visited.add(minVar);

        }
        varsNames = visited;
        System.out.println("Vars order after sort: " + varsNames.toString());
        // Let's check which vars are hidden
        factors.removeIf(entries->entries.size()==1);
        System.out.println("\n------STEP 2 - for every hidden var we will join its factors and then eliminate it------");
        for (String varName : varsNames){
            boolean isHidden = true; //bool that indicates if hidden
            String[] query = q.split(",");
            // for var in the query check if query contains it
            for (String str : query){
                str = str.substring(0, str.indexOf('=')); // for case that var is not only one character
                if (str.equals(varName)){
                    isHidden = false;
                }
            }
            // If var is hidden
            if (isHidden) {
                System.out.println("Hidden Var: "+ varName);
                // We want only the factors that contains the current hidden variable
                Set<HashMap<ArrayList<String>, Double>> relevantFactors = getVarFactors(varName);
                while (relevantFactors.size() > 1) { // we are joining until we have only one factor which contains it
                    System.out.println("Current Relevant Factors Size: " + relevantFactors.size());
                    // We want to join the factors by order from the smallest to the biggest
                    // So firstly we will get all the factors sizes and we will take the two smallest ones and join them
                    Hashtable<HashMap<ArrayList<String>, Double>, Integer> factorAndSize = getAllFactorsSizes(relevantFactors);
                    Hashtable<HashMap<ArrayList<String>, Double>, Integer> twoMinFactors = getTwoMinFactors(factorAndSize);
                    // Now we will get each smallest factor separated
                    HashMap<ArrayList<String>, Double> factor1 = new HashMap<>();
                    HashMap<ArrayList<String>, Double> factor2 = new HashMap<>();
                    int index = 0;
                    for (HashMap<ArrayList<String>, Double> factor : twoMinFactors.keySet()){
                        if (index == 0){ // if the second smallest (last in - first out -> second smallest joined last)
                            factor2 = factor;
                        }
                        else if (index == 1){ // if the smallest (joined first -> first in last out)
                            factor1 = factor;
                        }
                        index++;
                    }
                    System.out.println("The two smallest factors: " + factor1 + "\n" + factor2);
                    // Join them
                    joinFactors(factor1, factor2, relevantFactors);
                    System.out.println("PLUS COUNT: " + this.plusCount + ", " + "MULT COUNT: " + this.multCount);
                }
                if (relevantFactors.size() == 1) { // if size is 1 we want to eliminate the hidden variable
                    for (HashMap<ArrayList<String>, Double> factor : relevantFactors){
                        System.out.println("Factor before eliminate: " + factor);
                        System.out.println(varName);
                        eliminateFactor(network.getVarByName(varName), factor, relevantFactors);
                        System.out.println("PLUS COUNT: " + this.plusCount + ", " + "MULT COUNT: " + this.multCount);
                        System.out.println("Factor after eliminate: " + relevantFactors);
                    }
                }
                else {
                    System.out.println("ERROR - Factor size is probably 0 - check this");
                }
            }
        }

        System.out.println("------STEP 3 - join the query variable's factors left if there are more than 1 left------");
        for (HashMap<ArrayList<String>, Double> factor : factors) {
            System.out.println("Query var factor: " + factor);
        }
        // Now only query var factors are left, let's join them all
        while (factors.size() > 1) {
            String queryVar = queryVars.get(0);
            System.out.println("Query var: " + queryVar);
            Hashtable<HashMap<ArrayList<String>, Double>, Integer> factorAndSize = getAllFactorsSizes(factors);
            Hashtable<HashMap<ArrayList<String>, Double>, Integer> twoMinFactors = getTwoMinFactors(factorAndSize);
            HashMap<ArrayList<String>, Double> factor1 = new HashMap<>();
            HashMap<ArrayList<String>, Double> factor2 = new HashMap<>();
            int index = 0;
            for (HashMap<ArrayList<String>, Double> factor : twoMinFactors.keySet()) {
                if (index == 0) {
                    factor1 = factor;
                } else if (index == 1) {
                    factor2 = factor;
                }
                index++;
            }
            joinFactors(factor1, factor2, factors);
            System.out.println("PLUS COUNT: " + this.plusCount + ", " + "MULT COUNT: " + this.multCount);
        }
        Double sum = 0.0;
        for (HashMap<ArrayList<String>, Double> factor : factors){
            for(ArrayList<String> line : factor.keySet()){
                sum+=factor.get(line);
                this.plusCount++;
            }
        }
        // taking one off because we have a uneccassary one (for example: in 1+2+3 there are 2 plusCount and not 3 as we calculated)
        this.plusCount--;
        for (HashMap<ArrayList<String>, Double> factor : factors) {
            for (ArrayList<String> line : factor.keySet()) {
                factor.replace(line, factor.get(line), factor.get(line)/sum);
            }
        }
        String[]q1 = q.split(",");
        String queryVarValue = q1[0];
        System.out.println("queryVarValue: " + queryVarValue);
        for (HashMap<ArrayList<String>, Double> factor : factors) {
            for (ArrayList<String> line : factor.keySet()) {
                if (line.contains(queryVarValue)){
                    finalAnswer.add(Double.toString(factor.get(line)));
                    finalAnswer.add(String.valueOf(this.plusCount));
                    finalAnswer.add(String.valueOf(this.multCount));
                    System.out.println("Answer= " + factor.get(line) + ", " + this.plusCount + ", " + this.multCount);
                }
            }
        }
        // return the answer
        return finalAnswer;
    }

    public ArrayList<String> getQueryVars(){
        ArrayList<String> queryVars = new ArrayList<>();
        String[] q = this.query.split(",");
        for (String str : q){
            str = str.substring(0, str.indexOf('='));
            queryVars.add(str);
        }
        return queryVars;
    }

    public String getVarValue(String var){
        String[] q = this.query.split(",");
        for (String str : q) {
            String currVar = str.substring(0, str.indexOf('='));
            if (currVar.equals(var)){
                // System.out.println("Query: " + q + "\n Var: " + var + " Value: " + str.substring(str.indexOf('=')));
                return str.substring(str.indexOf('=') + 1);
            }
        }
        return "";
    }

    public ArrayList<String> whichEvidenceVarsContains(HashMap<ArrayList<String>,Double> cpt, List<String> evidenceVars){
        ArrayList<String> evidenceContains = new ArrayList<>();
        for (ArrayList<String> cptLine : cpt.keySet()){
            for (String lineValue : cptLine){
                for (String evidenceVar : evidenceVars){
                    if(lineValue.substring(0, lineValue.indexOf("=")).equals(evidenceVar)){
                        evidenceContains.add(evidenceVar);
                    }
                }
            }
            break;
        }
        return evidenceContains;
    }

    // Function which fitting factor with only the lines that correspond to the evidence variables' query values
    public HashMap<ArrayList<String>,Double> fitEvidenceCPT(HashMap<ArrayList<String>,Double> cpt, ArrayList<String> evidenceContains) {
        HashMap<ArrayList<String>,Double> factor = new HashMap<>();
        for (ArrayList<String> cptLine : cpt.keySet()){
            int count = 0;
            for (String evidenceVar : evidenceContains){
                String evidenceVarValue = evidenceVar + "=" + getVarValue(evidenceVar);
                if (cptLine.contains(evidenceVarValue)){ //
                    count+=1;
                }
            }
            if (count==evidenceContains.size()){ // indicates that line is good
                ArrayList<String> newLine = new ArrayList<>();
                for (String lineValue : cptLine){
                    boolean isEvidence = false;
                    for (String evidenceVar : evidenceContains){
                        String evidenceVarValue = evidenceVar + "=" + getVarValue(evidenceVar);
                        if (lineValue.equals(evidenceVarValue)){
                            isEvidence = true;
                        }
                    }
                    if (!isEvidence){
                        newLine.add(lineValue);
                    }
                }
                factor.put(newLine, cpt.get(cptLine));
            }
        }
        return factor;
    }

    public Double checkExist(String q){
        System.out.println("Q: " + q);
        boolean bool = true;
        String[] query = q.split(","); // Splitting the query (we want to get into each one of the vars easily)
        String nodeName = Character.toString(query[0].charAt(0)); // The node is the first one
        Variable node = network.getVarByName(nodeName);
        for (ArrayList<String> cptLine : node.getCpt().keySet()){
            bool = true;
            if (cptLine.get(0).equals(query[0])){
                String[] newQuery = Arrays.copyOfRange(query, 1, query.length); // Taking off the node - we want to check the others
                List<String> newCptLine = cptLine.subList(1, cptLine.size()); // Taking off the node - we want to check the others
                for (String queryValue : newQuery) {
                    if (!newCptLine.contains(queryValue)){
                        bool = false;
                    }
                }
                if (bool) { // If the current cpt 'line' contains the query return true
                    System.out.println("bool: " + bool);
                    return node.getCpt().get(cptLine);
                }
            }
            else{ // Cpt line doesn't contains the node value
                bool = false;
            }
        }
        return -1.0;
    }

    public boolean checkIfFatherOfQueryVar(Variable var, ArrayList<String> queryVars){
        if (var.getName().equals("A3") && queryVars.contains("A2")){
            int a = 1;
        }
        //System.out.println("VAR: " + var.getName());
        //System.out.println("QUERY VARS: " + queryVars.toString());
        ArrayList<String> newQueryVars = new ArrayList<>();
        for (String queryVarName : queryVars){
            Variable queryVar = network.getVarByName(queryVarName);
            //System.out.println("CURR QUERY VAR: " + queryVarName);
            //System.out.println("CURR QUERY VAR Parent: " + queryVar.getParentsNames(queryVar.getParents()));
            if (queryVar.getParents().contains(var)){
                return true;
            }
            for (Variable parent : queryVar.getParents()){
                newQueryVars.add(parent.getName());
            }
        }
        //System.out.println("newQueryVars: " + newQueryVars);
        if (newQueryVars.size()==0){
            return false;
        }
        return checkIfFatherOfQueryVar(var, newQueryVars);
    }

    // function which returns hashtable of every factor and its size
    public Hashtable<HashMap<ArrayList<String>, Double>, Integer>  getAllFactorsSizes(Set<HashMap<ArrayList<String>, Double>> relevantFactors){
        int factorSize = 0;
        Hashtable<HashMap<ArrayList<String>, Double>, Integer> factorAndSize = new Hashtable<>();
        // for every factors in our relevant factors set
        for (HashMap<ArrayList<String>, Double> factor : relevantFactors){
            for(ArrayList<String> string : factor.keySet()){
                factorSize = string.size() * factor.size(); // the size is decided by rows * columns
                factorAndSize.put(factor, factorSize); // adding the factor and it's size to a new hashtable
                break; // it's like a table so all the rows have the same size so we are breaking after the first iteration
            }
        }
        return factorAndSize;
    }

    // function which return hashtable of the two smallest factors from all the factors given (each factor given with its size)
    public Hashtable<HashMap<ArrayList<String>, Double>, Integer> getTwoMinFactors(Hashtable<HashMap<ArrayList<String>, Double>, Integer> factorAndSize){
        int min = 999999;
        Hashtable<HashMap<ArrayList<String>, Double>, Integer> twoMinFactors = new Hashtable<>();
        HashMap<ArrayList<String>, Double> minFactor = new HashMap<>();
        // finding the smallest one
        for (HashMap<ArrayList<String>, Double> factor : factorAndSize.keySet()){
            int size = factorAndSize.get(factor);
            if (size < min) {
                min = size;
                minFactor = factor;
            }
        }
        twoMinFactors.put(minFactor, min); // adding the smallest one
        int secondMin = 999999; // for the second smallest
        HashMap<ArrayList<String>, Double> secondMinFactor = new HashMap<>();
        // finding the second smallest one
        for (HashMap<ArrayList<String>, Double> factor : factorAndSize.keySet()){
            int size = factorAndSize.get(factor);
            if (size < secondMin && factor != minFactor){ // checking also it's now the smallest one
                secondMin = size;
                secondMinFactor = factor;
            }
        }
        twoMinFactors.put(secondMinFactor, secondMin); // adding the second smallest one
        return twoMinFactors;
    }

    // Function which returns the factors that contains a specific var
    public Set<HashMap<ArrayList<String>,Double>> getVarFactors(String varName){
        Set<HashMap<ArrayList<String>,Double>> relevantFactors = new HashSet<>();
        // for every factor in all the factors set
        for (HashMap<ArrayList<String>,Double> factor : this.factors){
            for (ArrayList<String> cptLine : factor.keySet()){
                boolean bool = false;
                // check if factor contains the variable
                for (String value : cptLine){
                    if (!bool) { // if already found we are done
                        String varInValue = value.substring(0, value.indexOf("=")); // we want the variable itself
                        if (varInValue.equals(varName)) {
                            relevantFactors.add(factor);
                            bool = true;
                        }
                    }
                    else {
                        break; // break if (bool is true <=> we found the var in the factor)
                    }
                }
                if (bool){ // again
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
                // if it's a common var than check if also equals
                if (commonVars.contains(str1.substring(0, str1.indexOf("=")))){
                    if (str1.equals(str2)){
                        count++;
                    }
                }
            }
        }
        // we want count to be the same as the common vars size => indicates of corresponding common vars values
        return count == commonVars.size();
    }

    // Function that returns new line after joining two lines
    // (if line1 contains A,B and line2 contains A,C so new line will contain A, B, C)
    // example -> line1 = (A=T, B=F), line2 = (A=T, C=T).
    // so new line = (A=T, B=F, C=T)
    public ArrayList<String> getNewLineAfterJoin (ArrayList<String> Line1, ArrayList<String> Line2) {
        ArrayList<String> newLine = new ArrayList<>(Line1); // Starting with line1 and adding part of line2 to it where needed
        for (String str2 : Line2){
            // add if doesn't contains
            if (!newLine.contains(str2)){
                newLine.add(str2);
            }
        }
        return newLine;
    }

    // Function which join two factors
    public void joinFactors(HashMap<ArrayList<String>,Double> factor1,
                            HashMap<ArrayList<String>,Double> factor2, Set<HashMap<ArrayList<String>, Double>> relevantFactors){
        Set<ArrayList<String>> alreadyAdded = new HashSet<>();
        ArrayList<String> commonVars = getCommonVars(factor1, factor2); // get the common vars - we are joining according to them
        HashMap<ArrayList<String>,Double> newFactor = new HashMap<>(); // the new factor after join
        for (ArrayList<String> cptLine_factor1 : factor1.keySet()) {
            for (ArrayList<String> cptLine_factor2 : factor2.keySet()) {
                if (checkIfJoinLines(cptLine_factor1, cptLine_factor2, commonVars)){ // check if common vars values are correspond
                    ArrayList<String> newLine = getNewLineAfterJoin(cptLine_factor1, cptLine_factor2); // get the new factor "joined" line
                    Double p1 = factor1.get(cptLine_factor1);
                    Double p2 = factor2.get(cptLine_factor2);
                    Double newP = p1*p2; // in join we multiply the correspond lines values
                    this.multCount++;
                    newFactor.put(newLine, newP); // adding the new line and its value to the new factor
                }
            }
        }
        //updating factors and relevant factors
        this.factors.add(newFactor);
        this.factors.remove(factor1);
        this.factors.remove(factor2);
        relevantFactors.add(newFactor);
        relevantFactors.remove(factor1);
        relevantFactors.remove(factor2);
        System.out.println("Factor after join: " + newFactor);
        System.out.println("Factor size: " + newFactor.size());
    }

    // Function that eliminates a var from a factor
    public void eliminateFactor (Variable var, HashMap<ArrayList<String>,Double> factor, Set<HashMap<ArrayList<String>, Double>> relevantFactors){
        int varIndex = getIndexInFactor(var, factor);
        System.out.println(var.getName());
        Set<ArrayList<String>> alreadyExist = new HashSet<>();
        HashMap<ArrayList<String>,Double> newFactor = new HashMap<>();
        Double ans = 0.0;
        for (ArrayList<String> cptLine_factor1 : factor.keySet()){
            if (alreadyExist.contains(cptLine_factor1)){
                continue;
            }
            ArrayList<String> newLine = new ArrayList<>();
            ans = factor.get(cptLine_factor1);
            for (ArrayList<String> cptLine_factor2 : factor.keySet()) {
                if (cptLine_factor1 != cptLine_factor2){
                    ArrayList<String> copy_cpt_factor1 = new ArrayList<>(cptLine_factor1);
                    newLine = copy_cpt_factor1;
                    ArrayList<String> copy_cpt_factor2 = new ArrayList<>(cptLine_factor2);
                    copy_cpt_factor1.remove(varIndex);
                    copy_cpt_factor2.remove(varIndex);
//                    System.out.println(var.getName());
                    if (checkIfLineIsEqual(copy_cpt_factor1, copy_cpt_factor2)){
                        ans += factor.get(cptLine_factor2);
                        alreadyExist.add(cptLine_factor2);
//                        System.out.println("PLUS IS: " + cptLine_factor1 + "+++++" + cptLine_factor2);
                        this.plusCount++;
                        //System.out.println(copy_cpt_factor1.toString() + copy_cpt_factor2.toString());

                    }
                }
            }
            newFactor.put(newLine, ans);
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
