# JSONex Specification

## Overview
JSON is a popular format for data serialization and configuration, but due to the constraints from the original standard, it becomes painful to be used for configuration. Thus, different kinds of similar alternative we created, such as YAML and TOML. With many advantages of JSON format such as simplicity, self-explanatory and compatibility of Javascript syntax, We still feel JSON is the right fit for configuration with some minor extensions, thus we propose an extended JSON format **JSONex**, which is supported by Jsonex libraries in both [Java](https://github.com/eBay/jsonex) and [TypeScript](https://github.com/treedoc/treedoc_ts).

## Issues with JSON
* **Mandatory quote for keys:** This is un-necessary redundant data just to waste spaces and bandwidth and make it harder to write and read
* **Only double quote (") can be used:** Not like javascript can use single quote (') or (`) as quote, with this flexibility, lots of escapes can be eliminated
* **No comments Support:** That's the most complained issue as a configuration format
* **Does not support multi-line String literal**: As configuration, we often need to embedded structured text
* **No comma allowed at end of last element**: This causes many of merge issues and make comment out a single line difficult. In javascript, this is allowed

## Proposal
To solve the above limitations, we propose **JSONEX** with following extensions
* Fully compatible with ES6 object literal syntax (So no need specific parser for Javascript)
* Standard JSON is a validate JSONEX
* Support line/block comments as Javascript
* Quote for Key is optional, only if key is not a valid javascript identifier, quote is mandatory
* Quote can use either ("), (') or (\`)
* Multi-line String literal support with back quote (`).
* Commas are allowed for last element (Make it merge friendly)
* Object attributes order matters, those order will be persisted

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

## Other similar effort
- [Json5](https://json5.org/)
- [Hjson](https://hjson.github.io/)

## See JSONex in a live viewer
- [TreeDocViewer](http://treedoc.org)

