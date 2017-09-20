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

package org.symphonyoss.symphony.tools.rest.probe;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;

import org.symphonyoss.symphony.jcurl.JCurl;
import org.symphonyoss.symphony.jcurl.JCurl.Builder;
import org.symphonyoss.symphony.jcurl.JCurl.HttpMethod;
import org.symphonyoss.symphony.tools.rest.model.AgentConfigBuilder;
import org.symphonyoss.symphony.tools.rest.model.IPod;
import org.symphonyoss.symphony.tools.rest.model.PodConfigBuilder;
import org.symphonyoss.symphony.tools.rest.util.Console;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;
import org.symphonyoss.symphony.tools.rest.util.home.ISrtHome;
import org.symphonyoss.symphony.tools.rest.util.home.SrtCommandLineHome;

import com.fasterxml.jackson.databind.JsonNode;

public class ProbePod
{
  private static final String   MIME_HTML             = "text/html";
  private static final String   MIME_JSON             = "application/json";
  private static final int[]    POD_PORTS             = new int[] { 443, 8443 };
  private static final int[]    AUTH_PORTS            = new int[] { 8444, 8445, 8446 };
  private static final int[]    AgentPorts            = new int[] { 443, 8444, 8445, 8446 };
  private static final String[] SUFFIXES              = new String[] { "-api", "" };
  private static final String   TOKEN                 = "token";
  private static final String   DisplayName           = "displayName";
  private static final String   Id                    = "id";
  private static final String   Company               = "company";
  private static final String[] SessionInfoFields     = new String[] { DisplayName, Id, Company };
  private static final String   SESSION_TOKEN         = "sessionToken";
  private static final String   KEYMANAGER_TOKEN      = "keyManagerToken";

  private final Console         console_;
  private String                name_;
  private String                domain_;
  private int                   connectTimeoutMillis_ = 2000;
  private int                   readTimeoutMillis_    = 0;
  private boolean               podHealthy_;
  private int                   podId_;
  
  private String                keystore_;
  private String                storepass_            = "changeit";
  private String                storetype_            = "pkcs12";
  private ScanResponse          sessionAuthResponse_;
  private ScanResponse          keyAuthResponse_;

  private Probe                 sessionInfoResult_;
  private ScanResponse          agentResponse_;
    
  private ISrtHome              srtHome_;
  private PodConfigBuilder      podConfig_            = new PodConfigBuilder();
  private AgentConfigBuilder    agentConfig_          = new AgentConfigBuilder();
  private Set<X509Certificate>  serverCerts_          = new HashSet<>();
  
  public static void main(String[] argv) throws IOException
  {
    new ProbePod(new Console(System.in, System.out, System.err), argv).run();
  }

  public ProbePod(Console console, String[] argv)
  {
    console_ = console;
    
    SrtCommandLineHome parser = new SrtCommandLineHome((v) -> 
      {
        if (name_ == null)
          name_ = v;
        else
          System.err.println("Unknown parameter \"" + v + "\" ignored.");
  
      })
      .addFlag((v) -> keystore_ = v, "keystore")
      .addFlag((v) -> storepass_ = v, "storepass")
      .addFlag((v) -> storetype_ = v, "storetype")
      ;
  
    parser.process(argv);
    
    srtHome_ = parser.createSrtHome(console_);
  }
  
  public ProbePod(Console console, String name, ISrtHome srtHome)
  {
    console_ = console;
    name_ = name;
    srtHome_ = srtHome;
  }

