import it.unisa.dia.gas.jpbc.Element;

public class RABEUSK {
    private int uid;
    private Element[] sk0;
    private Element[][] sk_y;
    private Element[] sk_p;
    private Element[] sk_theta;
    private Integer[] thetas;
    private String[] attrs;

    public RABEUSK(int uid,
                   Element[] sk0,
                   Element[][] sk_y,
                   Element[] sk_p,
                   Element[] sk_theta,
                   Integer[] thetas,
                   String[] attrs) {
        this.uid = uid;
        this.sk0 = sk0;
        this.sk_y = sk_y;
        this.sk_p = sk_p;
        this.sk_theta = sk_theta;
        this.thetas = thetas;
        this.attrs = attrs;
    }

    public int getUid() {
        return uid;
    }

    public Element[] getSk0() {
        return sk0;
    }

    public Element[][] getSk_y() {
        return sk_y;
    }

    public Element[] getSk_p() {
        return sk_p;
    }

    public Element[] getSk_theta() {
        return sk_theta;
    }

    public Integer[] getThetas() {
        return thetas;
    }

    public String[] getAttrs() {
        return attrs;
    }
}
