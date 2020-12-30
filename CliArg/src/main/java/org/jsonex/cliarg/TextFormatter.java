package org.jsonex.cliarg;


import java.util.ArrayList;
import java.util.List;

import static org.jsonex.core.util.ListUtil.mutateAt;
import static org.jsonex.core.util.StringUtil.appendRepeatedly;
import static java.lang.Math.max;

public class TextFormatter {
  public static String indent(String text, String indent) {
    return indent + text.replace("\n", "\n" + indent);
  }

  public static String  alignTabs(String text) {
    if (text == null)
      return text;
    String[] lines = text.split("\n");
    String[][] fields = new String[lines.length][];
    List<Integer> columnsSize = new ArrayList<>();

    for (int i = 0; i < lines.length; i++) {
      lines[i] = lines[i].trim();
      fields[i] = lines[i].split("\t");
      for (int c = 0; c < fields[i].length; c++) {
        int len = fields[i][c].length();
        mutateAt(columnsSize, c, 0, size -> max(size, len));
      }
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lines.length; i++) {
      if (!lines[i].isEmpty()) {
        String[] line = fields[i];
        for (int c = 0; c < line.length; c++) {
          appendRepeatedly(sb.append(line[c]), ' ', columnsSize.get(c) + 1 - line[c].length());
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
