package org.jsonex.core.util;

import lombok.Getter;
import lombok.SneakyThrows;
// import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.*;

/**
 * Method wrapper to wrapper constructors and methods, so client will have consistent interface when invoke constructors or methods.
 * It also collects the method parameter names. 
 */
public class MethodWrapper {
  public final static String METHOD_INIT = "<init>";

  // private static final LocalVariableTableParameterNameDiscoverer pnd = new LocalVariableTableParameterNameDiscoverer();
  @Getter final Method method;
  @Getter final Constructor<?> constructor;
  @Getter final Class<?> declaringClass;
  @Getter final String name;
  @Getter final Class<?> returnClass;
  @Getter final Type[] paramTypes;
  @Getter final Class<?>[] paramClasses;
  @Getter final String[] paramNames;
  @Getter final boolean isStatic;
  
  public MethodWrapper(Method _method){
    method = _method;
    method.setAccessible(true);
    constructor = null;
    declaringClass = method.getDeclaringClass();
    name = method.getName();
    returnClass = method.getReturnType();
    paramTypes = method.getGenericParameterTypes();
    paramClasses = method.getParameterTypes();
    paramNames = getParameterNames(method.getParameters()); // pnd.getParameterNames(method);
    isStatic = (method.getModifiers() & Modifier.STATIC) != 0;
  }
  
  public MethodWrapper(Constructor<?> _constructor) {
    constructor = _constructor;
    constructor.setAccessible(true);
    method = null;
    name = "<init>";    
    declaringClass = constructor.getDeclaringClass();
    returnClass = null;
    paramTypes = constructor.getGenericParameterTypes();
    paramClasses = constructor.getParameterTypes();
    paramNames = getParameterNames(constructor.getParameters()); // pnd.getParameterNames(constructor);
    isStatic = true; 
  }

  private String[] getParameterNames(Parameter[] parameters) {
    return ArrayUtil.map(parameters, Parameter::getName, new String[0]);
  }
  
  public String getSignature(boolean includeClassName) {
    StringBuilder sb = new StringBuilder();
    if (returnClass != null)
      sb.append(returnClass.getSimpleName() + " ");
    if (includeClassName) 
      sb.append(declaringClass.getName() + "/");
    sb.append(name + "(");
    for (int i=0; i<paramClasses.length; i++){
      if (i > 0) 
        sb.append(", ");
      String paramName = paramNames != null ? paramNames[i] : ("arg" + i);
      sb.append(paramClasses[i].getSimpleName() + " " + paramName);
    }
    sb.append(")");
    return sb.toString();
  }

  public String toString() {
    return getSignature(true);
  }
  
  public String getCanonicalName() {
    return declaringClass.getCanonicalName() + "/" + name;    
  }

  @SneakyThrows
  public Object invoke(Object obj, Object[] args) {
    if (method != null)
      return method.invoke(obj, args);
    else
      return constructor.newInstance(args);
  }
}

