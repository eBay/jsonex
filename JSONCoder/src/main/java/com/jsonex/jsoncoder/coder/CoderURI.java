package com.jsonex.jsoncoder.coder;

import com.jsonex.core.factory.InjectableInstance;
import com.jsonex.jsoncoder.BeanCoderContext;
import com.jsonex.jsoncoder.ICoder;
import com.jsonex.treedoc.TDNode;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.net.URI;

public class CoderURI implements ICoder<URI> {
  public static final InjectableInstance<CoderURI> it = InjectableInstance.of(CoderURI.class);
  public static CoderURI get() { return it.get(); }

  public Class<URI> getType() { return URI.class; }

  public TDNode encode(URI obj, Type type, BeanCoderContext ctx, TDNode target) {
    return target.setValue(obj.toString());
  }

  @SneakyThrows
  public URI decode(TDNode jsonNode, Type type, Object targetObj, BeanCoderContext ctx) {
    return new URI(jsonNode.getValue() + "");
  }
}
