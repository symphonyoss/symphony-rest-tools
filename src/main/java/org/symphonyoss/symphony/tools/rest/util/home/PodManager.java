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

package org.symphonyoss.symphony.tools.rest.util.home;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.symphonyoss.symphony.tools.rest.model.IModelListener;
import org.symphonyoss.symphony.tools.rest.model.IModelObject;
import org.symphonyoss.symphony.tools.rest.model.IPod;
import org.symphonyoss.symphony.tools.rest.model.IPodConfig;
import org.symphonyoss.symphony.tools.rest.model.NoSuchObjectException;
import org.symphonyoss.symphony.tools.rest.model.Pod;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;

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
              podMap_.put(file.getName(), Pod.newInstance(this, file));
              updated = true;
            }
            catch(NoSuchObjectException e)
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
          pod = Pod.newInstance(this, configDir);
        }
        catch(NoSuchObjectException e)
        {
          pod = null;
        }
        
        podMap_.put(hostName, pod);
      }
    }
    
    return podMap_.get(hostName);
  }

  @Override
  public IPod createOrUpdatePod(IPodConfig podConfig)
  {
    File configDir = getConfigPath(podConfig.getName());
    
    podConfig.store(configDir);
    
    Pod newPod;
    
    try
    {
      newPod = Pod.newInstance(this, configDir);
    }
    catch(NoSuchObjectException e)
    {
      throw new ProgramFault("Failed to read new pod which we just created - this can't happen", e);
    }
    
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
  
  public void modelChanged()
  {
    for(IModelListener listener : listeners_)
      listener.modelChanged();
  }
  
  public void modelObjectChanged(IModelObject modelObject)
  {
    for(IModelListener listener : listeners_)
      listener.modelObjectChanged(modelObject);
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
