package org.jsonex.snapshottest;

import lombok.SneakyThrows;
import org.jsonex.core.util.ClassUtil;

import java.lang.reflect.Method;

import static org.jsonex.core.util.ListUtil.exists;
import static org.jsonex.core.util.ListUtil.listOf;

public class SnapshotUtil {
  static StackTraceElement findCallerTestMethod() {
    StackTraceElement result = ClassUtil.findCallerStackTrace(SnapshotUtil.class, SnapshotUtil::isTestMethod);
    return result != null ? result : ClassUtil.findCallerStackTrace(SnapshotUtil.class);
  }

  @SneakyThrows
  static boolean isTestMethod(StackTraceElement s) {
    Class<?> cls = Class.forName(s.getClassName());
    return exists(listOf(cls.getMethods()), m -> m.getName().equals(s.getMethodName()) && isTestMethod(m));
  }

  static boolean isTestMethod(Method method) {
    return exists(listOf(method.getDeclaredAnnotations()), a ->
        a.annotationType().getSimpleName().equalsIgnoreCase("Test"));
  }
}
