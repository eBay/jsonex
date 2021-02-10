package org.jsonex.jsoncoder.fieldTransformer;

import org.jsonex.core.factory.InjectableInstance;

import java.util.function.Function;

public interface MaskStrategy extends Function<Object, String> {
  InjectableInstance<MaskStrategy> defaultImpl = InjectableInstance.of(DefaultImpl::new);
  static MaskStrategy ofDefault() { return defaultImpl.get(); }

  public class DefaultImpl implements MaskStrategy {
    @Override
    public String apply(Object o) {
      if (o == null)
        return null;
      return "[masked:hash=" + o.hashCode() + ",len=" + o.toString().length() +"]";
    }
  }
}
