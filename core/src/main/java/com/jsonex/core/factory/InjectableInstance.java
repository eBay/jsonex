/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.jsonex.core.factory;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

/**
 * An injectable singleton. Mainly used for interface. It also supports lazy initialization
 * of default implementation that could prevent initialization cause failure in unit test
 *
 * @param <TI>  The interface for the type
 */
@Accessors(chain = true)
public class InjectableInstance<TI> {
  private volatile Class<? extends TI> implClass;
  private volatile Supplier<? extends TI> objectCreator;
  @Setter private TI instance;
  private final Object initialCreator;

  private InjectableInstance(Object creator) {
    this.initialCreator = creator;
    setCreator(creator);
  }

  private InjectableInstance<TI> setCreator(Object creator) {
    this.implClass = null;
    this.objectCreator = null;
    instance = null;
    if (creator instanceof Class<?>)
      implClass = (Class<? extends TI>)creator;
    else
      objectCreator = (Supplier<TI>)creator;
    return this;
  }
    
  public static <TI, TC extends TI> InjectableInstance<TI> of(Class<TC> implCls) {
    if (implCls.isInterface() || Modifier.isAbstract(implCls.getModifiers()))
      throw new IllegalArgumentException("Implementation class has to be a concrete class");
    return new InjectableInstance<TI>(implCls);
  }
  
  public static <TI, TC extends TI> InjectableInstance<TI> of(Supplier<TI> objectCreator) {
    return new InjectableInstance<TI>(objectCreator);
  }
  
  public InjectableInstance<TI> setImplClass(Class<? extends TI> implClass) { return setCreator(implClass); }
  public InjectableInstance<TI> setObjectCreator(Supplier<? extends TI> objectCreator) { return setCreator(objectCreator); }
  public InjectableInstance<TI> reset() { return setCreator(initialCreator); }


  public TI get() {
    if (instance == null)
      createInstance();
    return instance;
  }
  
  @SuppressWarnings("unchecked")
  @SneakyThrows
  private synchronized void createInstance() {//NOPMD
    if (instance == null)  // Double null check
      instance = objectCreator != null ? objectCreator.get() : (TI)newInstance(implClass);
  }

  private static Object newInstance(Class<?> cls) throws Exception {
    Constructor<?> ctor = cls.getDeclaredConstructor();
    ctor.setAccessible(true);
    return ctor.newInstance();
  }
}
