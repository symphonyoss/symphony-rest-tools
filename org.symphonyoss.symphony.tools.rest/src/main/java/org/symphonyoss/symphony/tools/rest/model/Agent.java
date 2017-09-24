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
import java.net.MalformedURLException;
import java.net.URL;

public class Agent extends ModelObject implements IAgent
{
  private final Pod pod_;
  private AgentConfig config_;
  private URL url_;

  public Agent(Pod pod, AgentConfig config) throws NoSuchObjectException
  {
    super(pod, config);
    pod_ = pod;
    config_ = config;
    
    try
    {
      url_ = new URL(config_.getAgentApiUrl());
    }
    catch (MalformedURLException e)
    {
      addError("Invalid URL \"" + config_.getAgentApiUrl() + "\"");
    }
  }

  public static Agent newInstance(Pod pod, File configDir) throws NoSuchObjectException
  {
    AgentConfig config = new AgentConfig();
    
    config.load(configDir);
    
    return new Agent(pod, config);
  }

  @Override
  public URL getUrl()
  {
    return url_;
  }

  @Override
  public boolean hasChildren()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public IModelObject[] getChildren()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IModelObject getParent()
  {
    return pod_;
  }
}
