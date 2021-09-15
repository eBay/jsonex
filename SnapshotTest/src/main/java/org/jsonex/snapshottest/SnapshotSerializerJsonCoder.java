package org.jsonex.snapshottest;

import org.jsonex.jsoncoder.JSONCoder;
import org.jsonex.jsoncoder.JSONCoderOption;
import lombok.Getter;
import lombok.Setter;

public class SnapshotSerializerJsonCoder implements SnapshotSerializer<JSONCoderOption, SnapshotSerializerJsonCoder> {
  @Getter @Setter private transient JSONCoderOption option = JSONCoderOption.ofIndentFactor(2).setStrictOrder(true);

  @Override
  public String serialize(Object obj) {
    return JSONCoder.encode(obj, option);
  }

  @Override
  public String getFileExtension(Object ob) {
    return ".json";
  }
}
