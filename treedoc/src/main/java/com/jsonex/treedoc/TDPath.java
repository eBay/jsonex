package com.jsonex.treedoc;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter @Setter @Accessors(chain = true) @EqualsAndHashCode @ToString
public class TDPath {
  public enum PathPartType { ROOT, CHILD, RELATIVE, ID }

  @Getter @Setter @Accessors(chain = true) @RequiredArgsConstructor @EqualsAndHashCode @ToString
  public static class Part {
    final PathPartType type;
    String key;  // Only for Type.CHILD or TYPE.ID
    int level;  // Only for RELATIVE
    public static Part ofId(String id) { return new Part(PathPartType.ID).setKey(id); }
    public static Part ofChild(String key) { return new Part(PathPartType.CHILD).setKey(key); }
    public static Part ofRelative(int level) { return new Part(PathPartType.RELATIVE).setLevel(level); }
    public static Part ofRoot() { return new Part(PathPartType.ROOT); }
  }

  /** The TreeDoc file path or URL, it could absolution or relative */
  String docPath;
  /** The path parts */
  final List<Part> parts = new ArrayList<>();
  public TDPath addParts(Part... part) { parts.addAll(Arrays.asList(part)); return this; }

  public static TDPath parse(String str) {
    return parse(str.split("/"));
  }

  public static TDPath parse(String[] strs) {
    if (strs.length == 0 || strs.length == 1 && strs[0].isEmpty())
      return new TDPath().addParts(Part.ofRelative(0));

    TDPath path = new TDPath();
    for (String s : strs) {
      if (".".equals(s))
        path.addParts(Part.ofRelative(0));
      else if ("..".equals(s))
        path.addParts(Part.ofRelative(1));
      else if (s.isEmpty())
        path.addParts(Part.ofRoot());
      else
        path.addParts(Part.ofChild(s));
    }
    return path;
  }
}
