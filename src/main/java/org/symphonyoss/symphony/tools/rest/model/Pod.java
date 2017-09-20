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
import java.util.Properties;

public class Pod extends ModelObject implements IPod, IUrlEndpoint
{
  private static final String POD_HEALTHY = "pod.healthy";
  private static final String POD_ID = "pod.id";
  
  private PodConfig             config_;
  private boolean               podHealthy_;
  private int                   podId_;
  private URL                   url_;
  
  public Pod(PodConfig config) throws NoSuchObjectException
  {
    super(null, config);
    
    config_ = config;
    if(url_ != null)
    {
      try
      {
        url_ = new URL(config_.getPodUrl());
      }
      catch (MalformedURLException e)
      {
        addError("Invalid URL");
      }
    }
    
    addUrlEndpoint("KeyManager", config_.getKeyManagerUrl());
    addUrlEndpoint("SessionAuth", config_.getSessionAuthUrl());
    addUrlEndpoint("KeyAuth", config_.getKeyAuthUrl());
  }
  
  

  public static Pod newInstance(File configDir) throws NoSuchObjectException
  {
    PodConfig config = new PodConfig();
    
    config.load(configDir);
    
    return new Pod(config);
  }

  @Override
  protected void printFields(PrintWriter out)
  {
    super.printFields(out);
    
    out.printf(F, POD_HEALTHY,   podHealthy_);
    out.printf(F, POD_ID,        podId_);
  }

  @Override
  public void setProperties(Properties props)
  {
    super.setProperties(props);
    
    setIfNotNull(props, POD_HEALTHY,   podHealthy_);
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
}
