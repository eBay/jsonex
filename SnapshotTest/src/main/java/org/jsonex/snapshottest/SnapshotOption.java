package org.jsonex.snapshottest;

import lombok.Data;
import org.jsonex.core.factory.InjectableFactory;
import org.jsonex.jsoncoder.JSONCoderOption;

import java.beans.Transient;
import java.util.function.Function;

import static org.jsonex.core.util.LangUtil.*;

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
    return getIfInstanceOfOrElseThrow(serializer, SnapshotSerializerJsonCoder.class, s -> s.getOption());
  }

  public SnapshotOption mutateJsonCoderOption(Function<JSONCoderOption, JSONCoderOption> mutator) {
    doIfInstanceOfOrElseThrow(serializer, SnapshotSerializerJsonCoder.class,
        s -> s.setOption(mutator.apply(getJsonCoderOption())));
    return this;
  }
}
