import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.apache.commons.lang3.time.StopWatch;

import java.nio.charset.StandardCharsets;
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
    
    
    static final int STRING_LENGTH = 512;
    static final int RUN_TIMES = 100000;
    
    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < STRING_LENGTH; i++)
        {
            stringBuilder.append("a");
        }

        String msg = stringBuilder.toString();
        
        byte[] arr = encode("server_1");
        Pointer tag = new Memory(arr.length);
        tag.write(0, arr, 0, arr.length);

        Syslog syslog = (Syslog) Native.loadLibrary("c", Syslog.class);

        StopWatch sw = new StopWatch();
        sw.start();
        syslog.openlog(tag, Option.LOG_NDELAY, Facility.LOG_LOCAL0);
        for (int i = 0; i < RUN_TIMES; i++) {
            syslog.syslog(Priority.LOG_INFO, msg);
        }
        
        sw.stop();
        System.out.println(STRING_LENGTH + ":" + sw.getTime(TimeUnit.MILLISECONDS));
    }
    
    private static byte[] encode(String str) {
        byte[] src = str.getBytes(StandardCharsets.UTF_8);
        byte[] dest = new byte[src.length + 1];
        System.arraycopy(src, 0, dest, 0, src.length);
        dest[dest.length - 1] = 0;

        return dest;
    }
}
