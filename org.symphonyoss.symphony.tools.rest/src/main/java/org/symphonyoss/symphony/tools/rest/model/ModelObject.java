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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class ModelObject extends ModelObjectOrConfig implements IModelObject
{
  private static final String       STATE_PROPS   = "state.properties";

  private static final String     POD_HEALTHY       = "component.healthy";
  private static final String     COMPONENT_DIAGNOSTIC       = "component.diagnostic";
  
  private final IVirtualModelObject parent_;
  private final Config              config_;

  private List<IVirtualModelObject> childSet_     = new ArrayList<>();
  private IVirtualModelObject[]     children_     = new IVirtualModelObject[0];
  private StringBuilder             errorBuilder_ = new StringBuilder();
  private String                    errorText_    = null;
  private Boolean                   status_;
  private String                    statusMessage_;
  
  public ModelObject(IVirtualModelObject parent, Config config)
  {
    parent_ = parent;
    config_ = config;
    
    String errorText = config_.getErrorText();
    
    if(errorText != null)
      addError(errorText);
  }

  @Override
  public IConfig getConfig()
  {
    return config_;
  }
  
  @Override
  public void print(PrintWriter out)
  {
    getConfig().printFields(out);
    printFields(out);
  }

  protected void printFields(PrintWriter out)
  {
    out.printf(F, POD_HEALTHY,   status_);
    out.printf(F, COMPONENT_DIAGNOSTIC,   statusMessage_);
  }
  
  @Override
  public void setProperties(Properties props)
  {
    super.setProperties(props);
    
    setIfNotNull(props, POD_HEALTHY,          status_);
    setIfNotNull(props, COMPONENT_DIAGNOSTIC, statusMessage_);
  }
  
  public void store(File configDir)
  {
    store(configDir, STATE_PROPS);
  }

  @Override
  public String getName()
  {
    return config_.getName();
  }
  
  @Override
  public String getTypeName()
  {
    return config_.getTypeName();
  }

  public void addChild(IVirtualModelObject child)
  {
    synchronized (childSet_)
    {
      childSet_.add(child);
      synchronized (children_)
      {
        children_ = childSet_.toArray(new IVirtualModelObject[childSet_.size()]);
      }
    }
  }
  
  @Override
  public boolean hasChildren()
  {
    synchronized (children_)
    {
      return children_.length > 0;
    }
  }

  @Override
  public IVirtualModelObject[] getChildren()
  {
    synchronized (children_)
    {
      return children_;
    }
  }

  @Override
  public IVirtualModelObject getParent()
  {
    return parent_;
  }
  
  /**
   * Adds a simple child ONLY IF THE NAME IS NON-NULL
   * @param typeName  Type of the child
   * @param name      Name of the child
   */
  public void addSimpleChild(String typeName, String name)
  {
    if(name != null)
      addChild(new VirtualModelObject(this, typeName, name));
  }
  
  /**
   * Adds a URL endpoint child ONLY IF THE NAME IS NON-NULL
   * @param typeName  Type of the child
   * @param name      Name of the child
   */
  public void addUrlEndpoint(String typeName, String url)
  {
    if(url != null)
      addChild(UrlEndpoint.newInstance(this, typeName, url));
  }
  
  public void addError(String message)
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

  @Override
  public String getComponentStatusMessage()
  {
    return statusMessage_;
  }

  @Override
  public Boolean getComponentStatus()
  {
    return status_;
  }
  
  @Override
  public void setComponentStatus(Boolean healthy, String diagnostic)
  {
    status_ = healthy;
    statusMessage_ = diagnostic;
  }
  
  @Override
  public void resetStatus()
  {
    status_ = null;
    statusMessage_ = UNKNOWN_STATUS;
  }
}