  public void run() throws IOException
  {
    try
    {
      if (name_ == null)
      {
        name_ = console_.promptString("Hostname");
        // error("A host name must be specified (.symphony.com is implied)");
      }
  
  
      int i = name_.indexOf('.');
  
      if (i == -1)
        domain_ = ".symphony.com";
      else
      {
        domain_ = name_.substring(i);
        name_ = name_.substring(0, i);
      }
  
      console_.println("name=" + name_);
      console_.println("domain=" + domain_);
      console_.println();
      
      podConfig_.setHostName(name_ + domain_);
      
      IPod pod = srtHome_.getPodManager().getPod(podConfig_.getName());
      boolean   doProbe = true;
  
      if(pod != null)
      {
        console_.println("We have an existing config for this Pod:");
        console_.println("========================================");
        
        pod.print(console_.getOut());
        
        doProbe = console_.promptBoolean("Continue with probe?");
      }
      
      if(!doProbe)
      {
        console_.error("Aborted.");
      }
      else
      {
        console_.println("Probing for Pod");
        console_.println("===============");
        
        for(int port : POD_PORTS)
        {
          probePod(port);
          
          if(podConfig_.getPodUrl() != null)
            break;
        }
        
        if(podConfig_.getPodUrl() == null)
        {
          console_.println();
          console_.println("Probe Reveals a Website but no Pod");
          console_.println("==================================");
          
          String  format = "%-20s=%s\n";
          
          console_.printf(format, "Web URL", podConfig_.getWebUrl());
          console_.println();
        }
        else
        {
          if(podConfig_.getKeyManagerUrl() == null)
          {
            console_.println("No podInfo, try to look for an in-cloud key manager...");
            
            podConfig_.setKeyManagerUrl(new URL(podConfig_.getPodUrl() + "/relay"));
          }
          
          if(podConfig_.getKeyManagerUrl() == null)
          {
            // We found a pod but can't get podInfo - fatal error
            return;
          }
              
          try
          {
            URL kmUrl = new URL(podConfig_.getKeyManagerUrl());
            String keyManagerDomain;
            String keyManagerName = kmUrl.getHost();
            
            i = keyManagerName.indexOf('.');
      
            if (i == -1)
              keyManagerDomain = ".symphony.com";
            else
            {
              keyManagerDomain = keyManagerName.substring(i);
              keyManagerName = keyManagerName.substring(0, i);
            }
      
            console_.println("keyManagerName=" + keyManagerName);
            console_.println("keyManagerDomain=" + keyManagerDomain);
            
            
            console_.println();
            console_.println("Probing for API Keyauth");
            console_.println("=======================");
            
            keyAuthResponse_ = probeAuth("Key Auth", "/keyauth", keyManagerName, keyManagerDomain);
            
            if(keyAuthResponse_ != null)
            {
              podConfig_.setKeyAuthUrl(getUrl(keyAuthResponse_, TOKEN));
              
              String token = getTag(keyAuthResponse_, TOKEN);
              
              if(token != null)
                srtHome_.saveSessionToken(name_ + domain_, KEYMANAGER_TOKEN, token);
            }
      
            // Need to find a reliable health check indicator of keymanager in
            // all deployments, for now assume that as the pod told is this
            // is the KM that it is.
            
      //      Builder builder = getJCurl();
      //      builder = cookieAuth(builder);
      //      
      //      ProbeResponse response = probe(builder.build(), keyManagerName + keyManagerDomain, podConfig_.getKeyManagerUrl(), MIME_HTML);
      //      
      //      if(response.isFailed())
      //        return;
            console_.println("Found key manager at " + podConfig_.getKeyManagerUrl());
            
            console_.println();
            console_.println("Probing for API Agent");
            console_.println("=====================");
            
            agentResponse_ = probeAgent(name_, domain_);
            
            agentConfig_.setAgentApiUrl(getUrl(agentResponse_, null));
          }
          catch (MalformedURLException e)
          {
            console_.println("Invalid keyManagerUrl \"" + podConfig_.getKeyManagerUrl() + "\" (" +
                e.getMessage() + ")");
            return;
          }
      
          console_.println();
          console_.println("Probe Successful");
          console_.println("================");
          
          String  format = "%-20s=%s\n";
          
          console_.printf(format, "Web URL", podConfig_.getWebUrl());
          console_.printf(format, "Pod URL", podConfig_.getPodUrl());
          console_.printf(format, "Pod ID", podId_);
          console_.printf(format, "Key Manager URL", podConfig_.getKeyManagerUrl());
          console_.printf(format, "Session Auth URL", podConfig_.getSessionAuthUrl());
          console_.printf(format, "Key Auth URL", podConfig_.getKeyAuthUrl());
          console_.printf(format, "Pod API URL", podConfig_.getPodApiUrl());
          console_.printf(format, "Agent API URL", agentConfig_.getAgentApiUrl());
          
          if(keystore_ != null)
          {
            console_.println();
            console_.printf(format, "Client cert", keystore_);
            
            if(sessionInfoResult_.isFailed())
            {
              console_.println("This cert was not accepted for authentication");
            }
            else
            {
              console_.println("We authenticated as");
              for(String field : SessionInfoFields)
                console_.printf(format, "userInfo." + field, sessionInfoResult_.getJcurlResponse().getTag(field));
            }
          }
          console_.println();
        }
        
        console_.println("Root server certs:");
        for (X509Certificate cert : podConfig_.getTrustCerts())
          console_.println(cert.getSubjectX500Principal().getName());
    
        console_.println();
        console_.println("End server certs:");
        for (X509Certificate cert : serverCerts_)
          console_.println(cert.getSubjectX500Principal().getName());
        
        boolean updateConfig = pod == null ? true : console_.promptBoolean("Overwrite the saved config?");;
        
        
        if(updateConfig)
        {
          pod = srtHome_.getPodManager().createOrUpdatePod(podConfig_.build());

          if(agentConfig_.getAgentApiUrl() != null)
            pod.createOrUpdateAgent(agentConfig_);
          console_.error("Finished.");
        }
      }
    }
    catch(ProgramFault e)
    {
      console_.error("PROGRAM FAULT");
      e.printStackTrace(console_.getErr());
    }
    catch(Throwable e)
    {
      e.printStackTrace(console_.getErr());
    }
    finally
    {
      console_.close();
    }
  }

