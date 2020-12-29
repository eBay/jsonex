package com.jsonex.snapshottest;

import com.jsonex.core.util.LangUtil;
import com.jsonex.jsoncoder.JSONCoderOption;
import lombok.Data;

@Data
public class SnapshotOption {
  private final static String TEST_RESOURCE_ROOT = "src/test/resources";

  private String testResourceRoot = TEST_RESOURCE_ROOT;
  private SnapshotSerializer serializer = new SnapshotSerializerJsonCoder();

  /** This method only availabe if the serializer is SnapshotSerializerJsonCoder */
  private JSONCoderOption getJsonCoderOption() {
    return LangUtil.getIfInstanceOfElseThrow(serializer, SnapshotSerializerJsonCoder.class, s -> s.getOption());
  }
}
