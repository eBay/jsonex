package com.jsonex.snapshottest;

public interface SnapshotSerializer<TOption, T extends SnapshotSerializer> {
  TOption getOption();
  T setOption(TOption opt);
  String serialize(Object obj);
  String getFileExtension(Object ob);
}
