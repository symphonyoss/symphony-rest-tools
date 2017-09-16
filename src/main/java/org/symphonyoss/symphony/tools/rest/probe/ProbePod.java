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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;

import org.symphonyoss.symphony.jcurl.JCurl;
import org.symphonyoss.symphony.jcurl.JCurl.Builder;
import org.symphonyoss.symphony.jcurl.JCurl.HttpMethod;
import org.symphonyoss.symphony.tools.rest.util.Console;
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
  private static final String   PKCS12                = "pkcs12";
  private static final String   UNKNOWN               = "UNKNOWN";
  private static final String   SESSION_TOKEN         = "sessionToken";
  private static final String   KEYMANAGER_TOKEN      = "keyManagerToken";

  private Console               console_              = new Console(System.in, System.out, System.err);
  private String                name_;
  private String                domain_;
  private int                   connectTimeoutMillis_ = 2000;
  private int                   readTimeoutMillis_    = 0;
  private Set<X509Certificate>  rootCerts_            = new HashSet<>();
  private Set<X509Certificate>  serverCerts_          = new HashSet<>();
  private boolean               podHealthy_;
  private int                   podId_;
  private String                keyManagerUrl_        = UNKNOWN;
  private String                podUrl_               = UNKNOWN;
  private String                podApiUrl_            = UNKNOWN;
  private String                agentApiUrl_          = UNKNOWN;
  private String                sessionAuthUrl_       = UNKNOWN;
  private String                keyAuthUrl_           = UNKNOWN;
  private String                keystore_;
  private String                storepass_            = "changeit";
  private String                storetype_            = "pkcs12";
  private ScanResponse          sessionAuthResponse_;
  private ScanResponse          keyAuthResponse_;

  private Probe                 sessionInfoResult_;
  private ScanResponse          agentResponse_;
  private String                trustStorePassword_   = "changeit";
  private ISrtHome              srtHome_;

  public static void main(String[] argv) throws IOException
  {
    new ProbePod().run(argv);
  }

  private void run(String[] argv) throws IOException
  {
    try
    {
      SrtCommandLineHome srtHome = new SrtCommandLineHome((v) -> 
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
    
      srtHome_ = srtHome;
      srtHome.process(argv);
  
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
      
      File      configDir = srtHome.getConfigDir(name_ + domain_);
      File      configStoreFile  = new File(configDir, "symphony.properties");
      boolean   doProbe = true;
  
      if(configStoreFile.exists())
      {
        console_.println("We have an existing config for this Pod:");
        console_.println("========================================");
        
        try(BufferedReader r = new BufferedReader(new FileReader(configStoreFile)))
        {
          String line;
          
          while((line = r.readLine()) != null)
          {
            console_.println(line);
          }
        }
        
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
          
          if(podUrl_ != null)
            break;
        }
        
        if(keyManagerUrl_ == null)
        {
          console_.println("No podInfo, try to look for an in-cloud key manager...");
          
          keyManagerUrl_ = podUrl_ + "/relay";
        }
        
        if(keyManagerUrl_ == null)
        {
          // We found a pod but can't get podInfo - fatal error
          return;
        }
            
        try
        {
          URL kmUrl = new URL(keyManagerUrl_);
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
            keyAuthUrl_ = getUrl(keyAuthResponse_, TOKEN);
            
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
    //      ProbeResponse response = probe(builder.build(), keyManagerName + keyManagerDomain, keyManagerUrl_, MIME_HTML);
    //      
    //      if(response.isFailed())
    //        return;
          console_.println("Found key manager at " + keyManagerUrl_);
          
          console_.println();
          console_.println("Probing for API Agent");
          console_.println("=====================");
          
          agentResponse_ = probeAgent(name_, domain_);
          
          agentApiUrl_ = getUrl(agentResponse_, null);
        }
        catch (MalformedURLException e)
        {
          console_.println("Invalid keyManagerUrl \"" + keyManagerUrl_ + "\" (" +
              e.getMessage() + ")");
          return;
        }
    
        console_.println();
        console_.println("Probe Successful");
        console_.println("================");
        
        String  format = "%-20s=%s\n";
        
        console_.printf(format, "Pod URL", podUrl_);
        console_.printf(format, "Pod ID", podId_);
        console_.printf(format, "Key Manager URL", keyManagerUrl_);
        console_.printf(format, "Session Auth URL", sessionAuthUrl_);
        console_.printf(format, "Key Auth URL", keyAuthUrl_);
        console_.printf(format, "Pod API URL", podApiUrl_);
        console_.printf(format, "Agent API URL", agentApiUrl_);
        
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
        console_.println("Root server certs:");
        for (X509Certificate cert : rootCerts_)
          console_.println(cert.getSubjectX500Principal().getName());
    
        console_.println();
        console_.println("End server certs:");
        for (X509Certificate cert : serverCerts_)
          console_.println(cert.getSubjectX500Principal().getName());
        
        boolean updateConfig = false;
        
        if(configDir.exists())
        {
          if(configDir.isDirectory())
          {
            if(configStoreFile.exists())
            {
              updateConfig = console_.promptBoolean("Overwrite the saved config?");
            }
          }
          else
          {
            console_.error("Configuration directory \"" + configDir.getAbsolutePath() +
                "\" exists but is not a directory!");
          }
        }
        else
        {
          if(configDir.mkdirs())
          {
            updateConfig = true;
          }
          else
          {
            console_.error("Configuration directory \"" + configDir.getAbsolutePath() +
                "\" cannot be created.");
          }
        }
        
        if(updateConfig)
        {
          try
          {
            KeyStore  trustStore      = KeyStore.getInstance(PKCS12);
            File      trustStoreFile  = new File(configDir, "client.truststore");
            int       certIndex=1;
            
            trustStore.load(null, null);
            
            for (X509Certificate cert : rootCerts_)
              trustStore.setCertificateEntry(String.valueOf(certIndex++), cert);
            
            try(OutputStream stream = new FileOutputStream(trustStoreFile))
            {
              trustStore.store(stream, trustStorePassword_.toCharArray());
            }
            
            console_.println("Truststore saved as " + trustStoreFile.getAbsolutePath());
            
            try(PrintWriter stream = new PrintWriter(configStoreFile))
            {
              Properties  prop = new Properties();
              
              prop.setProperty("pod.url", podUrl_);
              prop.setProperty("keymanager.url", keyManagerUrl_);
              prop.setProperty("sessionauth.url", sessionAuthUrl_);
              prop.setProperty("keyauth.url", keyAuthUrl_);
              prop.setProperty("pod.url", podApiUrl_);
              prop.setProperty("agent.url", agentApiUrl_);
              prop.setProperty("truststore.file", trustStoreFile.getAbsolutePath());
              prop.setProperty("truststore.password", trustStorePassword_);
              if(keystore_ != null)
              {
                prop.setProperty("user.cert.file", keystore_);
                prop.setProperty("user.cert.password", storepass_);
                prop.setProperty("user.cert.type", storetype_);
              }
              
              prop.store(stream, "Created by ProbePod");
            }
            
            console_.println("Config file saved as " + configStoreFile.getAbsolutePath());
      
          }
          catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
          {
            console_.error("Failed to save trust store");
            console_.printStackTrace(e);
          }
        }
      }
    }
    finally
    {
      console_.close();
    }
  }

  private String getUrl(ScanResponse scanResponse, String token)
  {
    String url = UNKNOWN;
    
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

  private void probePod(int port)
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
    
    podUrl_ = "https://" + name_ + domain_ + (port == 443 ? "" : ":" + port);
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
      
      sessionAuthUrl_ = getUrl(sessionAuthResponse_, TOKEN);
      
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
        podApiUrl_ = sessionInfoResult_.getBaseUrl();
      }
      else
      {
        podApiUrl_ = sessionInfoResult_.getBaseUrl();
        
        console_.println("found pod API endpoint at " + podApiUrl_);
        
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
      keyManagerUrl_ = km.asText();
    
      console_.println("keyManagerUrl is " + keyManagerUrl_);
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
    keyManagerUrl_ = podInfoJsonData.get("keyManagerUrl").asText();

  }

  private @Nonnull ScanResponse probeAuth(String title, String basePath, String name, String domain)
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
  
  private @Nonnull ScanResponse probeAgent(String name, String domain)
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
  
  private void probeAgent(Probe probe)
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
      rootCerts_.add(cert);

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

  private boolean probeNonSSL(int port)
  {
    JCurl jcurl = getJCurl().build();

    String url = "http://" + name_ + domain_ + ":" + port;

    try
    {
      HttpURLConnection connection = jcurl.connect(url);

      console_.println("response from " + url + " = " + connection.getResponseCode());

      return connection.getResponseCode() == 200;

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
