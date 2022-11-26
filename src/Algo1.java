import java.util.ArrayList;
import java.util.HashMap;

public class Algo1 {
    private BayesianNetwork network;


    public Algo1(BayesianNetwork network){
        this.network = network;
    }
    public boolean checkExist(String q){
        String[] query = q.split(",");
        String nodeName = query[0];
        if (!network.checkIfVarExist(nodeName)){
            return false;
        }
        Variable node = network.getVarByName(nodeName);
//        for (String queryValue : query){
//            for (ArrayList<String> cptLine : node.getCpt().keySet())
//
//            if queryValue
//        }
        return false;
    }
    public String algo1(String q){
        String[] query = q.split(",");

        System.out.println(q);
        return "";
    }
}
