import models.Activity;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class RegexTest {
    @Test
    public void testActivityTitle() {
        Pattern pattern = Activity.TITLE_PATTERN;
        String s1 = "abcde";
        Assert.assertTrue(pattern.matcher(s1).matches());
        String s2 = "12345";
        Assert.assertTrue(pattern.matcher(s2).matches());
        String s3 = "我是中文";
        Assert.assertFalse(pattern.matcher(s3).matches());
        String s4 = "我是中文字";
        Assert.assertTrue(pattern.matcher(s4).matches());
        String s5 = "我是中文3";
        Assert.assertTrue(pattern.matcher(s5).matches());
        String s6 = "this'";
        Assert.assertTrue(pattern.matcher(s6).matches());
        String s7 = "thi蛤";
        Assert.assertFalse(pattern.matcher(s7).matches());
        String s8 = "thi蛤1";
        Assert.assertTrue(pattern.matcher(s8).matches());
        String s9 = "I是中3";
        Assert.assertFalse(pattern.matcher(s9).matches());
    }

    @Test
    public void testActivityAddress() {

    }
}
