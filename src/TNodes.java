import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
//import smu.smc.dcph.Utils;

import java.util.*;

public class TNodes {
    private Element[] nodes;
    private int num;

    public TNodes(){

    }

    public TNodes(Element[] nodes, int num) {
        this.nodes = nodes;
        this.num = num;
    }

    public static TNodes newInstance(int num){
        Element[] nodes = new Element[num * 2 - 1];
        return new TNodes(nodes, num);
    }

    public static TNodes newInstance(int num, Field group){
        Element[] nodes = new Element[num * 2 - 1];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = group.newRandomElement().getImmutable();
        }
        return new TNodes(nodes, num);
    }

    public Element[] getNodes() {
        return nodes;
    }

    public int getNum() {
        return num;
    }

    public Integer[] getPath(int lid){
        int pos = lid;

        List<Integer> res = new ArrayList<>();
        res.add(pos);
        while(pos > 0){
            pos = (pos - 1) / 2;
            res.add(pos);
        }
        Integer[] resint = new Integer[res.size()];
        for (int i = 0; i < res.size(); i++) {
            resint[i] = res.get(i);
        }

        return resint;
    }

    private int checkLeaf(int id){
        if(id >= num - 1 && id < 2 * num - 1){
            return 1;
        }else if(id < num - 1 && id >= 0){
            return 0;
        }else{
            return -1;
        }
    }

    public int getMappedId(int uid){
        return uid + num - 1;
    }

    public List<Integer> tnodes( long t){

        Set<Integer> Y = new HashSet<>();



        int temp;
        temp = (int)t;
        Y.add(temp);
        Y.add(temp +1);
        Y.add(temp + 2);

        if(Y.size() == 0){
            Y.add(0);
        }

        return new ArrayList<>(Y);
    }
}

