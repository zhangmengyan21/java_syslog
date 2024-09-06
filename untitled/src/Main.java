import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.apache.commons.lang3.time.StopWatch;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {
    static class Facility {
        static final int LOG_LOCAL0 = 16 << 3;
    }

    static class Option {
        static final int LOG_NDELAY = 0x08; /* don't delay open */
    }

    static class Priority {
        static final int LOG_INFO = 6; /* informational */
    }

    interface Syslog extends Library {
        void openlog(Pointer iIdent, int iLogopt, int iFacility);

        void syslog(int iPriority, String strMessage);

        void closelog();
    }
    
    interface Fcntl extends Library {
        int open(String path, int flags);
        
        int write(int fd, byte[] buffer, int count);
        
        int close(int fd);
        
    }
    
    static final int STRING_LENGTH = 512;
    static final int RUN_TIMES = 100000;
    
    public static void main(String[] args) {
        //TestOther();
        
        Random r = new Random(12345);
        List<String> lstMsg = new ArrayList<>();
        for (int i = 0;i < RUN_TIMES; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < STRING_LENGTH; j++)
            {
                String str = r.nextInt(10) + "";
                stringBuilder.append(str);
            }

            String message = stringBuilder.toString();
            lstMsg.add(message);
        }

        Map<Integer, Pointer> mp = new HashMap<>();
        mp.put(1, getPoint(1));
        mp.put(2, getPoint(2));
        mp.put(3, getPoint(3));
        mp.put(4, getPoint(4));
        mp.put(5, getPoint(5));
        mp.put(6, getPoint(6));
        mp.put(7, getPoint(7));
        mp.put(8, getPoint(8));
        mp.put(9, getPoint(9));
        mp.put(10, getPoint(10));

        ArrayList<Integer> lst = new ArrayList<>();
        for (int i = 0; i < RUN_TIMES; i++) {
            lst.add(1 + r.nextInt(10));
        }

        Syslog syslog = (Syslog) Native.loadLibrary("c", Syslog.class);

        int iServer = -1;
        StopWatch sw = new StopWatch();
        sw.start();
        syslog.openlog(mp.get(1), Option.LOG_NDELAY, Facility.LOG_LOCAL0);
        //long time1 = System.currentTimeMillis();
        for (int i = 0; i < RUN_TIMES; i++) {
//            if (iServer != tmpServer) {
//                iServer = tmpServer;
//            }
            syslog.syslog(Priority.LOG_INFO, lstMsg.get(i));
            
        }
        //long time2 = System.currentTimeMillis();
        
        sw.stop();
        System.out.println(STRING_LENGTH + ":" + sw.getTime(TimeUnit.MILLISECONDS));
        syslog.closelog();
        //System.out.println(time2 - time1);
    }
    
    private static Pointer getPoint(int iServerId) {
        byte[] arr = encode("server_" + iServerId);
        Pointer pSysTag = new Memory(arr.length);
        pSysTag.write(0, arr, 0, arr.length);
        return pSysTag;
    }

    private static byte[] encode(String message) {
        byte[] src = message.getBytes(StandardCharsets.UTF_8);
        byte[] dest = new byte[src.length + 1];
        System.arraycopy(src, 0, dest, 0, src.length);
        dest[dest.length - 1] = 0;

        return dest;
    }
    
    private static void TestOther() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[zmy]");
        
        for (int i = 0;i < 2048; i++) {
            stringBuilder.append("a");
        }
        
        stringBuilder.append("\n");

        String message = stringBuilder.toString();
        
        Fcntl fcntl = (Fcntl) Native.loadLibrary("c", Fcntl.class);
        byte[] arr = message.getBytes();
        long time1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            int fd = fcntl.open("/var/log/syslog",2 | 1024);
            fcntl.write(fd,arr,arr.length);
            fcntl.close(fd);
        }
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);
    }
}
