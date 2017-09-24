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

import com.fasterxml.jackson.databind.JsonNode;

@Immutable
public class PrincipalConfig extends Config implements IPrincipalConfig
{
  public static final String  TYPE_NAME   = "Principal";

  private static final String USER_NAME   = "username";
  private static final String USER_ID     = "id";
  private static final String CERTIFICATE = "certificate";

  /* package */ String        userName_;
  /* package */ long          userId_;
  /* package */ String        certificate_;
  

  public PrincipalConfig()
  {}
  
  public PrincipalConfig(PrincipalConfig other)
  {
    super(other);
    
    userName_             = other.userName_;
    userId_               = other.userId_;
    certificate_          = other.certificate_;
  }

  @Override
  public void load(JsonNode jsonNode)
  {
    hostName_ = jsonNode.get(USER_NAME).asText();
    userId_ = jsonNode.get(USER_ID).asInt();
  }

  @Override
  protected void loadFromProperties(Properties props)
  {
    super.loadFromProperties(props);
    
    userName_         = props.getProperty(USER_NAME);
    userId_           = getLongProperty(props, USER_ID);
    certificate_      = props.getProperty(CERTIFICATE);
  }

  @Override
  public void setProperties(Properties  prop)
  {
    super.setProperties(prop);
    
    setIfNotNull(prop, USER_NAME, userName_);
    setIfNotNull(prop, USER_ID, userId_);
    setIfNotNull(prop, CERTIFICATE, certificate_);
  }
  
  @Override
  public void printFields(PrintWriter out)
  {
    out.printf(F, USER_NAME, userName_);
    out.printf(F, USER_ID, userId_);
    out.printf(F, CERTIFICATE, certificate_);
  }

  @Override
  public String getUserName()
  {
    return userName_;
  }

  @Override
  public long getUserId()
  {
    return userId_;
  }

  @Override
  public String getCertificate()
  {
    return certificate_;
  }

  @Override
  public String getTypeName()
  {
    return TYPE_NAME;
  }
}
