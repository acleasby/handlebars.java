/**
 * Copyright (c) 2012-2015 Edgar Espina
 *
 * This file is part of Handlebars.java.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jknack.handlebars.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.HelperRegistry;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;

/**
 * Base class for {@link Template} who need to resolver {@link Helper}.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
abstract class HelperResolver extends BaseTemplate {

  /**
   * The parameter list.
   */
  protected List<Object> params = Collections.emptyList();

  /**
   * The hash object.
   */
  protected Map<String, Object> hash = Collections.emptyMap();

  /** Param's size. */
  protected int paramSize;

  /** Hash's size. */
  protected int hashSize;

  /**
   * Empty parameters.
   */
  private static final Object[] PARAMS = {};

  /**
   * Creates a new {@link HelperResolver}.
   *
   * @param handlebars The handlebars object. Required.
   */
  public HelperResolver(final Handlebars handlebars) {
    super(handlebars);
  }

  /**
   * Build a hash object by looking for values in the current context.
   *
   * @param context The current context.
   * @return A hash object with values in the current context.
   * @throws IOException If param can't be applied.
   */
  protected Map<String, Object> hash(final Context context) throws IOException {
    if (hashSize == 0) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    for (Entry<String, Object> entry : hash.entrySet()) {
      Object value = entry.getValue();
      value = ParamType.parse(context, value);
      result.put(entry.getKey(), value);
    }
    return result;
  }

  /**
   * Build a parameter list by looking for values in the current context.
   *
   * @param ctx The current context.
   * @return A parameter list with values in the current context.
   * @throws IOException If param can't be applied.
   */
  protected Object[] params(final Context ctx) throws IOException {
    if (paramSize <= 1) {
      return PARAMS;
    }
    Object[] values = new Object[paramSize - 1];
    for (int i = 1; i < paramSize; i++) {
      Object value = params.get(i);
      Object resolved = ParamType.parse(ctx, value);
      values[i - 1] = resolved == null && handlebars.stringParams()
          ? params.get(i) : resolved;
    }
    return values;
  }

  /**
   * Build a parameter list by looking for values in the current context.
   *
   * @param ctx The current context.
   * @return A parameter list with values in the current context.
   * @throws IOException If param can't be applied.
   */
  protected Object[] decoParams(final Context ctx) throws IOException {
    Object[] values = new Object[paramSize];
    for (int i = 0; i < paramSize; i++) {
      Object value = params.get(i);
      Object resolved = ParamType.parse(ctx, value);
      values[i] = resolved == null && handlebars.stringParams()
          ? value : resolved;
    }
    return values;
  }

  /**
   * Determine the current context. If the param list is empty, the current
   * context value is returned.
   *
   * @param context The current context.
   * @return The current context.
   * @throws IOException If param can't be applied.
   */
  protected Object determineContext(final Context context) throws IOException {
    if (paramSize == 0) {
      return context.model();
    }
    Object value = params.get(0);
    return ParamType.parse(context, value);
  }

  /**
   * Find the helper by it's name.
   *
   * @param name The helper's name.
   * @return The matching helper.
   */
  protected Helper<Object> helper(final String name) {
    Helper<Object> helper = handlebars.helper(name);
    if (helper == null && (paramSize > 0 || hashSize > 0)) {
      Helper<Object> helperMissing = handlebars.helper(HelperRegistry.HELPER_MISSING);
      if (helperMissing == null) {
        throw new HandlebarsException(
            new IllegalArgumentException("could not find helper: '" + name + "'"));
      }
      helper = helperMissing;
    }
    return helper;
  }

  /**
   * Set the hash.
   *
   * @param hash The new hash.
   * @return This resolver.
   */
  public HelperResolver hash(final Map<String, Object> hash) {
    if (hash == null || hash.size() == 0) {
      this.hash = Collections.emptyMap();
    } else {
      this.hash = hash;
    }
    this.hashSize = hash.size();
    return this;
  }

  /**
   * Set the parameters.
   *
   * @param params The new params.
   * @return This resolver.
   */
  public HelperResolver params(final List<Object> params) {
    if (params == null || params.size() == 0) {
      this.params = Collections.emptyList();
    } else {
      this.params = params;
    }
    this.paramSize = this.params.size();
    return this;
  }

  /**
   * Make a string of {@link #params}.
   *
   * @param params list of params.
   * @return Make a string of {@link #params}.
   */
  protected String paramsToString(final List<?> params) {
    if (paramSize > 0) {
      StringBuilder buffer = new StringBuilder();
      String sep = " ";
      for (Object param : params) {
        if (param instanceof BaseTemplate) {
          buffer.append(((BaseTemplate) param).text()).append(sep);
        } else {
          buffer.append(param).append(sep);
        }
      }
      buffer.setLength(buffer.length() - sep.length());
      return buffer.toString();
    }
    return "";
  }

  /**
   * Make a string of {@link #hash}.
   *
   * @return Make a string of {@link #hash}.
   */
  protected String hashToString() {
    if (hashSize > 0) {
      StringBuilder buffer = new StringBuilder();
      String sep = " ";
      for (Entry<String, Object> hash : this.hash.entrySet()) {
        Object hashValue = hash.getValue();
        String hashText = hashValue instanceof Variable
            ? ((Variable) hashValue).text()
            : hashValue.toString();
        buffer.append(hash.getKey()).append("=").append(hashText)
            .append(sep);
      }
      buffer.setLength(buffer.length() - sep.length());
      return buffer.toString();
    }
    return "";
  }

  @Override
  protected void collect(final Collection<String> result, final TagType tagType) {
    for (Object param : this.params) {
      if (param instanceof Variable) {
        ((Variable) param).collect(result, tagType);
      }
    }
  }

  @Override
  protected void collectReferenceParameters(final Collection<String> result) {
    for (Object param : this.params) {
      if (param instanceof Variable) {
        ((Variable) param).collectReferenceParameters(result);
      }
    }
    for (Object param : params) {
      if (ParamType.REFERENCE.apply(param) && !ParamType.STRING.apply(param)) {
        result.add((String) param);
      }
    }
    for (Object hashValue : hash.values()) {
      if (ParamType.REFERENCE.apply(hashValue) && !ParamType.STRING.apply(hashValue)) {
        result.add((String) hashValue);
      }
    }
  }
}
