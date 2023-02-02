import it.unisa.dia.gas.jpbc.Element;

public class RABEDK {
    private String[] attrs;
    private long t;
    private Element[] sk0;
    private Element[][] sk_y;
    private Element[] sk_p;

    public RABEDK(String[] attrs,
                  long t,
                  Element[] sk0,
                  Element[][] sk_y,
                  Element[] sk_p) {
        this.attrs = attrs;
        this.t = t;
        this.sk0 = sk0;
        this.sk_y = sk_y;
        this.sk_p = sk_p;
    }

    public String[] getAttrs() {
        return attrs;
    }

    public long getT() {
        return t;
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
}