  private URL getUrl(ScanResponse scanResponse, String token)
  {
    URL url = null;
    
    if(scanResponse.getValidProbe() != null)
    {
      url = scanResponse.getValidProbe().getBaseUrl();
      
      if(token == null)
        console_.println("Found " + scanResponse.getName() + " endpoint at " + url);
      else if(getTag(scanResponse, token) != null)
        console_.println("Found " + scanResponse.getName() + " endpoint at " + url + " and we authenticated!");
      else
        console_.println("Found " + scanResponse.getName() + " endpoint at " + url + " but we failed to authenticate.");
    }
    else
    {
      List<Probe> certAuthProbes = scanResponse.getCertAuthProbes();
      
      if(certAuthProbes.size() == 1)
      {
        url = certAuthProbes.get(0).getBaseUrl();
        console_.println("Found probable " + scanResponse.getName() + " endpoint at " + url);

      }
      else if(certAuthProbes.size() > 1)
      {
        for(Probe p : certAuthProbes)
          console_.println("Found possible " + scanResponse.getName() + " endpoint at " + p.getBaseUrl());
      }
      else
      {
        console_.println("Failed to find any " + scanResponse.getName() + " endpoint");
      }
    }
    return url;
  }

  private void probePod(int port) throws MalformedURLException
  {
    Probe probe = new Probe(name_, "", domain_, port,
        "/");
    
    doProbe(probe);
    
    if (probe.isFailed())
    {
      if (probeNonSSL(port))
        console_.println("This is a non-SSL website");
      else
        console_.println("This is not a website");

      return;
    }
    else if(podConfig_.getWebUrl() == null)
    {
      podConfig_.setWebUrl(probe.getProbeUrl());
    }
    

    probe = new Probe(name_, domain_, "", port,
        "/").setProbePath("/client/index.html", MIME_HTML);
    
    doProbe(probe);
    
    if (probe.isFailed())
    {
      console_.println("This is a website but not a Symphony Pod");
      return;
    }

    Probe healthCheckResult = new Probe(name_, domain_, "", port,
        "/").setProbePath("/webcontroller/HealthCheck", MIME_JSON);
    
    doProbe(healthCheckResult);

    if (healthCheckResult.isFailed())
    {
      console_.println("This looks quite like a Symphony Pod, but it isn't");
      return;
    }

    JsonNode healthJson = healthCheckResult.getJsonNode();

    if (healthJson == null)
    {
      console_.println("This looks a lot like a Symphony Pod, but it isn't");
      return;
    }

    if (!healthJson.isObject())
    {
      console_.println("This looks like a Symphony Pod, but the healthcheck returns something other than an object");
      console_.println(healthJson);
      return;
    }
    
    podConfig_.setPodUrl("https://" + name_ + domain_ + (port == 443 ? "" : ":" + port));
    podHealthy_ = true;
    healthJson.fields().forEachRemaining((field) ->
    {
      if (!field.getValue().asBoolean())
      {
        console_.println(field.getKey() + " is UNHEALTHY");
        podHealthy_ = false;
      }
    });

    if (podHealthy_)
      console_.println("We found a Symphony Pod!");
    else
      console_.println("We found a Symphony Pod, but it's not feeling well");

    console_.println();
    console_.println("Probing for API Sessionauth");
    console_.println("===========================");
    
    sessionAuthResponse_ = probeAuth("Session Auth", "/sessionauth", name_, domain_);
    
    if(sessionAuthResponse_ != null)
    {
      String token = getTag(sessionAuthResponse_, TOKEN);
      
      if(token != null)
        srtHome_.saveSessionToken(name_ + domain_, SESSION_TOKEN, token);
      
      podConfig_.setSessionAuthUrl(getUrl(sessionAuthResponse_, TOKEN));
      
      Builder builder = getJCurl();
      
      for(String field : SessionInfoFields)
        builder.extract(field, field);
      
      cookieAuth(builder);
      
      sessionInfoResult_ = new Probe(name_, domain_, "", port,
          "/pod").setProbePath("/v2/sessioninfo", MIME_JSON);
      
      doProbe(builder.build(), sessionInfoResult_);
      
      console_.println("JSON=" + sessionInfoResult_.getJsonNode());
      
      if(sessionInfoResult_.isFailed())
      {
        console_.println("Failed to connect to POD API");
        podConfig_.setPodApiUrl(sessionInfoResult_.getBaseUrl());
      }
      else
      {
        podConfig_.setPodApiUrl(sessionInfoResult_.getBaseUrl());
        
        console_.println("found pod API endpoint at " + podConfig_.getPodApiUrl());
        
        for(String field : SessionInfoFields)
          console_.printf("%-20s=%s\n", field, sessionInfoResult_.getJcurlResponse().getTag(field));
      }
    }
    
    Builder builder = getJCurl()
        .expect(401)
        .expect(200);
    
    cookieAuth(builder);
    
    Probe checkAuthResult = new Probe(name_, domain_, "", port,
        "/").setProbePath("login/checkauth?type=user", MIME_JSON);
    
    doProbe(builder.build(), checkAuthResult, 200, 401);

    if (checkAuthResult.isFailed())
    {
      console_.println("Can't do checkauth from this Pod.");
      return;
    }
    
    JsonNode checkAuthJson = checkAuthResult.getJsonNode();

    if (checkAuthJson == null)
    {
      console_.println("Invalid checkAuth response");
      return;
    }
    
    JsonNode km = checkAuthJson.get("keymanagerUrl");

    if (km == null)
    {
      console_.println("Invalid checkAuth response");
    }
    else
    {
      podConfig_.setKeyManagerUrl(new URL(km.asText()));
    
      console_.println("keyManagerUrl is " + podConfig_.getKeyManagerUrl());
    }
    
    builder = getJCurl();
    
    cookieAuth(builder);
    
    Probe podInfoResult = new Probe(name_, domain_, "", port,
        "/").setProbePath("/webcontroller/public/podInfo", MIME_JSON);
    
    doProbe(builder.build(), podInfoResult);

    if (podInfoResult.isFailed())
    {
      console_.println("Can't get podInfo from this Pod.");
      return;
    }

    JsonNode podInfoJson = podInfoResult.getJsonNode();

    if (podInfoJson == null)
    {
      console_.println("Invalid podInfo response");
      return;
    }
    
    JsonNode podInfoJsonData = podInfoJson.get("data");

    if (podInfoJsonData == null || !podInfoJsonData.isObject())
    {
      console_.println("This looks like a Symphony Pod, but the podInfo returns something unexpected");
      console_.println(podInfoJson);
      return;
    }
    
    podId_ = podInfoJsonData.get("podId").asInt();
    podConfig_.setKeyManagerUrl(new URL(podInfoJsonData.get("keyManagerUrl").asText()));

  }

