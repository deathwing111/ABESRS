import it.unisa.dia.gas.jpbc.*;
import java.util.*;
public class main {
    private RABEMSK msk;
    private RABEMPK mpk;
    private RevokeList rl;
    private KUNodes st;
    private  TNodes tn;
    public main(RABEMPK mpk, RABEMSK msk) {
        this.mpk = mpk;
        this.msk = msk;
    }
    public void setup(int num, Pairing pairing,long t){
        Element g = pairing.getG1().newRandomElement().getImmutable();
        Element h = pairing.getG2().newRandomElement().getImmutable();
        Element e = pairing.pairing(g, h).getImmutable();

        Element a1 = pairing.getZr().newRandomElement();
        Element a2 = pairing.getZr().newRandomElement();
        Element b1 = pairing.getZr().newRandomElement();
        Element b2 = pairing.getZr().newRandomElement();
        Element d1 = pairing.getZr().newRandomElement();
        Element d2 = pairing.getZr().newRandomElement();
        Element d3 = pairing.getZr().newRandomElement();

        // H1 = h ^ a1
        Element H1 = h.powZn(a1).getImmutable();
        // H2 = h ^ a2
        Element H2 = h.powZn(a2).getImmutable();
        // T1 = e(g, h) ^ (d1*a1 + d3)
        Element T1 = e.powZn(d1.duplicate().mul(a1).add(d3)).getImmutable();
        // T2 = e(g, h) ^ (d2*a2 + d3)
        Element T2 = e.powZn(d2.duplicate().mul(a2).add(d3)).getImmutable();

        Element[] glt = new Element[(int)Math.log(t)];
        for (int i = 0; i < Math.log(t)-3; i++){
            glt[i] = pairing.getG1().newRandomElement().getImmutable();
        }

        Element g_d1 = g.powZn(d1).getImmutable();
        Element g_d2 = g.powZn(d2).getImmutable();
        Element g_d3 = g.powZn(d3).getImmutable();
        this.mpk = new RABEMPK(pairing, H1, H2, T1, T2);
        this.msk = new RABEMSK(g, h,
                a1.getImmutable(), a2.getImmutable(),
                b1.getImmutable(), b2.getImmutable(),
                g_d1, g_d2, g_d3);
        this.st = KUNodes.newInstance(num, pairing.getG1());
        this.rl = new RevokeList();
        return;
    }

