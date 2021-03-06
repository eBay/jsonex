package org.jsonex.jsoncoder.coder;

import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.jsoncoder.BeanCoderContext;
import org.jsonex.jsoncoder.ICoder;
import org.jsonex.treedoc.TDNode;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.net.URL;

public class CoderURL implements ICoder<URL> {
  public static final InjectableInstance<CoderURL> it = InjectableInstance.of(CoderURL.class);
  public static CoderURL get() { return it.get(); }

  public Class<URL> getType() { return URL.class; }

  public TDNode encode(URL obj, Type type, BeanCoderContext ctx, TDNode target) {
    return target.setValue(obj.toString());
  }

  @SneakyThrows
  public URL decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext ctx) {
    return new URL(jsonNode.getValue() + "");
  }
}
