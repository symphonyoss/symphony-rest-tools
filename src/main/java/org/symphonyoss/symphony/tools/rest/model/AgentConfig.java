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

import java.io.PrintWriter;
import java.util.Properties;

import javax.annotation.concurrent.Immutable;

@Immutable
public class AgentConfig extends Config implements IAgentConfig
{
  private static final String AGENT_URL = "agent.url";
  
  /* package */ String                agentApiUrl_;

  public AgentConfig()
  {}
  
  public AgentConfig(AgentConfig other)
  {
    super(other);
    agentApiUrl_ = other.agentApiUrl_;
  }

  @Override
  protected void loadFromProperties(Properties props)
  {
    super.loadFromProperties(props);
    
    agentApiUrl_ = props.getProperty(AGENT_URL);
  }

  @Override
  protected void setProperties(Properties  prop)
  {
    super.setProperties(prop);
    
    setIfNotNull(prop, AGENT_URL, agentApiUrl_);
  }
  
  @Override
  public void printFields(PrintWriter out)
  {
    out.printf(F, AGENT_URL, agentApiUrl_);
  }

  @Override
  public String getAgentApiUrl()
  {
    return agentApiUrl_;
  }

  @Override
  public String getTypeName()
  {
    return "Agent";
  }
}
