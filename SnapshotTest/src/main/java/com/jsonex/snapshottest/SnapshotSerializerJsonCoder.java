package com.jsonex.snapshottest;

import com.jsonex.jsoncoder.JSONCoder;
import com.jsonex.jsoncoder.JSONCoderOption;
import lombok.Getter;

public class SnapshotSerializerJsonCoder implements SnapshotSerializer<JSONCoderOption> {
  @Getter private transient JSONCoderOption option = JSONCoderOption.ofIndentFactor(2);

  @Override
  public String serialize(Object obj) {
    return JSONCoder.encode(obj, option);
  }

  @Override
  public String getFileExtension(Object ob) {
    return ".json";
  }
}