    public RABEUSK kgen(int id, String[] attrs){
        Field zr = this.mpk.getPairing().getZr();
        Field g1 = this.mpk.getPairing().getG1();

        Element r1 = zr.newRandomElement();
        Element r2 = zr.newRandomElement();

        Element b1 = this.msk.getB1();
        Element b2 = this.msk.getB2();
        Element[] ainvs = new Element[]{
                this.msk.getA1(), this.msk.getA2()
        };
        Element[] g_dz = new Element[]{
                this.msk.getG_d1(),
                this.msk.getG_d2(),
                this.msk.getG_d3()
        };
        Element g = this.msk.getG();
        Element h = this.msk.getH();

        Element[] tmp = new Element[3];
        tmp[0] = b1.mul(r1);
        tmp[1] = b2.mul(r2);
        tmp[2] = r1.duplicate().add(r2);

        Element[] sk0 = new Element[3];
        for (int k = 0; k < 3; k++) {
            sk0[k] = h.powZn(tmp[k]).getImmutable();
        }

        Element[][] exps = new Element[2][3];
        for (int z = 0; z < 2; z++){
            for (int k = 0; k < 3; k++){
                exps[z][k] = tmp[k].mul(ainvs[z]);
            }
        }
        Element sigma_y;
        Element[][] sk_y_z = new Element[attrs.length][3];
        byte[][] conps = new byte[][]{null, new byte[2]};
        for(int i = 0; i < attrs.length; i++){
            sigma_y = zr.newRandomElement();
            conps[0] = attrs[i].getBytes();
            for(int z = 0; z < 2; z++){
                sk_y_z[i][z] = g.powZn(sigma_y.duplicate().mul(ainvs[z]));
                conps[1][1] = (byte) (z + 1);
                for (int k = 0; k < 3; k++) {
                    conps[1][0] = (byte) (k + 1);
                    sk_y_z[i][z] = sk_y_z[i][z].mul(
                            Utils.HashesE(conps, g1).powZn(exps[z][k])
                    );
                }
                sk_y_z[i][z] = sk_y_z[i][z].getImmutable();
            }
            sk_y_z[i][2] = g.powZn(sigma_y.negate()).getImmutable();
        }

        Element sigma_p = zr.newRandomElement();
        Element[] g_sig_a_s = new Element[]{
                g.powZn(sigma_p.duplicate().mul(ainvs[0])),
                g.powZn(sigma_p.duplicate().mul(ainvs[1]))
        };
        Element[] sk_s_p = new Element[2];
        byte[] conps_p = new byte[]{0,1,0,0};
        for(int z = 0; z < 2; z++){
            sk_s_p[z] = g_dz[z];
            conps_p[3] = (byte) (z + 1);
            for (int k = 0; k < 3; k++) {
                conps_p[2] = (byte) (k + 1);
                sk_s_p[z] = sk_s_p[z].mul(
                        Utils.HashE(conps_p, g1).powZn(exps[z][k])
                );
            }
            sk_s_p[z] = sk_s_p[z].mul(g_sig_a_s[z]).getImmutable();
        }

        Integer[] thetas = this.st.getPath(this.st.getMappedId(id));
        Element[] sk_theta = new Element[thetas.length];
        Element g_theta;
        Element tmpE = g_dz[2].mul(
                g.powZn(sigma_p.duplicate().negate())
        );
        for (int i = 0; i < thetas.length; i++) {
//            if(this.st.getNodes()[thetas[i]] == null)
//                this.st.getNodes()[thetas[i]] = g1.newRandomElement().getImmutable();
            g_theta = this.st.getNodes()[thetas[i]];
            sk_theta[i] = tmpE.duplicate().mul(g_theta.invert()).getImmutable();
        }

        RABEUSK usk = new RABEUSK(id, sk0, sk_y_z, sk_s_p, sk_theta, thetas, attrs);
        return usk;
    }

    public KUNodes getSt() {
        return st;
    }

    public RABEMPK getMpk() {
        return mpk;
    }

    public RABEMSK getMsk() {
        return msk;
    }

    public RevokeList getRl() {
        return rl;
    }

