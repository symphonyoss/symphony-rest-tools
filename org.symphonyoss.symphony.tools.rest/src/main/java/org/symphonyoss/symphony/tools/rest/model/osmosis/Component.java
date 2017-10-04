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

package org.symphonyoss.symphony.tools.rest.model.osmosis;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

public class Component implements IComponent
{
  private ComponentStatus                          status_;
  private String                                   statusMessage_;
  private CopyOnWriteArrayList<IComponentListener> listeners_ = new CopyOnWriteArrayList<>();
  
  public Component()
  {
    this(ComponentStatus.Initializing, "Initializing...");
  }
  
  protected Component(ComponentStatus status, String statusMessage)
  {
    status_ = status;
    statusMessage_ = statusMessage;
  }


  @Override
  public String getComponentStatusMessage()
  {
    return statusMessage_;
  }

  @Override
  public @Nullable ComponentStatus getComponentStatus()
  {
   return status_;
  }

  protected synchronized void setComponentStatus(ComponentStatus status, String statusMessage)
  {
    status_ = status;
    statusMessage_ = statusMessage;
    
    notifyListeners();
  }

  /**
   * Reset the status of this and any child components to null.
   */
  protected void resetStatus()
  {
    status_ = null;
    statusMessage_ = UNKNOWN_STATUS;
  }
  
  public void notifyListeners()
  {
    for(IComponentListener listener : listeners_)
      listener.componentStatusChanged(status_, statusMessage_);
  }

  @Override
  public void addListener(IComponentListener listener)
  {
    listeners_.add(listener);
    
  }

  @Override
  public void removeListener(IComponentListener listener)
  {
    listeners_.remove(listener);
  }
  
  public synchronized void setComponentStatusIfMoreSevere(ComponentStatus status, String statusMessage)
  {
    if(status.isMoreSevereThan(status_))
    {
      status_ = status;
      statusMessage_ = statusMessage;
      notifyListeners();
    }
  }
}
