import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class RABEMPK {
    private Pairing pairing;
    private Element h1;
    private Element h2;
    private Element t1;
    private Element t2;

    public RABEMPK(Pairing pairing, Element h1, Element h2, Element t1, Element t2) {
        this.pairing = pairing;
        this.h1 = h1;
        this.h2 = h2;
        this.t1 = t1;
        this.t2 = t2;
    }

    public Pairing getPairing() {
        return pairing;
    }

    public Element getH1() {
        return h1;
    }

    public Element getH2() {
        return h2;
    }

    public Element getT1() {
        return t1;
    }

    public Element getT2() {
        return t2;
    }
}
