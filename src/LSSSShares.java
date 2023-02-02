import it.unisa.dia.gas.jpbc.Element;

public class LSSSShares {
    private Vector vector;
    private String[] map;

    public LSSSShares(Vector vector, String[] map){
        this.vector = vector;
        this.map = map;
    }

    public String toString(){
        int len = this.vector.getLen();
        String res = "{\n";
        for(int i = 0; i < len; i++){
            res += "    " + this.vector.getValue(i) + "  -- " + this.map[i] + " " + i + "\n";
        }
        res += "}";
        return res;
    }

    public LSSSShares extract(int[] indexes){
        Vector vector = this.vector.extract(indexes);
        String[] map = new String[indexes.length];
        for(int i = 0; i < indexes.length; i++){
            map[i] = this.map[indexes[i]];
        }
        return new LSSSShares(vector, map);
    }

    public LSSSShares extract(String[] attrs){
        int[] res = Utils.search(this.map, attrs)[0];
        return extract(res);
    }

    public Element recover(Vector lambda){
        return this.vector.transform().mul1(lambda);
    }

    public Element getValue(int i){
        return this.vector.getValue(i);
    }

    public String getMap(int i){
        return this.map[i];
    }
}
