# JSONCoder

## Description
JSONCoder is a light weight generic object serialization / deserialization library similar to Jackson and FastJson.
This library has been widely used in various eBay domain projects for many years. It does not mean to be a replacement 
for other popular libraries. But it solves some specific problems which are not available or not well supported in other 
alternatives. Following are some of the particular features which make this library useful for certain scenario:
 
* Focuses on serialize arbitrary java objects by default by detecting cyclic object references
* Makes sensible default over configuration, such as silently ignore unknown properties by default for forward compatibility 
* Focuses on developer friendly APIs at the same time still provides flexible configurations.  
* Provides options to generate compact JSON format (No-standard format, such as make quotes for key optional, 
options to use single quote, thus avoid un-necessary escape)
* Parses configuration friendly JSON variations. Such as optional quotes, multi-line string literal, (comments to be supported) etc

## Features
* Auto-detect cyclic references and serialize the reference to avoid stack overflow
* Options to include class fields in addition to getters
* Plugable custom encoder and decoder 
* Plugable custom filter to filter out/in certain fields based on particular types
* Options to skip certain classes
* Options to skip sub-class fields
* Options to filter fields based on certain attributes such as: private fields, enum names, readonly fields, etc
* Custom Date format and fallback date format during deserialization
* Polymorphic types with "$type" attribute during deserialization
* Generic types during deserialization
* Deserialize and append to existing object (Incremental decoding, could be used to merge multiple config files)
* Support nested JSON String within JSON as normal sub JSON Object instead of serialized json string to avoid un-necessary
 escape in String literal
* Support Forward compatibility features: silently ignore unknown properties, use @DefaultEnum to annotate a default enum
which be used if unknown enum value encountered
* Partially Support proposed json extension format ([JSONX](./JSONX.md))
  * Certain compact JSON variations such as: quote with: ', ",`, and make quote for key optional 
  * Multi-line string literal with back quote '`'  

 

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
    opt.getOrAddSimpleFilter(SomeClass1.class).setInclude(true).addProperties("field1ForClass1", "field2ForClass1");
    // For SomeClass2, exclude field: "fieldForClass2"
    opt.getOrAddSimpleFilter(SomeClass2.class).addProperties("fieldForClass2");
    // For any class, exclude field: "fieldInAnyClass"
    opt.getOrAddDefaultFilter().addProperties("fieldInAnyClass");
    // Exclude certain classes
    opt.addSkippedClasses(SomeExcludedClass.class);
    String result = JSONCoder.encode(bean, opt);

    ```
4. Deserialize with generic types
    ```java
    String str = "['str1', 'str2', 'str3']";
    List<String> result = JSONCoder.global.decode(new DecodeReq<List<String>>(str){});
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
      
      @Override public Object encode(BigInteger o, BeanCoderContext context) {
        return o.toString();
      }
    
      @Override public BigInteger decode(Object o, Type type, BeanCoderContext context) {
        return new BigInteger((String)o);
      }
    }
    JSONCoderOption opt = new JSONCoderOption()
      .addCoder(new CoderBigInteger());
    String jsonStr = JSONCoder.global.encode(new BigInteger("1234"), opt);  
    ```
 
## Limitations and Future enhancements
* Support comments "//", "/*.. */"
* Currently for serialized JSON with "$ref", it can't be de-serialized due to the limitation of JSON parser
in standard JSON library, Will implement custom JSON parser to decouple from standard JSON library to solve
this issue.
* Performance improvement

## License
 
Copyright 2018-2019 eBay Inc. <BR>
Author/Developer: Jianwu Chen, Narendra Jain
 
Use of this source code is governed by an MIT-style license that can be found in the LICENSE file or at https://opensource.org/licenses/MIT.