    public RABEUpdateK kupdate(long t){
        Integer[] thetas = this.st.kunodes(this.rl, t).toArray(new Integer[0]);

        Field zr = this.mpk.getPairing().getZr();
        Field g1 = this.mpk.getPairing().getG1();
        Element g_1 = g1.newOneElement();
        Element g_theta, r_theta;
        Element[][] ku_thetas = new Element[thetas.length][2];
        byte[][] conps = new byte[][]{
                new byte[]{1}, Utils.long2bytes(t)
        };
        for (int i = 0; i < thetas.length; i++) {
            g_theta = this.st.getNodes()[thetas[i]];
            r_theta = zr.newRandomElement();

            for (int j = 0; j < t; j++){//lt
                //g_1 = g_1.mul(this.st.getNodes()[j]).powZn(g1.newElementFromHash(conps[j],0,1));//.powZn(mapping function);
                g_1 = g_1.mul(g1.newRandomElement().powZn(zr.newRandomElement()));//.powZn(mapping function);
            }
            ku_thetas[i][0] = g_theta.mul(//same
                    g_1.powZn(r_theta)
            ).getImmutable();
            ku_thetas[i][1] = this.msk.getH().powZn(r_theta).getImmutable();//same
        }

        return new RABEUpdateK(t, thetas, ku_thetas);
    }
    public RABEDK dkgen(RABEUSK sk, RABEUpdateK upk,long t){

        Integer[] thetas = this.st.kunodes(this.rl, t).toArray(new Integer[0]);
        Field zr = this.mpk.getPairing().getZr();
        Field g1 = this.mpk.getPairing().getG1();
        Element g_1 = g1.newOneElement();
        Element r_theta = zr.newRandomElement();
        Element g_theta = null;
        for (int i = 0; i < thetas.length; i++) {
             g_theta = this.st.getNodes()[thetas[i]];
        }

        List<Integer> ku_list = Arrays.asList(upk.getThetas());
        List<Integer> sk_list = Arrays.asList(sk.getThetas());
        Set<Integer> kunodes = new HashSet<>(ku_list);
        Set<Integer> ps = new HashSet<>(sk_list);
        ps.retainAll(kunodes);
        if(ps.size() < 1) {
            //System.out.println("can not gen dk for user " + sk.getUid() + " !");
            return null;
        }
        if(ps.size() > 1){
            System.out.println("Error! should only one node");
            return null;
        }

        Integer theta = ps.toArray(new Integer[0])[0];
        int sk_index = sk_list.indexOf(theta);
        int upk_index = ku_list.indexOf(theta);

        Element r = this.mpk.getPairing().getZr().newRandomElement();
        Element[] sk_p = new Element[4];

        byte[][] conps = new byte[][]{
                new byte[]{1}, Utils.long2bytes(upk.getT())
        };
        sk_p[0] = sk.getSk_p()[0];
        sk_p[1] = sk.getSk_p()[1];
        sk_p[2] = sk.getSk_theta()[sk_index].mul(
                upk.getKu_thetas()[upk_index][0]
        ).mul(
                Utils.HashesE(
                        conps, this.mpk.getPairing().getG1()
                ).powZn(r)
        ).getImmutable();
        for (int j = 0; j < t; j++){
            g_1 = g_1.mul(g1.newRandomElement().powZn(zr.newRandomElement()));//.powZn(mapping function);
        }
        Element temp = g_theta.mul(//same
                g_1
        ).getImmutable();
        sk_p[3] = upk.getKu_thetas()[upk_index][1].mul(
                temp.powZn(r)//this.msk.getH().powZn(r)
        ).getImmutable();

        return new RABEDK(sk.getAttrs(), upk.getT(), sk.getSk0(), sk.getSk_y(), sk_p);
    }
    public RABECipher enc(Element m, LSSSMatrix policy, long t, RABEUpdateK upk){


        Field zr = this.mpk.getPairing().getZr();
        Element[] ss = new Element[]{
                zr.newRandomElement(),
                zr.newRandomElement()};

        return enc(m, policy, t, ss, upk);
    }
    //enc之前是共同的部分
    public RABECipher enc(Element m, LSSSMatrix policy, long t, Element[] ss, RABEUpdateK upk){
        //Integer[] thetas = this.st.kunodes(this.rl, t).toArray(new Integer[0]);
        Field zr = this.mpk.getPairing().getZr();
        Field g1 = this.mpk.getPairing().getG1();

//        Element[] glt = new Element[(int)Math.log(t)];
//        for (int i = 0; i < Math.log(t)-3; i++){
//            glt[i] = g1.newRandomElement().getImmutable();
//        }
//        Integer[] temptn = this.tn.tnodes(t).toArray( new Integer[0]);
//
//        for (int i = 0; i < temptn.length; i++){
//            int a = temptn[i];
//        }
        Set<Integer> Y = new HashSet<>();
        int tem;
        tem = (int)t;
        Y.add(tem);
        Y.add(tem +1);
        Y.add(tem + 2);

        Element g_1 = g1.newOneElement();
        byte[][] conps = new byte[][]{
                new byte[]{1}, Utils.long2bytes(upk.getT())
        };
        //Element g_theta = null;
        int temp = (int)t;
        Element[] c0 = new Element[4 + temp];
        Element sums = ss[0].duplicate().add(ss[1]);
        for (int j = 0; j < 3; j++){
            g_1 = g_1.mul(g1.newRandomElement().powZn(zr.newRandomElement()));//.powZn(mapping function);

            for (int i = 0; i < Math.log(t)-3; i++) {
                //g_theta = this.st.getNodes()[thetas[i]];
                c0[i + 3] = g_1.powZn(sums).getImmutable();
            }
            //g.powZn(sums).getImmutable();
        }
        c0[0] = this.mpk.getH1().powZn(ss[0]).getImmutable();
        c0[1] = this.mpk.getH2().powZn(ss[1]).getImmutable();
        c0[2] = this.msk.getH().powZn(sums).getImmutable();
        c0[3] = Utils.HashesE(//c_yita
                new byte[][]{
                        new byte[]{1}, Utils.long2bytes(t)
                }, g1).powZn(sums).getImmutable();

        int row = policy.getRows();
        int col = policy.getCols();
        Element[][] cs = new Element[row][3];
        byte[][] tmpbytes = new byte[][]{null, new byte[]{0,0}};
        byte[] tmpbyte = new byte[]{0,0,0,0};
        Element tmpe;
        for (int i = 0; i < row; i ++){
            for (int l = 0; l < 3; l ++){
                tmpbytes[0] = policy.getMap()[i].getBytes();
                tmpbytes[1][0] = (byte) (l + 1);
                cs[i][l] = g1.newOneElement();
                for(int z = 0; z < 2; z++){
                    tmpbytes[1][1] = (byte) (z + 1);
                    cs[i][l] = cs[i][l].mul(
                            Utils.HashesE(
                                    tmpbytes, g1
                            ).powZn(ss[z])
                    );
                }

                tmpbyte[2] = (byte) (l + 1);
                for (int j = 0; j < col; j++) {
//                    tmpbyte[1] = (byte) (j + 1);
                    tmpbyte[1] = 1;
                    tmpe = g1.newOneElement();
                    for(int kk = 0; kk < 2; kk++){
                        tmpbyte[3] = (byte) (kk + 1);
                        tmpe = tmpe.mul(
                                Utils.HashE(
                                        tmpbyte, g1
                                ).powZn(ss[kk])
                        );
                    }
                    tmpe = tmpe.powZn(policy.getMatrix().getValue(i,j));
                    cs[i][l] = cs[i][l].mul(tmpe);

                }
                cs[i][l] = cs[i][l].getImmutable();
            }
        }

        Element c_p = this.mpk.getT1().powZn(ss[0]).mul(
                this.mpk.getT2().powZn(ss[1])
        ).mul(m).getImmutable();

        return new RABECipher(policy, t, c0, cs, c_p);
    }

