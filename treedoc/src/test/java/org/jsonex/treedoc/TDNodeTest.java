package org.jsonex.treedoc;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.List;

public class TDNodeTest {
  @Test
  public void testCreateLastNumberOfChildren() {
    TDNode node = new TreeDoc().getRoot().setType(TDNode.Type.ARRAY);
    long start = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      node.createChild("name_" + i).setType(TDNode.Type.MAP).createChild("name_" + i + "_1").setValue("value_" + i + "_1");
    }
    List<String> keys = node.getChildrenKeys();
    long time = System.currentTimeMillis() - start;
    System.out.println(time);
    assertTrue(time < 2000);
  }
}
