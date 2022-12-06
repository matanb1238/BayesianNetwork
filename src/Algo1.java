import java.security.KeyPair;
import java.util.*;

public class Algo1 {
    private BayesianNetwork network;


    public Algo1(BayesianNetwork network){
        this.network = network;
    }
    public Double checkExist(String q){
        boolean bool = true;
        String[] query = q.split(","); // Splitting the query (we want to get into each one of the vars easily)
        String nodeName = Character.toString(query[0].charAt(0)); // The node is the first one
        Variable node = network.getVarByName(nodeName);
        System.out.println(q);
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
    public ArrayList<String> algo1(String q) {
        String[] query = q.split(",");
        ArrayList<String> varsNames = network.getVarsNames();
        ArrayList<String> queryVars = new ArrayList<>();
        // Adding the variables
        for (String value : query) {
            int index = 0;
            for (int i=0; i<value.length(); i++){
                if (value.charAt(i) == '='){
                    index = i;
                }
                else{
                    continue;
                }
                break;
            }
            queryVars.add(value.substring(0, index));
        }
        ArrayList<String> hiddenVarsNames = new ArrayList<>();
        // Adding hidden vars
        for (String var : varsNames) {
            if (!queryVars.contains(var)) {
                hiddenVarsNames.add(var);
            }
        }
        // Arraylist of the final query - first we are adding the query and evidence vars and values
        Hashtable<Variable, String> finalQuery = new Hashtable<>();
        for (String value : query) {
            int index = 0;
            for (int i=0; i<value.length(); i++){
                if (value.charAt(i) == '='){
                    index = i;
                }
                else{
                    continue;
                }
                break;
            }
            String varName = value.substring(0, index);
            Variable var = network.getVarByName(varName);
            String varValue = value.substring(index+1, value.length());
            finalQuery.put(var, varValue);
        }

        for (String hiddenVarName : hiddenVarsNames) {
            Variable hiddenVar = network.getVarByName(hiddenVarName);
            String firstValue = hiddenVar.getValues().get(0);
            finalQuery.put(hiddenVar, firstValue);
        }
        String plusCount = "0";
        String multCount = "0";
        // mone calculate
        ArrayList<String> moneList = iterateHiddenQueries(finalQuery, hiddenVarsNames, plusCount, multCount);
        int integerPlusCount = Integer.valueOf(moneList.get(1));
        int integerMultCount = Integer.valueOf(moneList.get(2));
        plusCount = Integer.toString(integerPlusCount);
        multCount = Integer.toString(integerMultCount);
        Double mone = Double.parseDouble(moneList.get(0));
        System.out.println("Mone: " + mone);
        Double mechane = mone;
        int index = 0;
        for (int i=0; i<query[0].length(); i++){
            if (query[0].charAt(i) == '='){
                index = i;
            }
            else{
                continue;
            }
            break;
        }
        String firstVarName = query[0].substring(0, index);
        int len = query[0].length();
        Variable firstVar = network.getVarByName(firstVarName);
        String oldValue = query[0].substring(index+1, len);
        ArrayList<String> oldValues = new ArrayList<>();
        oldValues.add(oldValue);
        // Now we will calculate the mechane - we will iterate on all the "other" values of the first var in the query
        for (String newValue : firstVar.getValues()){
            if (!oldValues.contains(newValue)){ // We don't want the first value (from the original query)
                finalQuery.replace(firstVar, oldValue, newValue);
                ArrayList<String> mechaneList = iterateHiddenQueries(finalQuery, hiddenVarsNames, plusCount, multCount);
                // Updating counters
                mechane += Double.parseDouble(mechaneList.get(0));
                System.out.println("Mechane: " + mechane);
                int plusCounter = Integer.valueOf(mechaneList.get(1));
                plusCount = Integer.toString(++plusCounter); // Adding one because we did another plus action now
                int multCounter = Integer.valueOf(mechaneList.get(2));
                multCount = Integer.toString(multCounter);
                oldValues.add(newValue);
                oldValue=newValue;
                System.out.print("Check if newvalue=oldvalue is neccessary");
            }
        }
        ArrayList<String> finalAnswerList = new ArrayList<>();
        finalAnswerList.add(Double.toString(mone/mechane));
        finalAnswerList.add(plusCount);
        finalAnswerList.add(multCount);
        return finalAnswerList;
    }

    public ArrayList<String> iterateHiddenQueries(Hashtable<Variable, String> query, ArrayList<String> hiddenVarsNames, String plusCount, String multCount){
        Double finalAnswer = 0.0;
        int plusIndex = 0; // We will use an index to figure the first plus action - we don't want to consider it to our count
        // Now we are adding the hidden vars
        // We also want to calculate the number of values of the hidden vars
        // so we can iterate over them according to the algorithm
        int hiddenValuesVarsCount = 1;
        for (String varName : hiddenVarsNames) {
            Variable var = network.getVarByName(varName);
            hiddenValuesVarsCount *= var.getValues().size();
        }
        for (int i=0; i<hiddenValuesVarsCount; i++){
            for (int j=0; j<hiddenVarsNames.size(); j++) {
                int valuesCount = 1;
                for (int x=0; x<j; x++){
                    String currVarName = hiddenVarsNames.get(x);
                    Variable currVar = network.getVarByName(currVarName);
                    valuesCount *= currVar.getValues().size();
                }
                String varName = hiddenVarsNames.get(j);
                Variable hiddenVar = network.getVarByName(varName);
                if (i % valuesCount == 0){
                    String currValue = query.get(hiddenVar);
                    int valueIndex = hiddenVar.getValues().indexOf(currValue);
                    String newValue = "";
                    if (valueIndex == hiddenVar.getValues().size()-1){
                        newValue = hiddenVar.getValues().get(0);
                    }
                    else{
                        newValue = hiddenVar.getValues().get(valueIndex + 1);
                    }
                    query.remove(hiddenVar);
                    query.put(hiddenVar, newValue);
                }
            }
            ArrayList<String> currFinalAnswerList = calculateQuery(query, plusCount, multCount);
            Double ans = Double.parseDouble(currFinalAnswerList.get(0));
            finalAnswer += ans;
            if (plusIndex!=0){
                int plusCounter = Integer.valueOf(currFinalAnswerList.get(1));
                plusCount = Integer.toString(++plusCounter);
            }
            plusIndex++;
            multCount = currFinalAnswerList.get(2);
        }
        System.out.println("plusCount: " + plusCount);
        System.out.println("multCount: " + multCount);
        ArrayList<String> finalAnswerList = new ArrayList<>();
        finalAnswerList.add(Double.toString(finalAnswer));
        finalAnswerList.add(plusCount);
        finalAnswerList.add(multCount);
        return finalAnswerList;
    }

    // Given a query with Node and it's values - we calculate it
    public ArrayList<String> calculateQuery(Hashtable<Variable, String> query, String plusCount, String multCount){
        Double finalAns = 1.0;
        int multCounter = Integer.valueOf(multCount);
        int multIndex = 0; // We don't want to consider the first multiplying so we will use an index to figure the first one
        for (Variable var : query.keySet()){
            ArrayList<String> answerQuery = new ArrayList<>();
            // If var has no parents
            if (var.getParents().size()==0){
                ArrayList<String> valueArrList = new ArrayList<>();
                valueArrList.add(var.getName() + "=" + query.get(var));
                finalAns *= var.getCpt().get(valueArrList);
                if (multIndex!=0){ // If not first multiplying
                    multCounter++;
                }
                multIndex++;
            }
            else {
                // First we are adding the nodeVar
                answerQuery.add(var.getName() + "=" + query.get(var));
                // Now we are adding all the parents and their current query value to the answer query
                for (Variable parent : var.getParents()){
                    answerQuery.add(parent.getName() + "=" + query.get(parent));
                }
            }

            // Now we have the answer query for the current var
            // for example - if var B has parent A, and the query is P(A=T, C=F,.,B=F,..)
            // for A=T we will get P(A=T|B=F) -> answer query
            for (ArrayList<String> cptLine : var.getCpt().keySet()){
                int counter = 0;
                for (String answerQueryValue : answerQuery){
                    String name = Character.toString(answerQueryValue.charAt(0));
                    String val = Character.toString(answerQueryValue.charAt(2));
                    if (cptLine.contains(answerQueryValue)){
                        if (++counter==cptLine.size()){
                            finalAns *= var.getCpt().get(cptLine);
                            if (multIndex!=0){ // If not first multiplying
                                multCounter++;
                            }
                            multIndex++;
                        }
                        else{
                            continue;
                        }
                    }
                    else{
                        break;
                    }
                }
            }

        }
        ArrayList<String> finalAnswerList = new ArrayList<>();
        finalAnswerList.add(Double.toString(finalAns));
        finalAnswerList.add(plusCount);
        finalAnswerList.add(Integer.toString(multCounter));
        return finalAnswerList;
    }
}