  private @Nonnull ScanResponse probeAuth(String title, String basePath, String name, String domain) throws MalformedURLException
  {
    ScanResponse  response = new ScanResponse(title);
    
    for(String suffix : SUFFIXES)
    {
      for(int authPort : AUTH_PORTS)
      {
        Probe probe = new Probe(name, suffix, domain, authPort,
            basePath);
        
        JCurl jcurl = getJCurl()
            .method(JCurl.HttpMethod.POST)
            .extract(TOKEN, TOKEN)
            .build();
        
        probe.setProbePath("/v1/authenticate", MIME_JSON);
        
        doProbe(jcurl, probe);
        
        if(!probe.isFailed())
        {
          probe.setValid(true);
        }
        
        response.add(probe);
      }
    }
    
    return response;
  }
  
  private @Nonnull ScanResponse probeAgent(String name, String domain) throws MalformedURLException
  {
    ScanResponse  response = new ScanResponse("Agent API");
    
    for(String suffix : SUFFIXES)
    {
      for(int authPort : AgentPorts)
      {
        Probe agentProbe = new Probe(name, suffix, domain, authPort,
            "/agent");
            
        probeAgent(agentProbe);
        
        response.add(agentProbe);
      }
    }
    
    return response;
  }
  
  private void probeAgent(Probe probe) throws MalformedURLException
  {
    Builder builder = getJCurl()
        .method(HttpMethod.POST)
        .data("{ \"message\": \"Hello World\"}");
    
    headerAuth(builder);

    probe.setProbePath("/v1/util/echo", MIME_JSON);
    doProbe(builder.build(), probe);
    
    if(probe.isFailed())
      return;
    
    // Can't do 2 calls on one probe, leave this out for now....
    probe.setValid(true);
    
//    builder = getJCurl();
//    
//    headerAuth(builder);
//
//    probe.setProbePath("/v2/HealthCheck", MIME_JSON);
//    doProbe(builder.build(), probe);
//    
//    if(probe.isFailed())
//    {
//      console_.println("This looks like a pre-1.47 Agent.");
//      
//      probe.setValid(true);
//    }
//    else
//    {
//      JsonNode agentHealthJson = probe.getJsonNode();
//
//      if (agentHealthJson == null)
//      {
//        console_.println("This looks a lot like an Agent, but it isn't");
//        return;
//      }
//  
//      if (!agentHealthJson.isObject())
//      {
//        console_.println("This looks like an Agent, but the healthcheck returns something other than an object");
//        console_.println(agentHealthJson);
//        return;
//      }
//    
//      probe.setValid(true);
//      
//      agentHealthJson.fields().forEachRemaining((field) ->
//      {
//        switch(field.getKey())
//        {
//          case "podVersion":
//          case "agentVersion":
//            console_.println(field.getKey() + " is " + field.getValue().asText());
//            break;
//            
//          default:
//            if (field.getValue().asBoolean())
//            {
//              console_.println(field.getKey() + " is OK");
//            }
//            else
//            {
//              console_.println(field.getKey() + " is UNHEALTHY");
//              probe.setUnhealthy(true);
//            }
//        }
//      });
//    }
  }

