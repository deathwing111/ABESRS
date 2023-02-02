import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
//import smu.smc.dcph.Utils;

import java.util.*;

public class KUNodes {
    private Element[] nodes;
    private int num;

    public KUNodes(){

    }

    public KUNodes(Element[] nodes, int num) {
        this.nodes = nodes;
        this.num = num;
    }

    public static KUNodes newInstance(int num){
        Element[] nodes = new Element[num * 2 - 1];
        return new KUNodes(nodes, num);
    }

    public static KUNodes newInstance(int num, Field group){
        Element[] nodes = new Element[num * 2 - 1];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = group.newRandomElement().getImmutable();
        }
        return new KUNodes(nodes, num);
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

    public List<Integer> kunodes(RevokeList rl, long t){
        Set<Integer> X = new HashSet<>();
        Set<Integer> Y = new HashSet<>();

        for (int i = 0; i < rl.getSize(); i++) {
            if(rl.getTime(i) <= t) {
                X.addAll(Arrays.asList(getPath(getMappedId(rl.getUid(i)))));
            }
        }

        int posl, posr, flag;
        for (int id: X){
            flag = checkLeaf(id);
            if(flag != 0)
                continue;
            posl = 2 * id + 1;
            posr = 2 * id + 2;
            if(!X.contains(posl)){
                Y.add(posl);
            }
            if(!X.contains(posr)){
                Y.add(posr);
            }

        }

        if(Y.size() == 0){
            Y.add(0);
        }

        return new ArrayList<>(Y);
    }
}
