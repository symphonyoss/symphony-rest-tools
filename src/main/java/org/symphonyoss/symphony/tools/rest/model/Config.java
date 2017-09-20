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
import java.io.PrintWriter;
import java.util.Properties;

public abstract class Config extends ModelObjectOrConfig implements IConfig
{
  private static final String        HOST_NAME        = "host.name";
  
  /* package */ String               hostName_;
  
  private StringBuilder       errorBuilder_ = new StringBuilder();
  private String              errorText_    = null;

  
  protected Config()
  {}
  
  protected Config(Config other)
  {
    hostName_           = other.hostName_;
  }
  
  @Override
  protected void loadFromProperties(Properties props)
  {
    super.loadFromProperties(props);
    
    hostName_       = props.getProperty(HOST_NAME);
  }

  @Override
  protected void setProperties(Properties  prop)
  {
    super.setProperties(prop);
    
    setIfNotNull(prop, HOST_NAME, hostName_);
  }
  
  @Override
  public void printFields(PrintWriter out)
  {
    out.printf(F, HOST_NAME, hostName_);
  }

  @Override
  public String getName()
  {
    return hostName_;
  }
  
  public void load(File configDir) throws NoSuchObjectException
  {
    load(configDir, PROPERTY_FILE_NAME);
  }

  @Override
  public void store(File configDir)
  {
    store(configDir, PROPERTY_FILE_NAME);
  }
  
  /* package */ void addError(String message)
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
}
