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

import org.symphonyoss.symphony.tools.rest.model.IPodConfig;
import org.symphonyoss.symphony.tools.rest.model.NoSuchObjectException;
import org.symphonyoss.symphony.tools.rest.model.Pod;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;

public class PodManager extends ModelObjectManager implements IPodManager
{
    
  private Map<String, Pod>  podMap_ = new HashMap<>();
  private boolean           allLoaded_;
  
  public PodManager(File configDir)
  {
    super(configDir);
  }
  
  @Override
  public synchronized Set<Pod> getAll()
  {
    if(!allLoaded_)
    {
      for(File file : getConfigDir().listFiles())
      {
        if(podMap_.get(file.getName()) == null)
        {
          try
          {
            podMap_.put(file.getName(), Pod.newInstance(file));
          }
          catch(NoSuchObjectException e)
          {
            throw new ProgramFault("Failed to read pod config", e);
          }
        }
      }

    }
    
    return new HashSet<Pod>(podMap_.values());
  }

  @Override
  public synchronized Pod getPod(String hostName)
  {
    if(!podMap_.containsKey(hostName))
    {
      File configDir = getConfigPath(hostName);
     
      Pod pod;
      
      try
      {
        pod = Pod.newInstance(configDir);
      }
      catch(NoSuchObjectException e)
      {
        pod = null;
      }
      
      podMap_.put(hostName, pod);
    }
    
    return podMap_.get(hostName);
  }

  @Override
  public synchronized Pod createOrUpdatePod(IPodConfig podConfig)
  {
    File configDir = getConfigPath(podConfig.getName());
    
    podConfig.store(configDir);
    
    Pod newPod;
    
    try
    {
      newPod = Pod.newInstance(configDir);
    }
    catch(NoSuchObjectException e)
    {
      throw new ProgramFault("Failed to read new pod which we just created - this can't happen", e);
    }
    
    Pod oldPod = podMap_.put(podConfig.getName(), newPod);
    
    if(oldPod != null)
    {
      oldPod.modelUpdated(newPod);
    }
    
    return newPod;
  }
}
