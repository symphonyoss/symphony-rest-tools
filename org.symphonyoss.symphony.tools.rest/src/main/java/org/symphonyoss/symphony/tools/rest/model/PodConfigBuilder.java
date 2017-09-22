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

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Set;

public class PodConfigBuilder extends SslServerConfigBuilder<PodConfig>
implements IPodConfig
{
  public PodConfigBuilder()
  {
    super(new PodConfig());
  }

  public PodConfigBuilder(PodConfig other)
  {
    super(new PodConfig(other));
  }
  
  public IPodConfig  build()
  {
    return new PodConfig(config_);
  }

  public PodConfigBuilder setKeyManagerUrl(URL keyManagerUrl)
  {
    config_.keyManagerUrl_ = keyManagerUrl == null ? null : keyManagerUrl.toString();
    return this;
  }

  public PodConfigBuilder setPodUrl(String podUrl)
  {
    config_.podUrl_ = podUrl;
    return this;
  }

  public PodConfigBuilder setWebUrl(URL webUrl)
  {
    config_.webUrl_ = webUrl == null ? null : webUrl.toString();
    return this;
  }

  public PodConfigBuilder setWebTitle(String webTitle)
  {
    config_.webTitle_ = webTitle;
    return this;
  }

  public PodConfigBuilder setPodApiUrl(URL podApiUrl)
  {
    config_.podApiUrl_ = podApiUrl == null ? null : podApiUrl.toString();
    return this;
  }

  public PodConfigBuilder setSessionAuthUrl(URL sessionAuthUrl)
  {
    config_.sessionAuthUrl_ = sessionAuthUrl == null ? null : sessionAuthUrl.toString();
    return this;
  }

  public PodConfigBuilder setKeyAuthUrl(URL keyAuthUrl)
  {
    config_.keyAuthUrl_ = keyAuthUrl == null ? null : keyAuthUrl.toString();
    return this;
  }

  @Override
  public String getKeyManagerUrl()
  {
    return config_.getKeyManagerUrl();
  }

  @Override
  public String getPodUrl()
  {
    return config_.getPodUrl();
  }

  @Override
  public String getWebUrl()
  {
    return config_.getWebUrl();
  }

  @Override
  public String getWebTitle()
  {
    return config_.getWebTitle();
  }

  @Override
  public String getPodApiUrl()
  {
    return config_.getPodApiUrl();
  }

  @Override
  public String getSessionAuthUrl()
  {
    return config_.getSessionAuthUrl();
  }

  @Override
  public String getKeyAuthUrl()
  {
    return config_.getKeyAuthUrl();
  }

  @Override
  public Set<X509Certificate> getTrustCerts()
  {
    return config_.trustCerts_;
  }

  public void addRootCert(X509Certificate cert)
  {
    config_.trustCerts_.add(cert);
  }
}
