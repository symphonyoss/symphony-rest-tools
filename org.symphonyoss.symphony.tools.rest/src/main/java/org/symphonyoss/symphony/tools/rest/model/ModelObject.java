/*
 *
 *
 * Copyright 2017 Symphony Communication Services, LLC.
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.symphonyoss.symphony.tools.rest.model;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.symphonyoss.symphony.tools.rest.model.osmosis.ComponentProxy;
import org.symphonyoss.symphony.tools.rest.model.osmosis.ComponentStatus;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ModelObject extends ComponentProxy implements IModelObject
{
  
  private static final String   FORMAT_1_REQUIRED_FIELD_MISSING = "Required field \"%s\" missing";
  private static final String   FORMAT_1_INVALID_STATUS         = "Invalid component status \"%s\"";
  public static final String    NAME                            = "name";
  public static final String    COMPONENT_STATUS                = "componentStatus";
  public static final String    COMPONENT_STATUS_MESSAGE        = "componentStatusMessage";

  private final IModelObject parent_;
  private final String              typeName_;
  private final String              name_;
  
  private StringBuilder             errorBuilder_ = new StringBuilder();
  private String                    errorText_    = null;
  
  /**
   * Intended for virtual model objects which do no have persisted state.
   * For real model objects the other constructor should be used.
   * 
   * @param parent
   * @param typeName
   * @param name
   */
  public ModelObject(IModelObject parent, String typeName, String name)
  {
    parent_ = parent;
    typeName_ = typeName;
    name_ = name;
  }

  public ModelObject(IModelObject parent, String typeName, JsonNode config)  throws InvalidConfigException
  {
    this(parent, typeName, getRequiredTextNode(config, NAME));
    
    ComponentStatus status = null;
    
    String s = getOptionalTextNode(config, COMPONENT_STATUS);
    
    if(s != null)
    {
      try
      {
        status = ComponentStatus.valueOf(s);
      }
      catch(IllegalArgumentException e)
      {
        setComponentStatus(ComponentStatus.Failed, String.format(FORMAT_1_INVALID_STATUS, s));
      }
    }
    
    setComponentStatus(status, getOptionalTextNode(config, COMPONENT_STATUS_MESSAGE));
  }
  
  public void storeConfig(ObjectNode jsonNode, boolean includeMutable)
  {
    // Sub-classes should call super.storeConfig(jsonNode, includeMutable) when overriding.
    
    jsonNode.put(NAME, name_);
    
    if(includeMutable)
    {
      putIfNotNull(jsonNode, COMPONENT_STATUS,          getComponentStatus());
      putIfNotNull(jsonNode, COMPONENT_STATUS_MESSAGE,  getComponentStatusMessage());
    }
  }
  
  public static class Builder implements IBuilder
  {
    protected ObjectNode jsonNode_ = JsonNodeFactory.instance.objectNode();
    
    public JsonNode toJson()
    {
      return jsonNode_;
    }
    
    @Override
    public void store(File configDir, String fileName) throws IOException
    {
      ModelObject.store(configDir, fileName, jsonNode_);
    }

    @Override
    public void store(File configDir) throws IOException
    {
      ModelObject.store(configDir, jsonNode_);
    }
    
    public Builder setName(String name)
    {
      jsonNode_.put(NAME, name);
      return this;
    }
    
    public @Nullable String getName()
    {
      return getOptionalTextNode(jsonNode_, NAME);
    }
  }

  @Override
  public boolean hasChildren()
  {
    return false;
  }

  @Override
  public IModelObject[] getChildren()
  {
    return null;
  }

  @Override
  public IModelObject getParent()
  {
    return parent_;
  }

  @Override
  public String getTypeName()
  {
    return typeName_;
  }

  @Override
  public String getName()
  {
    return name_;
  }
  
  @Override
  public ObjectNode toJson()
  {
    ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
    
    storeConfig(jsonNode, true);
    
    return jsonNode;
  }
  
  public void addError(String message)
  {
    synchronized (errorBuilder_)
    {
      if(errorText_ != null)
        errorBuilder_.append("\n");
      
      errorBuilder_.append(message);
      errorText_ = errorBuilder_.toString();
    }
  }

  @Override
  public String getErrorText()
  {
    return errorText_;
  }
  

  @Override
  public void store(File configDir, String fileName) throws IOException
  {
    store(configDir, fileName, toJson());
  }

  @Override
  public void store(File configDir) throws IOException
  {
    store(configDir, toJson());
  }

  public static void store(File configDir, ObjectNode json) throws IOException
  {
    store(configDir, CONFIG_FILE_NAME, json);
  }
  
  public static void store(File configDir, String fileName, ObjectNode json) throws IOException
  {
    if(!configDir.isDirectory())
    {
      if(!configDir.mkdirs())
      {
        throw new IOException("Failed to create directory " + configDir.getAbsolutePath());
      }
    }
    
    File config = new File(configDir, fileName + DOT_JSON);
    ObjectMapper mapper = new ObjectMapper();
    
    try
    {
      mapper.writerWithDefaultPrettyPrinter().writeValue(config, json);
    }
    catch (IOException e)
    {
      throw new IOException(e);
    }
  }
 
  @Override
  public void print(PrintWriter out)
  {
    JsonFactory jsonFactory = new JsonFactory();
    jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    ObjectMapper mapper = new ObjectMapper(jsonFactory);
    
    try
    {
      mapper.writerWithDefaultPrettyPrinter().writeValue(out, toJson());
      out.println();
    }
    catch (IOException e)
    {
      throw new ProgramFault(e);
    }
  }
  
  /**
   * This object has been replaced with the given one.
   * 
   * @param newPod
   */
  public void modelUpdated(ModelObject newPod)
  {
  }

  protected static void putIfNotNull(ObjectNode jsonNode, String name, Object value)
  {
    if(value != null)
    {
      String str = value.toString().trim();
      
      if(str.length()>0)
        jsonNode.put(name, str);
    }
  }
  
  protected static JsonNode getRequiredNode(JsonNode jsonNode, String name) throws InvalidConfigException
  {
    JsonNode node = jsonNode.get(name);
    
    if(node == null)
      throw new InvalidConfigException(String.format(FORMAT_1_REQUIRED_FIELD_MISSING, name));
    
    return node;
  }
  
  protected static @Nonnull String getRequiredTextNode(JsonNode jsonNode, String name) throws InvalidConfigException
  {
    return getRequiredNode(jsonNode, name).asText();
  }
  
  protected static @Nonnull URL getRequiredUrlNode(JsonNode jsonNode, String name) throws InvalidConfigException
  {
    try
    {
      return new URL(getRequiredNode(jsonNode, name).asText());
    }
    catch (MalformedURLException e)
    {
      throw new InvalidConfigException(e);
    }
  }
  
  protected static long getRequiredLongNode(JsonNode jsonNode, String name) throws InvalidConfigException
  {
    return getRequiredNode(jsonNode, name).asLong();
  }
  
  protected static @Nullable String getOptionalTextNode(JsonNode jsonNode, String name)
  {
    JsonNode node = jsonNode.get(name);
    
    if(node == null)
      return null;
    
    return node.asText();
  }
  
  protected static @Nonnull URL getOptionalUrlNode(JsonNode jsonNode, String name) throws InvalidConfigException
  {
    try
    {
      JsonNode node = jsonNode.get(name);
      
      if(node == null)
        return null;
      
      return new URL(node.asText());
    }
    catch (MalformedURLException e)
    {
      throw new InvalidConfigException(e);
    }
  }
  
  protected static @Nullable Long getOptionalLongNode(JsonNode jsonNode, String name)
  {
    JsonNode node = jsonNode.get(name);
    
    if(node == null)
      return null;
    
    return node.asLong();
  }
}