    public void ctupt(Element m, RABEUpdateK upk, long t, Element[] ss, LSSSMatrix policy,long t_){
        Integer[] thetas = this.st.kunodes(this.rl, t).toArray(new Integer[0]);
        Field g1 = this.mpk.getPairing().getG1();
        Element g_1 = g1.newOneElement();
        Field zr = this.mpk.getPairing().getZr();

        Set<Integer> Y = new HashSet<>();
        int tem;
        tem = (int)t;
        Y.add(tem);
        Y.add(tem +1);
        Y.add(tem + 2);
        Set<Integer> X = new HashSet<>();
        int temp_;
        temp_ = (int)t_;
        X.add(temp_);
        X.add(temp_ +1);
        X.add(temp_ + 2);


        byte[][] conps = new byte[][]{
                new byte[]{1}, Utils.long2bytes(upk.getT())
        };

        for (int j = 0; j < Math.log(t)-3; j++) {
            g_1 = g_1.mul(g1.newRandomElement().powZn(zr.newRandomElement()));//.powZn(mapping function);
        }
        Element sums = ss[0].duplicate().add(ss[1]);
        Element[] ct = new Element[thetas.length];
         ct[0] = g_1.powZn(sums).getImmutable();
         for(int i = 1; i < Math.log(t)-3; i++){
             ct[i] = g1.newRandomElement().powZn(sums).getImmutable();
         }
        Element s_1 = zr.newRandomElement();
        Element s_2 = zr.newRandomElement();
        Element sums_ = s_1.add(s_2).getImmutable();
        Element[] temp = new Element[(int)Math.log(t)];
        temp[0] = g_1.powZn(sums_).getImmutable();
        for(int i = 1; i < Math.log(t)-3; i++){
            temp[i] = g1.newRandomElement().powZn(sums_).getImmutable();
        }
        Element[] c_t = new Element[(int)Math.log(t)];
        for (int i = 0;i < Math.log(t)-3; i++){
            c_t[i] = ct[i].mul(temp[i]).getImmutable();
        }
        Element[] tc0 = new  Element[3];
        tc0[0] = this.mpk.getH1().powZn(s_1).getImmutable();
        tc0[1] = this.mpk.getH2().powZn(s_2).getImmutable();
        tc0[2] = this.msk.getH().powZn(sums_).getImmutable();
        Element[] c_0 = new Element[3 + (int)Math.log(t)];
        Element[] c0 = new Element[3];
        c0[0] = this.mpk.getH1().powZn(ss[0]).getImmutable();
        c0[1] = this.mpk.getH2().powZn(ss[1]).getImmutable();
        c0[2] = this.msk.getH().powZn(sums).getImmutable();
        c_0[0] = c0[0].mul(tc0[0]).getImmutable();
        c_0[1] = c0[1].mul(tc0[1]).getImmutable();
        c_0[2] = c0[2].mul(tc0[2]).getImmutable();
        for (int i = 3; i < Math.log(t); i++){
            c_0[i]  = c_t[i - 3];
        }

        int row = policy.getRows();
        int col = policy.getCols();
        Element[][] cs = new Element[row][3];
        byte[][] tmpbytes = new byte[][]{null, new byte[]{0,0}};
        byte[] tmpbyte = new byte[]{0,0,0,0};
        Element tmpe;
        for (int i = 0; i < row; i ++){
            for (int l = 0; l < 3; l ++){
                tmpbytes[0] = policy.getMap()[i].getBytes();
                tmpbytes[1][0] = (byte) (l + 1);
                cs[i][l] = g1.newOneElement();
                for(int z = 0; z < 2; z++){
                    tmpbytes[1][1] = (byte) (z + 1);
                    cs[i][l] = cs[i][l].mul(
                            Utils.HashesE(
                                    tmpbytes, g1
                            ).powZn(ss[z])
                    );
                }

                tmpbyte[2] = (byte) (l + 1);
                for (int j = 0; j < col; j++) {
//                    tmpbyte[1] = (byte) (j + 1);
                    tmpbyte[1] = 1;
                    tmpe = g1.newOneElement();
                    for(int kk = 0; kk < 2; kk++){
                        tmpbyte[3] = (byte) (kk + 1);
                        tmpe = tmpe.mul(
                                Utils.HashE(
                                        tmpbyte, g1
                                ).powZn(ss[kk])
                        );
                    }
                    tmpe = tmpe.powZn(policy.getMatrix().getValue(i,j));
                    cs[i][l] = cs[i][l].mul(tmpe);

                }
                cs[i][l] = cs[i][l].getImmutable();
            }
        }

        Element c_p = this.mpk.getT1().powZn(ss[0]).mul(
                this.mpk.getT2().powZn(ss[1])
        ).mul(m).getImmutable();//add s_1,s_2




    }
    public Element decrypt(RABEDK dk, RABECipher cipher){

        Field g1 = this.mpk.getPairing().getG1();
        Field gt = this.mpk.getPairing().getGT();
        Pairing pairing = this.mpk.getPairing();

        int[][] indexes = Utils.search(
                cipher.getPolicy().getMap(),
                dk.getAttrs()
        );
        int[] ci = indexes[0];
        int[] ui = indexes[1];
        Vector lambda = cipher.getPolicy().extract(ci).genLambda();

        Element[] tmp1 = new Element[3];
        Element[] tmp2 = new Element[3];
        for (int i = 0; i < 3; i++) {
            tmp1[i] = g1.newOneElement();
            tmp2[i] = dk.getSk_p()[i];
        }
        for (int i = 0; i < ci.length; i++) {
            for (int j = 0; j < 3; j++) {
                tmp1[j] = tmp1[j].mul(
                        cipher.getCs()[ci[i]][j].powZn(lambda.getValue(i))
                );
                tmp2[j] = tmp2[j].mul(
                        dk.getSk_y()[ui[i]][j].powZn(lambda.getValue(i))
                );
            }
        }

        Element num = cipher.getC_p();
        Element den = gt.newOneElement();
        for (int j = 0; j < 3; j++) {
            num = num.mul(
                    pairing.pairing(tmp1[j], dk.getSk0()[j])
            );
            den = den.mul(
                    pairing.pairing(tmp2[j], cipher.getC0()[j])
            );
        }

        num = num.mul(
                pairing.pairing(cipher.getC0()[3], dk.getSk_p()[3])//ct==c0
        );

        Element rm = num.mul(den.invert());

        return rm;
    }






}
