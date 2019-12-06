package com.jsonex.treedoc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true) @RequiredArgsConstructor @Setter @Getter
public class TreeDoc {
  final Map<String, TDNode> idMap = new HashMap<>();
  final URI uri;
  final TDNode root = new TDNode(this);
  public TreeDoc() { this (null); }
}
