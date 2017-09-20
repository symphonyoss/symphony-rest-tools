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

public class ConfigBuilder<T extends Config> 
extends ModelObjectOrConfigOrBuilder
implements IConfig
{
  protected T config_;

  protected ConfigBuilder(T config)
  {
    config_ = config;
  }
  
  @Override
  public void printFields(PrintWriter out)
  {
    config_.printFields(out);
  }

  public ConfigBuilder<T> setHostName(String hostName)
  {
    config_.hostName_ = hostName;
    return this;
  }

  @Override
  public String getName()
  {
    return config_.getName();
  }

  @Override
  public void load(File configDir, String fileName) throws NoSuchObjectException
  {
    config_.load(configDir, fileName);
  }
  
  @Override
  public void store(File configDir)
  {
    store(configDir, PROPERTY_FILE_NAME);
  }

  @Override
  public void store(File configDir, String fileName)
  {
    config_.store(configDir, fileName);
  }

  @Override
  public String getTypeName()
  {
    return config_.getTypeName();
  }

  @Override
  public String getErrorText()
  {
    return config_.getErrorText();
  }
}
