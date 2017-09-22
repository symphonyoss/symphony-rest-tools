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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.symphonyoss.symphony.tools.rest.util.ProgramFault;
import org.symphonyoss.symphony.tools.rest.util.home.PodManager;

public class Pod extends ModelObject implements IPod, IUrlEndpoint
{

  public static final String      TYPE_KEY_MANAGER  = "KeyManager";
  public static final String      TYPE_SESSION_AUTH = "SessionAuth";
  public static final String      TYPE_KEY_AUTH     = "KeyAuth";

  private static final String     POD_ID            = "pod.id";
  private static final String     AGENT_DIR_NAME    = "agent";

  private final PodManager        manager_;
  private PodConfig               podConfig_;
  private Map<String, Agent>      agentMap_         = new HashMap<>();
  private Map<String, IComponent> componentMap_     = new HashMap<>();
  private Integer                 podId_;
  private URL                     url_;
  
  public Pod(PodManager manager, PodConfig config) throws NoSuchObjectException
  {
    super(null, config);
    
    manager_ = manager;
    podConfig_ = config;
    if(podConfig_.getPodUrl() != null)
    {
      try
      {
        url_ = new URL(podConfig_.getPodUrl());
      }
      catch (MalformedURLException e)
      {
        addError("Invalid Pod URL");
      }
    }
    else if(podConfig_.getWebUrl() != null)
    {
      try
      {
        url_ = new URL(podConfig_.getWebUrl());
      }
      catch (MalformedURLException e)
      {
        addError("Invalid Web URL");
      }
    }
    
    addUrlEndpoint(TYPE_KEY_MANAGER, podConfig_.getKeyManagerUrl());
    addUrlEndpoint(TYPE_SESSION_AUTH, podConfig_.getSessionAuthUrl());
    addUrlEndpoint(TYPE_KEY_AUTH, podConfig_.getKeyAuthUrl());
  }
  
  

  public static Pod newInstance(PodManager manager, File configDir) throws NoSuchObjectException
  {
    PodConfig config = new PodConfig();
    
    config.load(configDir);
    
    Pod pod = new Pod(manager, config);
    
    File agentsDir = new File(configDir, AGENT_DIR_NAME);
    
    if(agentsDir.isDirectory())
    {
      for(File agentConfigDir : agentsDir.listFiles())
      {
        Agent.newInstance(pod, agentConfigDir);
      }
    }
    return pod;
  }

  @Override
  protected void printFields(PrintWriter out)
  {
    super.printFields(out);
    
    out.printf(F, POD_ID,        podId_);
  }

  @Override
  public void setProperties(Properties props)
  {
    super.setProperties(props);
    
    setIfNotNull(props, POD_ID,        podId_);
  }

  /**
   * This object has been replaced with the given one.
   * 
   * @param newPod
   */
  public void modelUpdated(Pod newPod)
  {
  }

  @Override
  public URL getUrl()
  {
    return url_;
  }

  @Override
  public IPodConfig getPodConfig()
  {
    return podConfig_;
  }

  @Override
  public Integer getPodId()
  {
    return podId_;
  }

  
  public void setPodId(Integer podId)
  {
    podId_ = podId;
  }

  @Override
  public IAgent createOrUpdateAgent(IAgentConfig agentConfig)
  {
    File configDir = manager_.getConfigPath(podConfig_.getName(),
        AGENT_DIR_NAME, agentConfig.getName());
    
    agentConfig.store(configDir);
    
    Agent newAgent;
    
    try
    {
      newAgent = Agent.newInstance(this, configDir);
    }
    catch(NoSuchObjectException e)
    {
      throw new ProgramFault("Failed to read new agent which we just created - this can't happen", e);
    }
    
    Agent oldAgent;
    synchronized (agentMap_)
    {
      oldAgent = agentMap_.put(agentConfig.getName(), newAgent);
    }
    
    if(oldAgent != null)
    {
      oldAgent.modelUpdated(newAgent);
    }
    
    manager_.modelObjectChanged(this);
    
    return newAgent;
  }
  
  @Override
  public void resetStatus()
  {
    super.resetStatus();
    
    synchronized (componentMap_)
    {
      for(IComponent component : componentMap_.values())
        component.resetStatus();
    }
  }

  @Override
  public IComponent getComponent(String name)
  {
    while(name.startsWith("_"))
      name = name.substring(1);
    
    while(name.endsWith("_"))
      name = name.substring(0, name.length() - 1);
    
    synchronized (componentMap_)
    {
      IComponent component = componentMap_.get(name);
      
      if(component == null)
      {
        VirtualModelObject vmo = new VirtualModelObject(this, GENERIC_COMPONENT, name);
        
        componentMap_.put(name, vmo);
        
        addChild(vmo);
        
        return vmo;
      }
      
      return component;
    }
  }
}
