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
public class PodConfig extends SslServerConfig implements IPodConfig
{
  public static final String TYPE_NAME = "Pod";

  private static final String        POD_URL          = "pod.url";
  private static final String        KEY_MANAGER_URL  = "keymanager.url";
  private static final String        SESSION_AUTH_URL = "sessionauth.url";
  private static final String        KEY_AUTH_URL     = "keyauth.url";
  private static final String        POD_API_URL      = "podApi.url";
  
  /* package */ String               keyManagerUrl_;
  /* package */ String               podUrl_;
  /* package */ String               podApiUrl_;
  /* package */ String               sessionAuthUrl_;
  /* package */ String               keyAuthUrl_;
  

  public PodConfig()
  {}
  
  public PodConfig(PodConfig other)
  {
    super(other);
    
    keyManagerUrl_      = other.keyManagerUrl_;
    podUrl_             = other.podUrl_;
    podApiUrl_          = other.podApiUrl_;
    sessionAuthUrl_     = other.sessionAuthUrl_;
    keyAuthUrl_         = other.keyAuthUrl_;
  }

  @Override
  protected void loadFromProperties(Properties props)
  {
    super.loadFromProperties(props);
    
    podUrl_         = props.getProperty(POD_URL);
    keyManagerUrl_  = props.getProperty(KEY_MANAGER_URL);
    sessionAuthUrl_ = props.getProperty(SESSION_AUTH_URL);
    keyAuthUrl_     = props.getProperty(KEY_AUTH_URL);
    podApiUrl_      = props.getProperty(POD_API_URL);
  }

  @Override
  public void setProperties(Properties  prop)
  {
    super.setProperties(prop);
    
    setIfNotNull(prop, POD_URL, podUrl_);
    setIfNotNull(prop, KEY_MANAGER_URL, keyManagerUrl_);
    setIfNotNull(prop, SESSION_AUTH_URL, sessionAuthUrl_);
    setIfNotNull(prop, KEY_AUTH_URL, keyAuthUrl_);
    setIfNotNull(prop, POD_API_URL, podApiUrl_);
  }
  
  @Override
  public void printFields(PrintWriter out)
  {
    out.printf(F, POD_URL, podUrl_);
    out.printf(F, KEY_MANAGER_URL, keyManagerUrl_);
    out.printf(F, SESSION_AUTH_URL, sessionAuthUrl_);
    out.printf(F, KEY_AUTH_URL, keyAuthUrl_);
    out.printf(F, POD_API_URL, podApiUrl_);
  }

  @Override
  public String getKeyManagerUrl()
  {
    return keyManagerUrl_;
  }

  @Override
  public String getPodUrl()
  {
    return podUrl_;
  }

  @Override
  public String getPodApiUrl()
  {
    return podApiUrl_;
  }

  @Override
  public String getSessionAuthUrl()
  {
    return sessionAuthUrl_;
  }

  @Override
  public String getKeyAuthUrl()
  {
    return keyAuthUrl_;
  }

  @Override
  public String getTypeName()
  {
    return TYPE_NAME;
  }
}
