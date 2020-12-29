package com.jsonex.snapshottest;

public interface SnapshotSerializer<TOption> {
  TOption getOption();
  String serialize(Object obj);
  String getFileExtension(Object ob);
}
