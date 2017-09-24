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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.symphonyoss.symphony.tools.rest.util.ProgramFault;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class ModelObjectOrConfig extends ModelObjectOrConfigOrBuilder
{
  protected static final String F = "%-30s %s\n";
  protected static final String DOT_PROPERTIES = ".properties";

  protected void setProperties(Properties props)
  {
  }
  
  protected void loadFromProperties(Properties props)
  {
  }
  

  
  protected void load(JsonNode jsonNode)
  {
  }
  
  public void setIfNotNull(Properties prop, String name, Object value)
  {
    if(value != null)
    {
      String str = value.toString().trim();
      
      if(str.length()>0)
        prop.setProperty(name, str);
    }
  }
  
  protected long  getLongProperty(Properties props, String name)
  {
    String s = props.getProperty(name);
    
    if(s == null)
      return 0;
    
    try
    {
      return Integer.parseInt(s);
    }
    catch(NumberFormatException e)
    {
      return 0;
    }
  }
  
  
  @Override
  public void load(File configDir, String fileName) throws NoSuchObjectException
  {
    File        config  = new File(configDir, fileName + DOT_PROPERTIES);
    Properties  props   = new Properties();
    
    try(Reader reader = new FileReader(config))
    {
      props.load(reader);
      
      loadFromProperties(props);
    }
    catch(FileNotFoundException e)
    {
      throw new NoSuchObjectException(e);
    }
    catch (IOException e)
    {
      throw new ProgramFault("Failed to load", e);
    }
  }
  
  @Override
  final public void store(File configDir, String fileName)
  {
    if(!configDir.isDirectory())
    {
      if(!configDir.mkdirs())
      {
        throw new ProgramFault("Failed to create directory " + configDir.getAbsolutePath());
      }
    }
    
    doStore(configDir, fileName);
  }
  
  protected void doStore(File configDir, String fileName)
  { 
    File config = new File(configDir, fileName + DOT_PROPERTIES);
    Properties props = new Properties();
    
    setProperties(props);
    
    try(Writer writer = new FileWriter(config))
    {
      props.store(writer, "Created by ModelObjectOrConfig");
    }
    catch (IOException e)
    {
      throw new ProgramFault(e);
    }
  }
}
