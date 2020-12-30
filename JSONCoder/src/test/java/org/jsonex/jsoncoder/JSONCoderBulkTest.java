package org.jsonex.jsoncoder;

import org.jsonex.core.charsource.CharSource;
import org.jsonex.core.charsource.ReaderCharSource;
import org.jsonex.core.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.Reader;

@Slf4j
public class JSONCoderBulkTest {
  @Rule final public TestName name = new TestName();
  public void log(String msg) { log.info(this.getClass().getSimpleName() + "." + name.getMethodName() + ":" + msg); }

  public static class Address {
    public String city;
    public String country;
  }

  public static class User {
    public String firstName;
    public String lastName;
    public Address address;
  }

  @Test public void testBulkDecode() {
    Reader in = FileUtil.loadResource(this.getClass(), "bulk.json");
    CharSource source = new ReaderCharSource(in);
    String[] expectedFirstNames = {"first1", "first2", "first3"};
    for (int i = 0; !source.isEof(); i++) {
      User user = JSONCoder.global.decode(source, User.class);
      log(JSONCoder.global.encode(user));
      Assert.assertEquals(expectedFirstNames[i], user.firstName);
    }
  }
}
