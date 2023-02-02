import it.unisa.dia.gas.jpbc.Element;

public class RABEMSK {
    private Element g;
    private Element h;
    private Element a1;
    private Element a2;
    private Element b1;
    private Element b2;
    private Element g_d1;
    private Element g_d2;
    private Element g_d3;

    public RABEMSK(Element g, Element h,
                   Element a1, Element a2,
                   Element b1, Element b2,
                   Element g_d1, Element g_d2, Element g_d3) {
        this.g = g;
        this.h = h;
        this.a1 = a1;
        this.a2 = a2;
        this.b1 = b1;
        this.b2 = b2;
        this.g_d1 = g_d1;
        this.g_d2 = g_d2;
        this.g_d3 = g_d3;
    }

    public Element getG() {
        return g;
    }

    public Element getH() {
        return h;
    }

    public Element getA1() {
        return a1;
    }

    public Element getA2() {
        return a2;
    }

    public Element getB1() {
        return b1;
    }

    public Element getB2() {
        return b2;
    }

    public Element getG_d1() {
        return g_d1;
    }

    public Element getG_d2() {
        return g_d2;
    }

    public Element getG_d3() {
        return g_d3;
    }
}
