/*
 *
 *
 * Copyright 2017 The Symphony Software Foundation
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
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
import org.symphonyoss.symphony.tools.rest.util.CommandLineParser;

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
  private static final String   Probably              = " (probably, we could not actually authenticate)";
  private static final String   PKCS12                = "pkcs12";
  private static final String   UNKNOWN               = "UNKNOWN";

  private PrintStream           out_                  = System.out;
  private PrintStream           err_                  = System.err;
  private BufferedReader        in_                   = new BufferedReader(new InputStreamReader(System.in));
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

  public static void main(String[] argv) throws IOException
  {
    new ProbePod().run(argv);
  }

  private void run(String[] argv) throws IOException
  {
    CommandLineParser clp = new CommandLineParser((v) -> 
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
    
    clp.process(argv);

    if (name_ == null)
    {
      out_.print("Hostname: ");
      out_.flush();

      name_ = in_.readLine();
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

    out_.println("name=" + name_);
    out_.println("domain=" + domain_);

    out_.println();
    out_.println("Probing for Pod");
    out_.println("===============");
    
    for(int port : POD_PORTS)
    {
      probePod(port);
      
      if(podUrl_ != null)
        break;
    }
    
    if(keyManagerUrl_ == null)
    {
      out_.println("No podInfo, try to look for an in-cloud key manager...");
      
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

      out_.println("keyManagerName=" + keyManagerName);
      out_.println("keyManagerDomain=" + keyManagerDomain);
      
      
      out_.println();
      out_.println("Probing for API Keyauth");
      out_.println("=======================");
      
      keyAuthResponse_ = probeAuth("Key Auth", "/keyauth", keyManagerName, keyManagerDomain);
      
      if(keyAuthResponse_ != null)
      {
        keyAuthUrl_ = getUrl(keyAuthResponse_, TOKEN);
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
      out_.println("Found key manager at " + keyManagerUrl_);
      
      out_.println();
      out_.println("Probing for API Agent");
      out_.println("=====================");
      
      agentResponse_ = probeAgent(name_, domain_);
      
      agentApiUrl_ = getUrl(agentResponse_, null);
    }
    catch (MalformedURLException e)
    {
      out_.println("Invalid keyManagerUrl \"" + keyManagerUrl_ + "\" (" +
          e.getMessage() + ")");
      return;
    }

    out_.println();
    out_.println("Probe Successful");
    out_.println("================");
    
    String  format = "%-20s=%s\n";
    
    out_.format(format, "Pod URL", podUrl_);
    out_.format(format, "Pod ID", podId_);
    out_.format(format, "Key Manager URL", keyManagerUrl_);
    out_.format(format, "Session Auth URL", sessionAuthUrl_);
    out_.format(format, "Key Auth URL", keyAuthUrl_);
    out_.format(format, "Pod API URL", podApiUrl_);
    out_.format(format, "Agent API URL", agentApiUrl_);
    
    if(keystore_ != null)
    {
      out_.println();
      out_.format(format, "Client cert", keystore_);
      
      if(sessionInfoResult_.isFailed())
      {
        out_.println("This cert was not accepted for authentication");
      }
      else
      {
        out_.println("We authenticated as");
        for(String field : SessionInfoFields)
          out_.format(format, "userInfo." + field, sessionInfoResult_.getJcurlResponse().getTag(field));
      }
    }
    out_.println();
    out_.println("Root server certs:");
    for (X509Certificate cert : rootCerts_)
      out_.println(cert.getSubjectX500Principal().getName());

    try
    {
      KeyStore  trustStore      = KeyStore.getInstance(PKCS12);
      File      trustStoreFile  = File.createTempFile("server", ".truststore");
      int       certIndex=1;
      
      trustStore.load(null, null);
      
      for (X509Certificate cert : rootCerts_)
        trustStore.setCertificateEntry(String.valueOf(certIndex++), cert);
      
      try(OutputStream stream = new FileOutputStream(trustStoreFile))
      {
        trustStore.store(stream, trustStorePassword_.toCharArray());
      }
      
      out_.println("Truststore saved as " + trustStoreFile.getAbsolutePath());
      
      File      configStoreFile  = File.createTempFile("symphony", ".properties");
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
      
      out_.println("Config file saved as " + configStoreFile.getAbsolutePath());

    }
    catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
    {
      err_.println("Failed to save trust store");
      e.printStackTrace();
    }
    
    out_.println();
    out_.println("End server certs:");
    for (X509Certificate cert : serverCerts_)
      out_.println(cert.getSubjectX500Principal().getName());
  }

  private String getUrl(ScanResponse scanResponse, String token)
  {
    String url = UNKNOWN;
    
    if(scanResponse.getValidProbe() != null)
    {
      url = scanResponse.getValidProbe().getBaseUrl();
      
      if(token == null)
        out_.println("Found " + scanResponse.getName() + " endpoint at " + url);
      else if(getTag(scanResponse, token) != null)
        out_.println("Found " + scanResponse.getName() + " endpoint at " + url + " and we authenticated!");
      else
        out_.println("Found " + scanResponse.getName() + " endpoint at " + url + " but we failed to authenticate.");
    }
    else
    {
      List<Probe> certAuthProbes = scanResponse.getCertAuthProbes();
      
      if(certAuthProbes.size() == 1)
      {
        url = certAuthProbes.get(0).getBaseUrl() + Probably;
        out_.println("Found probable " + scanResponse.getName() + " endpoint at " + url);

      }
      else if(certAuthProbes.size() > 1)
      {
        for(Probe p : certAuthProbes)
          out_.println("Found possible " + scanResponse.getName() + " endpoint at " + p.getBaseUrl());
      }
      else
      {
        out_.println("Failed to find any " + scanResponse.getName() + " endpoint");
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
        out_.println("This is a non-SSL website");
      else
        out_.println("This is not a website");

      return;
    }

    probe = new Probe(name_, domain_, "", port,
        "/").setProbePath("/client/index.html", MIME_HTML);
    
    doProbe(probe);
    
    if (probe.isFailed())
    {
      out_.println("This is a website but not a Symphony Pod");
      return;
    }

    Probe healthCheckResult = new Probe(name_, domain_, "", port,
        "/").setProbePath("/webcontroller/HealthCheck", MIME_JSON);
    
    doProbe(healthCheckResult);

    if (healthCheckResult.isFailed())
    {
      out_.println("This looks quite like a Symphony Pod, but it isn't");
      return;
    }

    JsonNode healthJson = healthCheckResult.getJsonNode();

    if (healthJson == null)
    {
      out_.println("This looks a lot like a Symphony Pod, but it isn't");
      return;
    }

    if (!healthJson.isObject())
    {
      out_.println("This looks like a Symphony Pod, but the healthcheck returns something other than an object");
      out_.println(healthJson);
      return;
    }
    
    podUrl_ = "https://" + name_ + domain_ + (port == 443 ? "" : ":" + port);
    podHealthy_ = true;
    healthJson.fields().forEachRemaining((field) ->
    {
      if (!field.getValue().asBoolean())
      {
        out_.println(field.getKey() + " is UNHEALTHY");
        podHealthy_ = false;
      }
    });

    if (podHealthy_)
      out_.println("We found a Symphony Pod!");
    else
      out_.println("We found a Symphony Pod, but it's not feeling well");

    out_.println();
    out_.println("Probing for API Sessionauth");
    out_.println("===========================");
    
    sessionAuthResponse_ = probeAuth("Session Auth", "/sessionauth", name_, domain_);
    
    if(sessionAuthResponse_ != null)
    {
      sessionAuthUrl_ = getUrl(sessionAuthResponse_, TOKEN);
      
      Builder builder = getJCurl();
      
      for(String field : SessionInfoFields)
        builder.extract(field, field);
      
      cookieAuth(builder);
      
      sessionInfoResult_ = new Probe(name_, domain_, "", port,
          "/pod").setProbePath("/v2/sessioninfo", MIME_JSON);
      
      doProbe(builder.build(), sessionInfoResult_);
      
      out_.println("JSON=" + sessionInfoResult_.getJsonNode());
      
      if(sessionInfoResult_.isFailed())
      {
        out_.println("Failed to connect to POD API");
        podApiUrl_ = sessionInfoResult_.getBaseUrl() + Probably;
      }
      else
      {
        podApiUrl_ = sessionInfoResult_.getBaseUrl();
        
        out_.println("found pod API endpoint at " + podApiUrl_);
        
        for(String field : SessionInfoFields)
          out_.format("%-20s=%s\n", field, sessionInfoResult_.getJcurlResponse().getTag(field));
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
      out_.println("Can't do checkauth from this Pod.");
      return;
    }
    
    JsonNode checkAuthJson = checkAuthResult.getJsonNode();

    if (checkAuthJson == null)
    {
      out_.println("Invalid checkAuth response");
      return;
    }
    
    JsonNode km = checkAuthJson.get("keymanagerUrl");

    if (km == null)
    {
      out_.println("Invalid checkAuth response");
    }
    else
    {
      keyManagerUrl_ = km.asText();
    
      out_.println("keyManagerUrl is " + keyManagerUrl_);
    }
    
    builder = getJCurl();
    
    cookieAuth(builder);
    
    Probe podInfoResult = new Probe(name_, domain_, "", port,
        "/").setProbePath("/webcontroller/public/podInfo", MIME_JSON);
    
    doProbe(builder.build(), podInfoResult);

    if (podInfoResult.isFailed())
    {
      out_.println("Can't get podInfo from this Pod.");
      return;
    }

    JsonNode podInfoJson = podInfoResult.getJsonNode();

    if (podInfoJson == null)
    {
      out_.println("Invalid podInfo response");
      return;
    }
    
    JsonNode podInfoJsonData = podInfoJson.get("data");

    if (podInfoJsonData == null || !podInfoJsonData.isObject())
    {
      out_.println("This looks like a Symphony Pod, but the podInfo returns something unexpected");
      out_.println(podInfoJson);
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
//      out_.println("This looks like a pre-1.47 Agent.");
//      
//      probe.setValid(true);
//    }
//    else
//    {
//      JsonNode agentHealthJson = probe.getJsonNode();
//
//      if (agentHealthJson == null)
//      {
//        out_.println("This looks a lot like an Agent, but it isn't");
//        return;
//      }
//  
//      if (!agentHealthJson.isObject())
//      {
//        out_.println("This looks like an Agent, but the healthcheck returns something other than an object");
//        out_.println(agentHealthJson);
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
//            out_.println(field.getKey() + " is " + field.getValue().asText());
//            break;
//            
//          default:
//            if (field.getValue().asBoolean())
//            {
//              out_.println(field.getKey() + " is OK");
//            }
//            else
//            {
//              out_.println(field.getKey() + " is UNHEALTHY");
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
      out_.println("Probing " + probe.getProbeUrl() + "...");
      
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
        out_.println("Failed with HTTP status " + probe.getHttpStatus());
        return;
      }

      JCurl.Response jcr = probe.setJcurlResponse(jcurl.processResponse(connection));

      Certificate[] certs = jcr.getServerCertificates();

      X509Certificate cert = (X509Certificate) certs[certs.length - 1];
      rootCerts_.add(cert);

      out_.println("Root server cert " + cert.getSubjectX500Principal().getName());
      
      cert = (X509Certificate) certs[0];
      
      out_.println("End server cert " + cert.getSubjectX500Principal().getName());
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
        out_.println("Certificate auth required for " + probe.getHostNameAndPort());
      }
      else
        out_.println("SSL problem to " + probe.getHostNameAndPort());
    }
    catch (UnknownHostException e)
    {
      out_.println(probe.getHostName() + " is not a valid host name");
    }
    catch (SocketTimeoutException | ConnectException e)
    {
      out_.println("Cannot connect to " + probe.getHostNameAndPort());
    }
    catch (CertificateParsingException | IOException e)
    {
      e.printStackTrace();
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
      builder.header("sessionToken", token);
    
    token = getTag(keyAuthResponse_, TOKEN);
    
    if(token != null)
      builder.header("keyManagerToken", token);
    
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

      out_.println("response from " + url + " = " + connection.getResponseCode());

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
