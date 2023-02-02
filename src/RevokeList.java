//import smu.smc.dcph.Engine;

import java.util.ArrayList;
import java.util.List;

public class RevokeList {
    private List<Entry> list;

    public class Entry{
        private Integer uid;
        private long time;

        public Entry(Integer uid, long time) {
            this.uid = uid;
            this.time = time;
        }

        public Integer getUid() {
            return uid;
        }

        public long getTime() {
            return time;
        }
    }

    public RevokeList(){
        this.list = new ArrayList<>();
    }

    public void add(int uid, long t){
        this.list.add(new Entry(uid, t));
    }

    public Entry get(int index){
        return this.list.get(index);
    }

    public int getUid(int index){
        return this.list.get(index).getUid();
    }

    public long getTime(int index){
        return this.list.get(index).getTime();
    }

    public int getSize(){
        return this.list.size();
    }

    public boolean contains(int uid){
        for (int i = 0; i < this.list.size(); i++) {
            if(this.getUid(i) == uid)
                return true;
        }
        return false;
    }


}
