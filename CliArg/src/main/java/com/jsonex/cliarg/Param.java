package com.jsonex.cliarg;

import com.jsonex.core.annotation.*;
import com.jsonex.core.type.Nullable;
import com.jsonex.core.util.BeanProperty;
import lombok.Data;

import static com.jsonex.core.util.LangUtil.doIfNotNull;
import static com.jsonex.core.util.ListUtil.isIn;

/**
 * Represent an command line parameter, it can be either argument or option
 * If index is not null indicate it's argument
 *   Argument default to required unless explicitly specified.
 *   required argument can't follow non-required argument which index less than it
 * If index is null indicates it's an option, option default to not required, unless specified
 *
 * For option of Boolean type, it will be mapped as flag, that means the value of the option can be omitted.
 *
 * For Param of complex type, the value can be specified as JSON(ex) string, the top level "{" or "[", can be
 * omitted. The quote for key and value can be omitted.
 */
@Data
public class Param {
  final BeanProperty property;
  String name;
  @Nullable String shortName;
  @Nullable Integer index;
  @Nullable String description;
  @Nullable Object defVal;
  boolean required;

  public Param(BeanProperty property, Object defObj) {
    this.property = property;
    name = property.getName();
    doIfNotNull(property.getAnnotation(Name.class), a -> name = a.value());
    doIfNotNull(property.getAnnotation(ShortName.class), a -> shortName = a.value());
    doIfNotNull(property.getAnnotation(Description.class), a -> description = a.value());
    doIfNotNull(property.getAnnotation(Index.class), a -> index = a.value());

    defVal = property.get(defObj);
    required = index != null;
    doIfNotNull(property.getAnnotation(Required.class), a -> required = a.value());
  }

  boolean isBooleanType() {
    return isIn(property.getType(), Boolean.class, boolean.class);
  }

  String getUsage() {
    if (index != null)
      return isRequired() ? " <" + name + ">" : " [" + name + "]";

    StringBuilder sb = new StringBuilder();
    if (shortName != null)
      sb.append("-" + shortName);
    else
      sb.append("--" + name);

    if (!isBooleanType()) {
      sb.append(" <value>");
    }
    return isRequired() ? " " + sb.toString() : " [" + sb + "]";
  }
}
