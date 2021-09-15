package org.jsonex.snapshottest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsonex.core.util.ClassUtil;

import java.lang.reflect.Method;

import static org.jsonex.core.util.ListUtil.exists;
import static org.jsonex.core.util.ListUtil.listOf;

@Slf4j
public class SnapshotUtil {
  static StackTraceElement findCallerTestMethod() {
    StackTraceElement result = ClassUtil.findCallerStackTrace(SnapshotUtil.class, SnapshotUtil::isTestMethod);
    return result != ClassUtil.UNKNOWN_STACK_TRACE_ELEMENT ? result : ClassUtil.findCallerStackTrace(Snapshot.class);
  }

  @SneakyThrows
  static boolean isTestMethod(StackTraceElement s) {
    try {
      Class<?> cls = Class.forName(s.getClassName());
      return exists(listOf(ClassUtil.getAllMethods(cls)), m -> m.getName().equals(s.getMethodName()) && isTestMethod(m));
    } catch (ClassNotFoundException e) {
      log.warn("Class not found: "  + e.getMessage());
      return  false;
    }
  }

  static boolean isTestMethod(Method method) {
    return exists(listOf(method.getDeclaredAnnotations()), a ->
        a.annotationType().getSimpleName().equalsIgnoreCase("Test"));
  }
}