  private void doProbe(Probe probe)
  {
    doProbe(getJCurl().build(), probe);
  }
  
  private void doProbe(JCurl jcurl, Probe probe, int ...expectedStatus)
  {
    try
    {
      console_.println("Probing " + probe.getProbeUrl() + "...");
      
      HttpURLConnection connection = jcurl.connect(probe.getProbeUrl());

      boolean ok = false;
      int status = connection.getResponseCode();
      
      probe.setHttpStatus(status);
      
      if(expectedStatus.length == 0)
      {
        ok = status == 200;
      }
      else
      {
        for(int exp : expectedStatus)
        {
          if(status == exp)
          {
            ok = true;
            break;
          }
        }
      }
      
      if (!ok)
      {
        console_.println("Failed with HTTP status " + probe.getHttpStatus());
        return;
      }

      JCurl.Response jcr = probe.setJcurlResponse(jcurl.processResponse(connection));

      Certificate[] certs = jcr.getServerCertificates();

      X509Certificate cert = (X509Certificate) certs[certs.length - 1];
      podConfig_.addRootCert(cert);

      console_.println("Root server cert " + cert.getSubjectX500Principal().getName());
      
      cert = (X509Certificate) certs[0];
      
      console_.println("End server cert " + cert.getSubjectX500Principal().getName());
      serverCerts_.add(cert);

      if (!probe.isResponseTypeValid())
        return;

      probe.setFailed(false);
    }
    catch (SSLHandshakeException e)
    {
      String msg = e.getMessage().toLowerCase();
      
      if (msg.contains("bad_certificate") || msg.contains("certificate_unknown"))
      {
        probe.setFailedCertAuth(true);
        console_.println("Certificate auth required for " + probe.getHostNameAndPort());
      }
      else
        console_.println("SSL problem to " + probe.getHostNameAndPort());
    }
    catch (UnknownHostException e)
    {
      console_.println(probe.getHostName() + " is not a valid host name");
    }
    catch (SocketTimeoutException | ConnectException e)
    {
      console_.println("Cannot connect to " + probe.getHostNameAndPort());
    }
    catch (CertificateParsingException | IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      console_.flush();
    }
  }

