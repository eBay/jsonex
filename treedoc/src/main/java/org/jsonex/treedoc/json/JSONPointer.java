package org.jsonex.treedoc.json;

import org.jsonex.core.factory.InjectableInstance;
import org.jsonex.core.util.StringUtil;
import org.jsonex.treedoc.TDNode;
import org.jsonex.treedoc.TDPath;

import static java.lang.Integer.parseInt;

/**
 * <pre>
 * Implementation of JSONPointer with following specs and extensions
 *
 * 1. swagger spec of ref: https://swagger.io/docs/specification/using-ref/
 * 2. relative json pointer: https://json-schema.org/draft/2019-09/relative-json-pointer.html
 * 3. json-schema using id with ref: https://json-schema.org/understanding-json-schema/structuring.html#using-id-with-ref
 *
 * Different from specs:
 * 1. Doesn't support tailing # to indicate the key. Which is not necessary
 * 2. Support ".." as parent, "." as current relative node
 *
 * Examples:
 * 1. URL + Anchor:  http://a.com/path#/p1/p2
 * 2. URL only:  http://a.com
 * 3. Anchor only:  #/p1/p2
 * 4. Relative with number: 2/p1/p2
 * 5. Relative with parent: ../p1/p2
 * 6. Anchor with $id reference:  [http://a.com/path]#nodeId
 * </pre>
 */
public class JSONPointer {
  public final static InjectableInstance<JSONPointer> it = InjectableInstance.of(JSONPointer.class);
  public static JSONPointer get() { return it.get(); }

  public TDPath parse(String str) {
    TDPath path = new TDPath();
    if (StringUtil.isEmpty(str))
      return path;

    if (str.endsWith("#")) // Ignore the last # which indicate "key" of the map
      str = str.substring(0, str.length() - 1);

    if (str.indexOf('#') < 0) {
      if (parseParts(str, path, true))
        return path;
      path.setDocPath(str);
      path.addParts(TDPath.Part.ofRoot());
    } else {
      String[] strs = str.split("#");
      if (!strs[0].isEmpty())
        path.setDocPath(strs[0]);
      parseParts(strs[1], path,false);
    }

    return path;
  }

  boolean parseParts(String str, TDPath path, boolean relativeWithNum) {
    String[] parts = str.split("/");
    if (relativeWithNum) {
      try {
        path.addParts(TDPath.Part.ofRelative(parseInt(parts[0])));
      } catch(NumberFormatException e) {
        return false;
      }
    } else {
      if (parts[0].isEmpty()) {
        path.addParts(TDPath.Part.ofRoot());
      } else if (".".equals(parts[0])) {
        path.addParts(TDPath.Part.ofRelative(0));
      } else if ("..".equals(parts[0])) {
        path.addParts(TDPath.Part.ofRelative(1));
      } else {
        path.addParts(TDPath.Part.ofChildOrId(parts[0], parts[0]));
      }
    }

    for (int i = 1; i < parts.length; i++) {
      path.addParts(parsePart(parts[i]));
    }
    return true;
  }

  TDPath.Part parsePart(String str) {
    str = str.replace("~1", "/").replace("~0", "~");
    return TDPath.Part.ofChild(str);
  }

  public TDNode query(TDNode node, String path) {
    return node.getByPath(parse(path));
  }
}
