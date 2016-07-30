import com.lee.bsdiff.BsDiff;
import com.lee.bsdiff.BsPatch;
import com.lee.bsdiff.Logger;
import com.lee.bsdiff.Main;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * Created by jianglee on 7/30/16.
 */
public class BsDiffTest extends Assert {

    @BeforeClass
    public static void init() {
        Logger.setLevel(3);
    }

    @Test
    public void testBsDiff() {
        BsDiff bsdiff = new BsDiff("src/test/resource/old.txt", "src/test/resource/new.txt");
        String patchFileName = bsdiff.bsdiff(null);
        BsPatch bsPatch = new BsPatch("src/test/resource/old.txt", patchFileName);
        String newFileName = bsPatch.bsPatch(null);
        assertTrue(Main.check("src/test/resource/new.txt", newFileName));
        if (null != patchFileName) {
            new File(patchFileName).delete();
        }
        if (null != newFileName) {
            new File(newFileName).delete();
        }
    }

}
