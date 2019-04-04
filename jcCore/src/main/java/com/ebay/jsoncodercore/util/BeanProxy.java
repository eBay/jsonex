/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple factory to create a proxy for interfaces of data object. So that can simulate setter/getter method with
 * a Map as internal storage 
 * <p> It supports following setter methods: void setXXX(val), void setXXX(flag, val), and also chained style setters.
 * <p> It supports following getter methods: Object getXXX(), Object isXXX(), Object hasXXX(), those getter methods could also 
 * optionally with one argument as sub-attribute.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public class BeanProxy {
  @Data
  private static class Node {
    Object val;
    Map<Object, Node> children;
    
    public Node() {}
    
    public Node getOrCreate(Object key) {
      if (children == null)
        children = new HashMap<>();
      Node result = children.get(key);
      if (result == null) {
        result = new Node();
        children.put(key, result);
      }
      return result;
    }
    
    public Node getOrCreate(int pathIdx, Object... path) {
      Node child = getOrCreate(path[pathIdx]);
      return pathIdx == path.length -1 ? child : child.getOrCreate(pathIdx + 1, path);
    }
  }
  
  public static class BeanInvocationHandler implements InvocationHandler {
    final Node root = new Node();

    private static final Method hashCodeMethod;
    private static final Method equalsMethod;
    private static final Method toStringMethod;
    private boolean isInToString; // prevent recursive call cause stack overflow

    static {
      try {
        hashCodeMethod = Object.class.getMethod("hashCode");
        equalsMethod = Object.class.getMethod("equals", Object.class);
        toStringMethod = Object.class.getMethod("toString");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public Object invoke(Object proxy, Method method, Object... args) {
      try {
        if(method.equals(hashCodeMethod))
          return root.hashCode();
        if(method.equals(equalsMethod))
          return root.equals(args[0]);
        if(method.equals(toStringMethod)){
          if(isInToString){
            return this.toString();
          }else{
            isInToString = true;
            String str = root.toString();
            isInToString = false;
            return str;
          }
        }
        
        String name = method.getName();
        
        // Check setters
        if (name.startsWith("set")) {
          String att = StringUtil.lowerFirst(name.substring(3));
          if (args.length == 1)
            root.getOrCreate(att).val = args[0];
          else if (args.length == 2) {
            root.getOrCreate(0, att, args[0]).val = args[1];
          }
          return method.getReturnType() == Void.class ? null : proxy;
        }
          
        // check getters
        String att = null;
        if (name.startsWith("get") || name.startsWith("has"))
          att = name.substring(3);
        else if (name.startsWith("is"))
          att = name.substring(2);
          
        if (att != null) {
          att = StringUtil.lowerFirst(att);
          Object val = null;
          if (args == null || args.length == 0) {
            val = root.getOrCreate(att).val;
          } else if (args.length == 1) {
            val = root.getOrCreate(0, att, args[0]).val;
          }
          return val == null ? getDefaultValue(method.getReturnType()) : val;
        }
        
        if (name.equals("toString"))//NOPMD
          return toString();
  
        log.error("Unimplemented method invoked: " + method + "; this=" + this);
      } catch (Exception e) {
        log.error("", e);
      }
      return getDefaultValue(method.getReturnType());
    }
  }
  
  /**
   * For attributes without a setter method, we can use this method to set it dynamically
   */
  public static void setAttribute(Object proxy, Object path, Object value) {
    setAttribute(proxy, new Object[] { path }, value);
  }
  
  public static void setAttribute(Object proxy, Object[] path, Object value) {
    BeanInvocationHandler handler = (BeanInvocationHandler)Proxy.getInvocationHandler(proxy);
    handler.root.getOrCreate(0, path).val = value;
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T createProxy(Class<T> cls) {
    return (T) Proxy.newProxyInstance(BeanProxy.class.getClassLoader(),  new Class[]{cls}, new BeanInvocationHandler());
  }
  
  @SuppressWarnings("unchecked")
  private static <T> T getDefaultValue(Class<T> clazz) {
    return (T) Array.get(Array.newInstance(clazz, 1), 0);
  }
}
