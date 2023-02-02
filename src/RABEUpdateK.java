import it.unisa.dia.gas.jpbc.Element;

public class RABEUpdateK {
    private long t;
    private Integer[] thetas;
    private Element[][] ku_thetas;

    public RABEUpdateK(long t, Integer[] thetas, Element[][] ku_thetas) {
        this.t = t;
        this.thetas = thetas;
        this.ku_thetas = ku_thetas;
    }

    public long getT() {
        return t;
    }

    public Integer[] getThetas() {
        return thetas;
    }

    public Element[][] getKu_thetas() {
        return ku_thetas;
    }
}
