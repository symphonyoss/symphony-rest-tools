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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.symphonyoss.symphony.tools.rest.util.ProgramFault;
import org.symphonyoss.symphony.tools.rest.util.home.IPodManager;
import org.symphonyoss.symphony.tools.rest.util.home.ModelObjectManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PodManager extends ModelObjectManager implements IPodManager
{
  private Map<String, Pod>                     podMap_    = new HashMap<>();
  private boolean                              allLoaded_;
  private CopyOnWriteArrayList<IModelListener> listeners_ = new CopyOnWriteArrayList<>();

  public PodManager(File configDir)
  {
    super(configDir);
  }
  
  
  @Override
  public int getSize()
  {
    synchronized(podMap_)
    {
      if(allLoaded_)
        return podMap_.size();
    }
    
    String[] names = getConfigDir().list();
    
    if(names == null)
      return 0;
    
    return names.length;
  }


  @Override
  public String getDefaultPodName() throws NoSuchObjectException
  {
    synchronized(podMap_)
    {
      if(allLoaded_)
      {
        Set<String> keySet = podMap_.keySet();
        
        if(keySet.size() == 0)
          throw new NoSuchObjectException("No pod configurations exist");
        
        if(keySet.size() == 1)
          return keySet.iterator().next();
        
        return null;
      }
    }
    
    String[] names = getConfigDir().list();
    
    if(names.length == 0)
      throw new NoSuchObjectException("No pod configurations exist");
    
    if(names.length == 1)
      return names[0];
    
    return null;
  }


  @Override
  public Set<IPod> getAll()
  {
    loadAll();
    
    return new HashSet<IPod>(podMap_.values());
  }

  private void loadAll()
  {
    if(!allLoaded_)
    {
      boolean updated = false;
      
      synchronized(podMap_)
      {
        for(File file : getConfigDir().listFiles())
        {
          if(podMap_.get(file.getName()) == null)
          {
            try
            {
              podMap_.put(file.getName(), loadPod(file));
              updated = true;
            }
            catch(IOException | InvalidConfigException e)
            {
              throw new ProgramFault("Failed to read pod config", e);
            }
          }
        }
      }

      if(updated)
        for(IModelListener listener : listeners_)
          listener.modelChanged();
    }
  }

  @Override
  public Pod getPod(String hostName)
  {
    synchronized(podMap_)
    {
      if(!podMap_.containsKey(hostName))
      {
        File configDir = getConfigPath(hostName);
       
        Pod pod;
        
        try
        {
          pod = loadPod(configDir);
        }
        catch(IOException | InvalidConfigException e)
        {
          pod = null;
        }
        
        podMap_.put(hostName, pod);
      }
    }
    
    return podMap_.get(hostName);
  }

  private Pod loadPod(File configDir) throws JsonProcessingException, IOException, InvalidConfigException
  {
    File podConfig = new File(configDir, IModelObject.CONFIG_FILE_NAME + IModelObject.DOT_JSON);
    ObjectMapper mapper = new ObjectMapper();
    
    JsonNode jsonNode = mapper.readTree(podConfig);
    
    return new Pod(this, jsonNode);
  }


  @Override
  public IPod createOrUpdatePod(Pod.Builder podConfig, Agent.Builder agentBuilder) throws InvalidConfigException, IOException
  {
    File configDir = getConfigPath(podConfig.getName());
    
    Pod   newPod = podConfig.build(this);
    
    if(agentBuilder.getAgentApiUrl() != null)
    {
      newPod.addAgent(agentBuilder);
    }
    
    newPod.store(configDir);
    
    Pod oldPod;
    synchronized (podMap_)
    {
      oldPod = podMap_.put(podConfig.getName(), newPod);
    }
    
    if(oldPod != null)
    {
      oldPod.modelUpdated(newPod);
    }
    
    modelChanged();
    
    return newPod;
  }
  
  @Override
  public void modelChanged()
  {
    for(IModelListener listener : listeners_)
      listener.modelChanged();
  }
  
  @Override
  public void modelObjectChanged(IModelObject modelObject)
  {
    for(IModelListener listener : listeners_)
      listener.modelObjectChanged(modelObject);
  }
  
  @Override
  public void modelObjectStructureChanged(IModelObject modelObject)
  {
    for(IModelListener listener : listeners_)
      listener.modelObjectStructureChanged(modelObject);
  }

  @Override
  public IModelObject[] getElements()
  {
    synchronized (podMap_)
    {
      return podMap_.values().toArray(new IModelObject[podMap_.size()]);
    }
  }

  @Override
  public void addListener(IModelListener listener)
  {
    listeners_.add(listener);
    loadAll();
  }

  @Override
  public void removeListener(IModelListener listener)
  {
    listeners_.remove(listener);
  }
}
