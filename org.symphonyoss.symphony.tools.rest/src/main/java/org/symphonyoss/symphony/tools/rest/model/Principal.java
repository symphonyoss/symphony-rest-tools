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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateParsingException;

import javax.swing.text.StyleContext.SmallAttributeSet;

import org.symphonyoss.symphony.jcurl.JCurl;
import org.symphonyoss.symphony.jcurl.JCurl.Response;
import org.symphonyoss.symphony.tools.rest.Srt;
import org.symphonyoss.symphony.tools.rest.util.Console;

public class Principal extends ModelObject
{

  private static final String SESSION_INFO = "/v2/sessioninfo";
  
  private final IPod pod_;

  private String skey_;
  private String kmsession_;
  
  public Principal(IPod pod, Config config)
  {
    super(pod, config);
    pod_ = pod;
  }

  public void validate(String skey, String kmsession)
  {

    skey_ = skey;
    kmsession_ = kmsession;

    
  }

  public static Principal newInstance(Console console, IPod pod, String skey, String kmsession)
  {
    console.println("Validating session");
    
    JCurl jcurl = JCurl.builder()
        .header(Srt.SESSION_TOKEN, skey)
        .build();
    
    PrincipalConfig config = new PrincipalConfig();
    
    try
    {
      HttpURLConnection con = jcurl.connect(pod.getPodConfig().getPodApiUrl() + SESSION_INFO);
      
      Response response = jcurl.processResponse(con);
      
      config.load(response.getJsonNode());
    }
    catch(IOException | CertificateParsingException e)
    {
      config.addError("Unable to validate JSON: " + e);
    }
    Principal principal = new Principal(pod, config);
    
    principal.skey_ = skey;
    principal.kmsession_ = kmsession;
    
    return principal;
  }

}
