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
    public String algo1(String q) {
        String[] query = q.split(",");
        ArrayList<String> varsNames = network.getVarsNames();
        ArrayList<String> queryVars = new ArrayList<>();
        // Adding the variables
        for (String value : query) {
            queryVars.add(Character.toString(value.charAt(0)));
        }
        ArrayList<String> hiddenVarsNames = new ArrayList<>();
        // Adding hidden vars
        for (String var : varsNames) {
            if (!queryVars.contains(var)) {
                hiddenVarsNames.add(var);
                System.out.println("Hidden: " + var);
            }
        }
        // Arraylist of the final query - first we are adding the query and evidence vars and values
        Hashtable<Variable, String> finalQuery = new Hashtable<>();
        for (String value : query) {
            String varName = Character.toString(value.charAt(0));
            Variable var = network.getVarByName(varName);
            String varValue = Character.toString(value.charAt(2));
            finalQuery.put(var, varValue);
        }

        for (String hiddenVarName : hiddenVarsNames) {
            Variable hiddenVar = network.getVarByName(hiddenVarName);
            String firstValue = hiddenVar.getValues().get(0);
            finalQuery.put(hiddenVar, firstValue);
        }
        // mone calculate
        System.out.println("Mone");
        Double mone = iterateHiddenQueries(finalQuery, hiddenVarsNames);
        System.out.println("End Mone" + mone);
        Double mechane = mone;
        String firstVarName = Character.toString(query[0].charAt(0));
        Variable firstVar = network.getVarByName(firstVarName);
        String oldValue = firstVar.getValues().get(0);
        for (String newValue : firstVar.getValues()){
            if (!newValue.equals(oldValue)){
                finalQuery.replace(firstVar, oldValue, newValue);
                mechane += iterateHiddenQueries(finalQuery, hiddenVarsNames);
                oldValue = newValue;
            }
        }
        System.out.println("Final Answer= " + mone/mechane);
        return "";
    }

    public Double iterateHiddenQueries(Hashtable<Variable, String> query, ArrayList<String> hiddenVarsNames){
        Double finalAnswer = 0.0;
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
            System.out.println("I: " + i);
            System.out.println("Starting Algo: ");
            for (Variable var : query.keySet()){
                System.out.println(var.getName() + "=" + query.get(var));
            }
            System.out.println("+");
            finalAnswer += calculateQuery(query);
        }
        return finalAnswer;
    }

    // Given a query with Node and it's values - we calculate it
    public Double calculateQuery(Hashtable<Variable, String> query){
        Double finalAns = 1.0;
        for (Variable var : query.keySet()){
            ArrayList<String> answerQuery = new ArrayList<>();
            // If var has no parents
            if (var.getParents().size()==0){
                ArrayList<String> valueArrList = new ArrayList<>();
                valueArrList.add(var.getName() + "=" + query.get(var));
                System.out.println(var.getName() + "=" + query.get(var) + "=" + var.getCpt().get(valueArrList));
                finalAns *= var.getCpt().get(valueArrList);
            }
            else {
                // First we are adding the nodeVar
                answerQuery.add(var.getName() + "=" + query.get(var));
                //System.out.println("AnswerQuery");
                //System.out.println(var.getName() + "=" + query.get(var));
                // Now we are adding all the parents and their current query value to the answer query
                for (Variable parent : var.getParents()){
                    answerQuery.add(parent.getName() + "=" + query.get(parent));
                }
                //System.out.println("End");
            }

            // Now we have the answer query for the current var
            // for example - if var B has parent A, and the query is P(A=T, C=F,.,B=F,..)
            // for A=T we will get P(A=T|B=F) -> answer query
            for (ArrayList<String> cptLine : var.getCpt().keySet()){
                int counter = 0;
                for (String answerQueryValue : answerQuery){
                    String name = Character.toString(answerQueryValue.charAt(0));
                    String val = Character.toString(answerQueryValue.charAt(2));
                    //System.out.println("AnswerQueryValue: " + name + "=" + val);
                    if (cptLine.contains(answerQueryValue)){
                        if (++counter==cptLine.size()){
                            finalAns *= var.getCpt().get(cptLine);
                            System.out.println(var.getName() + "=" + query.get(var) + "=" + var.getCpt().get(cptLine));
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
        return finalAns;
    }
}
