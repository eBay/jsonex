# JSONex Specification

## Overview
JSON is a popular format for data serialization and configuration, but due to the constraints from the original standard, it becomes painful to be used for configuration. Thus, different kinds of similar alternative we created, such as YAML and TOML. With many advantages of JSON format such as simplicity, self-explanatory and compatibility of Javascript syntax, We still feel JSON is the right fit for configuration with some minor extensions, thus we propose an extended JSON format **JSONex**, which is supported by Jsonex libraries in both [Java](https://github.com/eBay/jsonex) and [TypeScript](https://github.com/treedoc/treedoc_ts).

## Issues with JSON
* **Mandatory quote for keys:** This is un-necessary redundant data just to waste spaces and bandwidth and make it harder to write and read
* **Only double quote (") can be used:** Not like javascript can use single quote (') or (`) as quote, with this flexibility, lots of escapes can be eliminated
* **No comments Support:** That's the most complained issue as a configuration format
* **Does not support multi-line String literal**: As configuration, we often need to embedded structured text
* **No comma allowed at end of last element**: This causes many of merge issues and make comment out a single line difficult. In javascript, this is allowed

## Design Goal
* **Easy to use** for many kinds of use cases
  * configure files
  * command line arguments
  * URL parameters
  * Data storage. Serialize/Deserialize objects
  * Data transportation through network.
* **Storage efficient**: remove as much redundancy as possible
* **Easy to parse** The parser should be straight forward to implement

## Proposal
To solve the above limitations, we propose **JSONEX** with following extensions
* Fully compatible with ES6 object literal syntax for majority variables (So no need specific parser for Javascript)
* Standard JSON is a validate JSONEX
* Support line/block comments as Javascript
* Quote for Key is optional, required only if key is not a valid javascript identifier, quote is mandatory
* Quote for value is optional, required only if value has special characters confuses parser (Non ES6 compatible)
* Quote can use either ("), (') or (\`)
* Optional top level brace (Non ES6 compatible)
* Multi-line String literal support with back quote (`).
* Commas are allowed for last element (Make it merge friendly)
* Object attributes order matters, those order will be persisted
* Path compression such as a:b:c  (Non ES6 compabile)

## Examples

**Normal JSON**
```json
{
  "name": "project \"Name\"",
  "version": "0.1.0",
  "scripts": {
    "lint": "vue-cli-service lint"
  },
  "dependencies": {
    "axios": "^0.18.0",
    "bootstrap": "^4.1.1"
  },
  "multiLine": "line1\nline2"
}
```
**JSONEX**
```ecmascript 6
// JSONEX Format
{
  name: 'project"Name"',  /* No escape of double quote when quoted with single quote */
  version: "0.1.0",
  scripts: {
    lint: "vue-cli-service lint",   // Allow comma after last element
  },
  dependencies: {
    axios: "^0.18.0",
    bootstrap: "^4.1.1",
  },
  // Use back quote for multi-line literal
  multiLine: `line1  
line2`,
}
```
## Comparison with JSON for some features

<table>
<tr><th>Feature</th><th>JSONex</th><th>JSON</th></tr>
<tr>
  <td>optional json top level braces - object</td>
  <td><code>a:1,b:2</code></td>
  <td>

```json
{
  "a": 1,
  "b": 2
}
```
  </td>
</tr>
<tr>
  <td>optional json top level braces - array</td>
  <td><code>a,b,1</code></td>
  <td>

```json
["a", "b", 1]
```
  </td>
</tr>
<tr>
  <td>optional json top level braces - array of objects</td>
  <td><code>{a:1},{b:2},c</code></td>
  <td>

```json
[
  {"a": 1},
  {"b": 2},
  "c"
]
```
  </td>
</tr>


<tr>
  <td>Path compression</td>
  <td><code>a:b:{c:1, d:2}</code></td>
  <td>

```json
{
  "a":{
    "b":{
      "c":1,
      "d":2
    }
  }
}
```
  </td>
</tr>
<tr>
  <td>Type wrapper</td>
  <td><code>buyer:user{name:abc, age:10}</code></td>
  <td>

```json
{
  "buyer":{
    "$type":"user",
    "name":"abc",
    "age":10
  }
}
```
  </td>
</tr>
<tr>
  <td>Optional Quotes</td>
  <td><code>{a: value1, b: value2}</code></td>
  <td>

```json
{"a": "value1", "b": "value2"}
```
  </td>
</tr>
<tr>
  <td>Commas are allowed for last element</td>
  <td><code>{a: 1, b: 2, }</code></td>
  <td>

```json
{"a": 1, "b": 2}
```
  </td>
</tr>
<tr>
  <td>Multi-line value</td>
  <td><pre>{
    a: 
      `abc 
       line2`, 
    b: 2 
  }</pre></td>
  <td>

```json
{"a": "abc\nline2", "b": 2}
```
  </td>
</tr>

</table>


## Other similar effort
- [Json5](https://json5.org/)
- [Hjson](https://hjson.github.io/)

## See JSONex in a live viewer
- [TreeDocViewer](http://treedoc.org)

