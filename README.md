# JSONCoder
[![Build Status](https://travis-ci.org/eBay/jsonex.svg?branch=master)](https://travis-ci.org/eBay/jsonex)
[![codecov](https://codecov.io/gh/eBay/jsonex/branch/master/graph/badge.svg)](https://codecov.io/gh/eBay/jsonex)
## Description
Jsonex JSONCoder is a light-weight generic object serialization / deserialization library similar to Jackson, GSON or FastJson. This library has been widely used in various eBay projects for years. It's not a replacement for other popular libraries. But it solves some specific problems which are not available or not well-supported in other alternatives.

## Why JSONCoder
There are plenty of options for JSON serialization/deserialization libraries, why we need another one? If you are also a fan of JSON, but hate the restrictions by JSON standards that make it so hard to be used as a configuration format, but still don't want to give up JSON to YAML (JSON is nice, why need Yet Another pretty ugly Markup Language). You can have a try of this library. This library focuses on solving following problems well:

* Supports JSON extension proposal ([JSONEX](./JSONEX.md)) which will make it friendly for configuration, such as
    * comments support
    * quote of key/value is optional
    * customize quote characters (Avoid un-necessary escapes by choosing different quote characters like Javascript) 
    * multi-line string literal as in ES6
    * Merge of config files (e.g. environment specific config override common config)
    * Optional top level "[", "{" brackets
* Supports serialization of arbitrary java objects out of box by detecting cyclic object references. You don't have to write serializer friendly classes or use any custom annotations to pollute your domain models. Whatever you have, it's supported.
* Make sensible default over configuration. **Minimize annotation** or configuration usage. Such as it silently ignores unknown properties by default for forward compatibility
* Focuses on developer friendly APIs at the same time still provides flexible configurations  

## Features
* Auto-detect cyclic references and serialize the reference to avoid stack overflow
* Options to include class fields in addition to getters
* Pluggable custom encoder and decoder 
* Pluggable custom filters to exclude or mask fields for particular types
* Options to skip certain classes
* Options to skip sub-class fields
* Options to filter fields based on certain attributes such as: private fields, enum names, readonly fields, etc
* Custom date format and fallback date format during deserialization
* Polymorphic types with "$type" attribute during deserialization
* Generic types during deserialization
* Deserialize and append to existing object (Incremental decoding, could be used to merge multiple config files)
* Supports nested JSON String within JSON as normal sub JSON Object instead of serialized json string to avoid un-necessary escape in String literal
* Supports forward compatibility features: silently ignore unknown properties, use @DefaultEnum to annotate a default enum which be used if unknown enum value is encountered
* Supports proposed json extension format ([JSONEX](./JSONEX.md)), [Example in test](JSONCoder/src/test/resources/org/jsonex/jsoncoder/jsonex.json)
* JDK version: 1.7 or above 


And many more, please refer the class [JSONCoderOption](JSONCoder/src/main/java/org/jsonex/jsoncoder/JSONCoderOption.java)
for more details. 

Please refer the unit test class for more detailed usage patterns: 
[JSONCoderTest](JSONCoder/src/test/java/org/jsonex/jsoncoder/JSONCoderTest.java)

## Usage

- Maven Dependencies
    ```xml
     <dependency>
       <groupId>org.jsonex</groupId>
       <artifactId>JSONCoder</artifactId>
       <version>${jsonCoderVersion}</version>
     </dependency>
    ````
    You can get current version by searching [maven central](https://search.maven.org/search?q=g:org.jsonex)

- Simple Serialization / Deserialization
    ```java
      // serialization
      JSONCoder.global.encode(o)
      // de-serialization
      SomeClass obj = JSONCoder.global.decode(str, SomeClass.class);
    ```
- Filter out fields and classes
    ```java
   JSONCoderOption opt = new JSONCoderOption();
    // For TestBean2 and it's sub-classes, only include field: "enumField2", "testBean"
    opt.addFilterFor(TestBean2.class, include("enumField2", "testBean"));("field1ForClass1", "field2ForClass1");
    // For TestBean, exclude field: "publicStrField"
    opt.addFilterFor(TestBean.class, exclude("publicStrField"));
    // For any class, exclude field: "fieldInAnyClass"
    opt.getDefaultFilter().addProperties("fieldInAnyClass");
    // Exclude certain classes
    opt.addSkippedClasses(SomeExcludedClass.class);
    String result = JSONCoder.encode(bean, opt);
    ```
- Mask out certain fields: for privacy reason, quite often when we serialize an object, we need to maskout certain fields such as emailAddress, here's example to do that:
  ```java
  String result = JSONCoder.encode(bean, JSONCoderOption.ofIndentFactor(2).addFilterFor(SomeBean.class, mask("field1", "field2")));
  ```
   
- Deserialize with generic types
    ```java
    String str = "['str1', 'str2', 'str3']";
    List<String> result = JSONCoder.global.decode(new DecodeReq<List<String>>(){}.setSource(str));
    ```
- Deserialize and merge to existing object (Incremental decode)
    ```java
    TestBean bean = JSONCoder.global.decodeTo(jsonStr, bean);
    ```
- Set custom Quote and custom indentations
    ```java
    JSONCoderOption opt = new JSONCoderOption();
    opt.getJsonOption().setQuoteChar('`');
    opt.getJsonOption().setIndentFactor(2);
    String jsonStr = JSONCoder.global.encode(someObj, opt);
    ```
- Register custom coder for certain classes
    ```java
    public class CoderBigInteger implements ICoder<BigInteger>{
      public Class<BigInteger> getType() {return BigInteger.class;}
      
      @Override public TDNode encode(BigInteger o, BeanCoderContext context, TDNode target) {
        return target.setValue(o.toString());
      }
    
      @Override public BigInteger decode(TDNode jsonNode, Type type, BeanCoderContext context) {
        return new BigInteger((String)jsonNode.getValue());
      }
    }
    JSONCoderOption opt = new JSONCoderOption()
      .addCoder(new CoderBigInteger());
    String jsonStr = JSONCoder.global.encode(new BigInteger("1234"), opt); 
    ```
 
## Limitations and Future enhancements
* Performance improvement
* Support of variable placeholder in JSON doc

## Tech blogs
[Dependency Indirection with Injectable Factory](https://medium.com/@jianwu_23512/dependency-indirection-with-injectable-factory-d6f2f60cced1)

## License
 
Copyright 2018-2021 eBay Inc. <BR>
Author/Developer: Jianwu Chen, Narendra Jain
 
Use of this source code is governed by an MIT-style license that can be found in the LICENSE file or at https://opensource.org/licenses/MIT.
