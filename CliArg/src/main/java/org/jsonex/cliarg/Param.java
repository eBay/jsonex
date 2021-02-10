package org.jsonex.cliarg;

import org.jsonex.core.annotation.*;
import org.jsonex.core.type.Nullable;
import org.jsonex.core.util.BeanProperty;
import lombok.Data;

import static org.jsonex.core.util.LangUtil.doIf;
import static org.jsonex.core.util.LangUtil.doIfNotNull;
import static org.jsonex.core.util.ListUtil.isIn;
import static org.jsonex.core.util.StringUtil.noNull;

/**
 * Represent an command line parameter, it can be either argument or option
 * If index is not null indicate it's argument
 *   Argument default to required unless explicitly specified.
 *   required argument can't follow non-required argument which index less than it
 * If index is null indicates it's an option, option default to not required, unless specified
 *
 * For option of Boolean type, it will be mapped as flag, that means the value of the option can be omitted.
 *
 * For Param of complex type or array/list, the value can be specified as JSON(ex) string, the top level "{" or "[",
 * can be, omitted. The quote for key and value can be omitted.
 *
 * For array parameters, it also possible to specify the values as separate options. The values will be merged
 *
 * Following Annotation will be processed for each parameter:
 *
 * {@link Name}  Name of the parameter, optional, default to field name
 * {@link ShortName}  The optional short name
 * {@link Description}  The optional description
 * {@link Index}  Indicate this an indexed parameter
 * {@link Required}  Indicate if this field is required. all the index fields are required unless explicitly indicated.
 *     All the non-index fields are not required unless explicitly indicated.
 */
@Data
public class Param {
  transient final BeanProperty property;
  String name;
  @Nullable String shortName;
  @Nullable Integer index;
  @Nullable String description;
  @Nullable Object defVal;
  private Boolean required;

  public Param(BeanProperty property, Object defObj) {
    this.property = property;
    name = property.getName();
    defVal = property.get(defObj);
    doIfNotNull(property.getAnnotation(Name.class), a -> name = a.value());
    doIfNotNull(property.getAnnotation(ShortName.class), a -> shortName = a.value());
    doIfNotNull(property.getAnnotation(Description.class), a -> description = a.value());
    doIfNotNull(property.getAnnotation(Index.class), a -> index = a.value());
    doIfNotNull(property.getAnnotation(Required.class), a -> required = a.value());
  }

  boolean isBooleanType() { return isIn(property.getType(), Boolean.class, boolean.class); }

  boolean isRequired() { return required != null ? required : index != null; }

  String getUsage() {
    if (index != null)
      return isRequired() ? " <" + name + ">" : " [" + name + "]";

    StringBuilder sb = new StringBuilder(shortName != null ? "-" + shortName : "--" + name);
    doIf(!isBooleanType(), () -> sb.append(" {"+ name +"}"));
    return isRequired() ? " " + sb.toString() : " [" + sb + "]";
  }

  String getDescriptionLine() {
    StringBuilder sb = new StringBuilder();
    if (index == null) {
      sb.append(shortName != null ? "-" + shortName + ", " : "    ");
      sb.append("--" + name);
    } else
      sb.append("<" + name + ">");
    sb.append(":\t" + noNull(description));
    return sb.toString();
  }
}
