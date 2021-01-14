package org.jsonex.cliarg;

import lombok.Data;
import lombok.SneakyThrows;
import org.jsonex.core.annotation.Description;
import org.jsonex.core.annotation.Examples;
import org.jsonex.core.annotation.Name;
import org.jsonex.core.annotation.Summary;
import org.jsonex.core.type.Nullable;
import org.jsonex.core.util.Assert;
import org.jsonex.core.util.BeanProperty;
import org.jsonex.core.util.ClassUtil;

import java.util.*;

import static java.lang.Math.min;
import static org.jsonex.core.util.LangUtil.doIf;
import static org.jsonex.core.util.LangUtil.doIfNotNull;
import static org.jsonex.core.util.ListUtil.first;
import static org.jsonex.core.util.ListUtil.setAt;

/**
 * CLI specification based on annotated java bean of `cls`. Following annotations will be processed:
 *
 * Class level:
 *   {@link Name}: Name of the command. (Optional) Default to the class simple name
 *   {@link Summary}: Summary of the command (Optional)
 *   {@link Description}: Description of the command (Optional)
 *   {@link Examples}: Array of string representation of samples usages (Optional)
 *
 * For field level annotations, please refer to class {@link Param}
 *
 * @param <T>
 */
@Data
public class CLISpec<T> {
  final Class<T> cls;
  final T defVal;
  String name;
  @Nullable String summary;
  @Nullable String description;
  @Nullable String[] examples;

  int firstOptionalIndex = Integer.MAX_VALUE;
  List<Param> optionParams = new ArrayList<>();
  List<Param> indexedParams = new ArrayList<>();
  Set<String> requiredParams = new HashSet<>();

  @SneakyThrows
  public CLISpec(Class<T> cliCls) {
    this.cls = cliCls;
    this.defVal = createDefaultInstance();
    init();
  }

  private void init() {
    name = cls.getSimpleName();
    doIfNotNull(cls.getAnnotation(Name.class), a -> name = a.value());
    doIfNotNull(cls.getAnnotation(Description.class), a -> description = a.value());
    doIfNotNull(cls.getAnnotation(Summary.class), a -> summary = a.value());
    doIfNotNull(cls.getAnnotation(Examples.class), a -> examples = a.value());

    Map<String, BeanProperty> propertyMap = ClassUtil.getProperties(cls);
    for(BeanProperty prop : propertyMap.values()) {
      if (prop.isImmutable(false))
        continue;
      Param param = new Param(prop, defVal);
      doIf(param.isRequired(), () -> requiredParams.add(param.name));

      if (param.index != null) {
        setAt(indexedParams, param.index, param);
        if (!param.isRequired())
          firstOptionalIndex = min(firstOptionalIndex, param.index);
        else {
          Assert.isTrue(param.index < firstOptionalIndex, () ->
              "Required index argument can't be after Non-Required argument: firstOptionalIndex:"
                  + firstOptionalIndex + "; param: " + param);
        }
      } else
        optionParams.add(param);
    };
    for (int i = 0; i < indexedParams.size(); i++) {
      Assert.isTrue(indexedParams.get(i) != null,
          "Indexed argument has to start from 0 and be continuous, missing definition at index: " +i);
    }
  }

  public Optional<Param> getOptionParamByName(String name) {
    return first(optionParams, p -> p.name.equals(name) || name.equals(p.shortName));
  }

  public String printUsage() {
    StringBuilder sb = new StringBuilder("NAME: " + name);
    doIfNotNull(summary, s -> sb.append(" - " + s));
    doIfNotNull(description, s -> sb.append("\nDESCRIPTION\n  " + s));
    sb.append("\nUSAGE\n  " + getUsage());
    if (examples != null && examples.length > 0) {
      sb.append("\nEXAMPLES");
      for (String ex : examples)
        sb.append("\n  " + ex);
    }

    sb.append("\nARGUMENTS / OPTIONS");
    StringBuilder optDesc = new StringBuilder();
    for (Param p : indexedParams)
      optDesc.append("\n  " + p.getDescriptionLine());

    for (Param p : optionParams)
      optDesc.append("\n  " + p.getDescriptionLine());

    sb.append(TextFormatter.alignTabs(optDesc.toString()));
    return sb.toString();
  }

  public String getUsage() {
    StringBuilder sb = new StringBuilder(name);
    for (Param p : optionParams)
      sb.append(p.getUsage());

    for (Param p : indexedParams)
      sb.append(p.getUsage());

    return sb.toString();
  }

  @SneakyThrows
  public T createDefaultInstance() { return cls.newInstance(); }
  public CLIParser<T> parse(String[] args, int argIndex) { return new CLIParser<T>(this, args, argIndex).parseOneParam(); }
}