  private Builder getJCurl()
  {
    Builder builder = JCurl.builder()
        .trustAllHostnames(true)
        .trustAllCertificates(true)
        .header("User-Agent", "ProbePod / 0.1.0 https://github.com/bruceskingle/symphony-rest-tools");

    if (connectTimeoutMillis_ > 0)
      builder.connectTimeout(connectTimeoutMillis_);

    if (readTimeoutMillis_ > 0)
      builder.readTimeout(readTimeoutMillis_);

    if(keystore_ != null)
    {
      builder.keystore(keystore_);
      builder.storepass(storepass_);
    
      if(storetype_ != null)
        builder.storetype(storetype_);
    }
    return builder;
  }
  
  private Builder headerAuth(Builder builder)
  {
    String token = getTag(sessionAuthResponse_, TOKEN);
        
    if(token != null)
      builder.header(SESSION_TOKEN, token);
    
    token = getTag(keyAuthResponse_, TOKEN);
    
    if(token != null)
      builder.header(KEYMANAGER_TOKEN, token);
    
    return builder;
  }
  
  private String getTag(ScanResponse scanResponse, String tag)
  {
    if(scanResponse == null)
      return null;
    
    Probe probe = scanResponse.getValidProbe();
    
    if(probe == null)
      return null;
    
    return probe.getJcurlResponse().getTag(tag);
  }
  
  private Builder cookieAuth(Builder builder)
  {
    StringBuilder s = new StringBuilder();
    
    String token = getTag(sessionAuthResponse_, TOKEN);
    
    if(token != null)
      s = appendCookie(s, "skey", token);
    
    token = getTag(keyAuthResponse_, TOKEN);
    
    if(token != null)
      s = appendCookie(s, "kmsession", token);
    
    if(s != null)
    {
      builder.header("Cookie", s.toString());
    }
    
    return builder;
  }

  private StringBuilder appendCookie(StringBuilder s, String name, String value)
  {
    if(s == null)
      s = new StringBuilder();
    else
      s.append(";");
    
    s.append(name);
    s.append("=");
    s.append(value);
    
    return s;
  }

  private boolean probeNonSSL(int port) throws MalformedURLException
  {
    JCurl jcurl = getJCurl().build();

    URL url = new URL("http://" + name_ + domain_ + ":" + port);

    try
    {
      HttpURLConnection connection = jcurl.connect(url);

      console_.println("response from " + url + " = " + connection.getResponseCode());

      if(connection.getResponseCode() == 200)
      {
        podConfig_.setWebUrl(url);
        
        return true;
      }
      return false;
    }
    catch (IOException e)
    {
      return false;
    }
    finally
    {
    }
  }
}
