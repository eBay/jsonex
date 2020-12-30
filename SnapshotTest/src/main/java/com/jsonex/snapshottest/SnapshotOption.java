package com.jsonex.snapshottest;

import com.jsonex.core.factory.InjectableFactory;
import com.jsonex.jsoncoder.JSONCoderOption;
import lombok.Data;

import java.beans.Transient;
import java.util.function.Function;

import static com.jsonex.core.util.LangUtil.doIfInstanceOf;
import static com.jsonex.core.util.LangUtil.getIfInstanceOfElseThrow;

@Data
public class SnapshotOption {
  private static InjectableFactory._0<SnapshotOption> factory = InjectableFactory._0.of(SnapshotOption::new);

  private final static String TEST_RESOURCE_ROOT = "src/test/resources";

  public static SnapshotOption of() { return factory.get(); }

  private String testResourceRoot = TEST_RESOURCE_ROOT;
  private SnapshotSerializer serializer = new SnapshotSerializerJsonCoder();

  /** This method is only available if the serializer is SnapshotSerializerJsonCoder */
  @Transient
  public JSONCoderOption getJsonCoderOption() {
    return getIfInstanceOfElseThrow(serializer, SnapshotSerializerJsonCoder.class, s -> s.getOption());
  }

  public SnapshotOption mutateJsonCoderOption(Function<JSONCoderOption, JSONCoderOption> mutator) {
    doIfInstanceOf(serializer, SnapshotSerializerJsonCoder.class, s -> s.setOption(mutator.apply(getJsonCoderOption())));
    return this;
  }
}
