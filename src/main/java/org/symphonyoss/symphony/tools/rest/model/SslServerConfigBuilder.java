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
import java.security.cert.X509Certificate;
import java.util.Set;

public class SslServerConfigBuilder<T extends SslServerConfig> extends ConfigBuilder<T>
implements ISslServerConfig
{
  protected SslServerConfigBuilder(T config)
  {
    super(config);
  }

  public SslServerConfigBuilder<T> setTrustStorePassword(String trustStorePassword)
  {
    config_.trustStorePassword_ = trustStorePassword;
    return this;
  }

  public SslServerConfigBuilder<T> setTrustStoreFile(File trustStoreFile)
  {
    config_.trustStoreFile_ = trustStoreFile;
    return this;
  }

  @Override
  public String getTrustStorePassword()
  {
    return config_.getTrustStorePassword();
  }

  @Override
  public File getTrustStoreFile()
  {
    return config_.getTrustStoreFile();
  }

  @Override
  public Set<X509Certificate> getTrustCerts()
  {
    return config_.getTrustCerts();
  }

  @Override
  public void store(File configDir, String fileName)
  {
    // TODO Auto-generated method stub
    super.store(configDir, fileName);
  }
}
