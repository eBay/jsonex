# JSONCoder
[![Build Status](https://travis-ci.org/jianwu/JSONCoder.svg?branch=master)](https://travis-ci.org/jianwu/JSONCoder)
[![codecov](https://codecov.io/gh/eBay/JSONCoder/branch/master/graph/badge.svg)](https://codecov.io/gh/eBay/JSONCoder)
## Description
JSONCoder is a light weight generic object serialization / deserialization library similar to Jackson or FastJson. This library has been widely used in various eBay domain projects for years. It does not mean to be a replacement for other popular libraries. But it solves some specific problems which are not available or not well supported in other alternatives. 

## Why JSONCoder
There are plenty of options for JSON serialization/deserialization libraries, why we need another one? If you are also a fan of JSON , but hate the restrictions by JSON standards that make it so hard to be used as a configuration language, but still don't want to give up JSON to YAML (JSON is nice, why need Yet Another Markup Language). You can have a try of this library. This library focuses on solving following problems well:  

* Supports JSON extension proposal ([JSONX](./JSONX.md)) which will make it friendly for configuration, such as
    * comments support
    * quote of key is optional
    * customize quote characters (Avoid un-necessary escapes by choosing different quote characters) 
    * multi-line string literal as in ES6
    * Merge of config files (e.g. environment specific config override common config)
* Supports serialization of arbitrary java objects out of box by detecting cyclic object references. You don't have to write serializer friendly classes. Whatever you have, it's supported.
* Make sensible default over configuration. Minimize annotation or configuration usage. Such as it silently ignores unknown properties by default for forward compatibility
* Focuses on developer friendly APIs at the same time still provides flexible configurations  

## Features
* Auto-detect cyclic references and serialize the reference to avoid stack overflow
* Options to include class fields in addition to getters
* Plugable custom encoder and decoder 
* Plugable custom filter to filter out/in certain fields based on particular types
* Options to skip certain classes
* Options to skip sub-class fields
* Options to filter fields based on certain attributes such as: private fields, enum names, readonly fields, etc
* Custom date format and fallback date format during deserialization
* Polymorphic types with "$type" attribute during deserialization
* Generic types during deserialization
* Deserialize and append to existing object (Incremental decoding, could be used to merge multiple config files)
* Supports nested JSON String within JSON as normal sub JSON Object instead of serialized json string to avoid un-necessary escape in String literal
* Supports forward compatibility features: silently ignore unknown properties, use @DefaultEnum to annotate a default enum which be used if unknown enum value is encountered
* Supports proposed json extension format ([JSONX](./JSONX.md)), [Example in test](JSONCoder/src/test/resources/com/ebay/jsoncoder/jsonext.json)
* JDK version: 1.7 or above 


And many more, please refer the class [JSONCoderOption](JSONCoder/src/main/java/com/ebay/jsoncoder/JSONCoderOption.java)
for more details. 

Please refer the unit test class for more detailed usage patterns: 
[JSONCoderTest](JSONCoder/src/test/java/com/ebay/jsoncoder/JSONCoderTest.java)

## Usage

1. Maven Dependencies
    ```xml
     <dependency>
       <groupId>com.ebay.jsoncoder</groupId>
       <artifactId>JSONCoder</artifactId>
       <version>${jsonCoderVersion}</version>
     </dependency>
    ````
2. Simple Serialization / Deserialization
    ```java
      // serialization
      JSONCoder.global.encode(o)
      // de-serialization
      SomeClass obj = JSONCoder.global.decode(str, SomeClass.class);
    ```
3. Filter out fields and classes
    ```java
   JSONCoderOption opt = new JSONCoderOption();
    // For SomeClass1 and it's sub-classes, only include field: "field1ForClass1", "field2ForClass1"
    opt.getSimpleFilterFor(SomeClass1.class).setInclude(true).addProperties("field1ForClass1", "field2ForClass1");
    // For SomeClass2, exclude field: "fieldForClass2"
    opt.getSimpleFilterFor(SomeClass2.class).addProperties("fieldForClass2");
    // For any class, exclude field: "fieldInAnyClass"
    opt.getDefaultFilter().addProperties("fieldInAnyClass");
    // Exclude certain classes
    opt.addSkippedClasses(SomeExcludedClass.class);
    String result = JSONCoder.encode(bean, opt);

    ```
4. Deserialize with generic types
    ```java
    String str = "['str1', 'str2', 'str3']";
    List<String> result = JSONCoder.global.decode(new DecodeReq<List<String>>(){}.setSource(str));
    ```
5. Deserialize and merge to existing object (Incremental decode)
    ```java
    TestBean bean = JSONCoder.global.decodeTo(jsonStr, bean);
    ```
6. Set custom Quote and custom indentations
    ```java
    JSONCoderOption opt = new JSONCoderOption();
    opt.getJsonOption().setQuoteChar('`');
    opt.getJsonOption().setIndentFactor(2);
    String jsonStr = JSONCoder.global.encode(someObj, opt);
    ```
7. Register custom coder for certain classes
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


## License
 
Copyright 2018-2019 eBay Inc. <BR>
Author/Developer: Jianwu Chen, Narendra Jain
 
Use of this source code is governed by an MIT-style license that can be found in the LICENSE file or at https://opensource.org/licenses/MIT.
