package org.jsonex.treedoc.json;

import org.jsonex.treedoc.TDNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.jsonex.core.util.ListUtil.exists;

/** if it returns null, node will be skipped */
public interface NodeFilter extends Function<TDNode, TDNode> {
  abstract class RegexFilter implements NodeFilter {
    public final List<Pattern> pathPatterns = new ArrayList<>();
    abstract TDNode transform(TDNode n);
    public RegexFilter(String... strPatterns) { addPatterns(strPatterns); }
    public TDNode apply(TDNode n) {
      if (!matches(n.getPathAsString()))
        return n;
      return transform(n);
    }
    void addPatterns(String... patterns) {
      if (patterns != null)
        for (String ptn : patterns)
          pathPatterns.add(Pattern.compile(ptn));
    }

    boolean matches(String path) {
      return exists(pathPatterns, p -> p.matcher(path).matches());
    }
  }

  class ExcludeFilter extends RegexFilter {
    public ExcludeFilter(String... patterns) { super(patterns); }
    TDNode transform(TDNode n) { return null; }
  }

  class MaskFilter extends RegexFilter {
    MaskFilter(String... patterns) {super(patterns);}
    TDNode transform(TDNode n) {
      if (n.getValue() == null && !n.hasChildren())
        return n;
      return n.cloneOfSimpleType(getMaskStr(n));
    }
    private String getMaskStr(TDNode n) {
      switch (n.getType()) {
        case SIMPLE:
          String str = Objects.toString(n.getValue());
          return str.isEmpty() ? str : format("[Masked:len=%d,%x]", str.length(), str.hashCode());
        case MAP: return "{Masked:len=" + n.getChildrenSize() + "}";
        case ARRAY: return "[Masked:len=" + n.getChildrenSize() + "]";
      }
      return "[Masked]";  // Shouldn't happen
    }
  }

  static ExcludeFilter exclude(String... patterns) { return new ExcludeFilter(patterns); }

  static MaskFilter mask(String... patterns) { return new MaskFilter(patterns); }
}
