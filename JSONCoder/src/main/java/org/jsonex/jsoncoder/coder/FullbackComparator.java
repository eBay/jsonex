package org.jsonex.jsoncoder.coder;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;

/** A comparator that will full back to compare toString() value if the object is not comparable */
@Slf4j
class FullbackComparator implements Comparator<Object> {
  final static FullbackComparator it = new FullbackComparator();

  @Override
  public int compare(Object o1, Object o2) {
    if (o1 == null && o2 == null)
      return 0;
    if (o1 == null)
      return -1;
    if (o2 == null)
      return 1;

    try {
      if (o1 instanceof Comparable) {
        return ((Comparable<Object>)o1).compareTo(o2);
      }
      if (o2 instanceof Comparable) {
        return -((Comparable<Object>)o2).compareTo(o1);
      }
    } catch (Exception e) {  // Could be Class Cast exception depends on the implementation of actual Comparable.
      log.error("Error compare objects: o1.clss=" + o1.getClass() + ";o2.class=" + o2.getClass(), e);
    }

    // Fall back to string comparison.
    return o1.toString().compareTo(o2.toString());
  }
}
