package org.jsonex.jsoncoder.fieldTransformer;

import org.jsonex.core.factory.InjectableInstance;

import java.util.function.Function;

import static java.lang.String.format;

public interface MaskStrategy extends Function<Object, String> {
  InjectableInstance<MaskStrategy> defaultImpl = InjectableInstance.of(DefaultImpl::new);
  static MaskStrategy ofDefault() { return defaultImpl.get(); }

  public class DefaultImpl implements MaskStrategy {
    @Override
    public String apply(Object o) {
      if (o == null)
        return null;
      String str = String.valueOf(o);
      return str.isEmpty() ? "" : format("[Masked:len=%d,%x]", str.length(), str.hashCode());
    }
  }
}
