/*************************************************************
 Copyright 2018-2019 eBay Inc.
 Author/Developer: Jianwu Chen

 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
 ************************************************************/

package com.ebay.jsoncodercore.factory;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * An injectable singleton. Mainly used for interface. It also supports lazy initialization
 * of default implementation that could prevent initialization cause failure in unit test
 * 
 * @author jianchen
 *
 * @param <TI>  The interface for the type
 */
@Accessors(chain = true)
public class InjectableInstance<TI> {
  Class<?> implClass;
  Supplier<TI> objectCreator;
  // @VisibleForTesting
  @Setter private TI instance;
    
  public static <TI, TC extends TI> InjectableInstance<TI> of(Class<TC> implCls) {
    if (implCls.isInterface() || Modifier.isAbstract(implCls.getModifiers()))
      throw new IllegalArgumentException("Implementation class has to be concrete class");
    return new InjectableInstance<TI>().setImplClass(implCls);
  }
  
  public static <TI, TC extends TI> InjectableInstance<TI> of(Supplier<TI> objectCreator) {
    return new InjectableInstance<TI>().setObjectCreator(objectCreator);
  }
  
  // @VisibleForTesting 
  public <TC extends TI> InjectableInstance<TI> setImplClass(Class<TC> implClass) {
    this.implClass = implClass;
    this.objectCreator = null;
    instance = null;
    return this;
  }

  // @VisibleForTesting
  public <TC extends TI> InjectableInstance<TI> setObjectCreator(Supplier<TI> objectCreator) {
    this.implClass = null;
    this.objectCreator = objectCreator;
    instance = null;
    return this;
  }

  public TI get() {
    if (instance == null)  // Do lazy load here as DAO class constructor do heavy DAL initialization which is not necessary of UT
      createDefaultInstance();
    return instance;
  }
  
  @SuppressWarnings("unchecked")
  private synchronized void createDefaultInstance() {//NOPMD
    try {
      instance = objectCreator != null ? objectCreator.get() : (TI)newInstance(implClass);
    } catch (Exception e) {
      throw new RuntimeException("Error creating instance", e);
    }
  }
  private static Object newInstance(Class<?> cls) throws Exception {
    Constructor<?> ctor = cls.getDeclaredConstructor();
    ctor.setAccessible(true);
    return ctor.newInstance();
  }
}
