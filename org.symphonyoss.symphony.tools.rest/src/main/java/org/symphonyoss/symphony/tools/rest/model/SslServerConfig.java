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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.symphonyoss.symphony.tools.rest.util.ProgramFault;

@Immutable
public class SslServerConfig extends Config implements ISslServerConfig
{
  private static final String   PKCS12                = "pkcs12";
  private static final String DOT_TRUSTSTORE = ".truststore";

  private static final String TRUSTSTORE_FILE     = "truststore.file";
  private static final String TRUSTSTORE_PASSWORD = "truststore.password";

  /* package */ String        trustStorePassword_ = "changeit";
  /* package */ File          trustStoreFile_;
  /* package */ Set<X509Certificate> trustCerts_       = new HashSet<>();


  public SslServerConfig()
  {}
  
  public SslServerConfig(SslServerConfig other)
  {
    super(other);
    
    trustStorePassword_ = other.trustStorePassword_;
    trustStoreFile_     = other.trustStoreFile_;
    trustCerts_.addAll(other.trustCerts_);
  }

  @Override
  protected void loadFromProperties(Properties props)
  {
    super.loadFromProperties(props);
    
    trustStoreFile_ = new File(props.getProperty(TRUSTSTORE_FILE));
    trustStorePassword_ = props.getProperty(TRUSTSTORE_PASSWORD);
  }

  @Override
  public void setProperties(Properties  prop)
  {
    super.setProperties(prop);
    
    setIfNotNull(prop, TRUSTSTORE_FILE, trustStoreFile_.getAbsolutePath());
    setIfNotNull(prop, TRUSTSTORE_PASSWORD, trustStorePassword_);
  }
  
  @Override
  public void printFields(PrintWriter out)
  {
    out.printf(F, TRUSTSTORE_FILE, trustStoreFile_.getAbsolutePath());
    out.printf(F, TRUSTSTORE_PASSWORD, trustStorePassword_);
  }

  @Override
  public String getTrustStorePassword()
  {
    return trustStorePassword_;
  }

  @Override
  public File getTrustStoreFile()
  {
    return trustStoreFile_;
  }


  @Override
  public Set<X509Certificate>  getTrustCerts()
  {
    return new HashSet<X509Certificate>(trustCerts_);
  }

  @Override
  public void load(File configDir, String fileName) throws NoSuchObjectException
  {
    super.load(configDir, fileName);
    
    try
    {
      KeyStore  trustStore      = KeyStore.getInstance(PKCS12);
      File      trustStoreFile  = new File(configDir, fileName + DOT_TRUSTSTORE);
      char[]    password        = trustStorePassword_.toCharArray();
      
      try(InputStream stream = new FileInputStream(trustStoreFile))
      {
        trustStore.load(stream, password);
        
        Enumeration<String> en = trustStore.aliases();
        
        while(en.hasMoreElements())
        {
          Certificate cert = trustStore.getCertificate(en.nextElement());
          
          if(cert instanceof X509Certificate)
            trustCerts_.add((X509Certificate) cert);
          else
            throw new ProgramFault("Unexpected certificate type " + cert.getClass().getName());
        }
      }
    }
    catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
    {
      addError("Unable to read truststore: " + e.getMessage());
    }
  }
  
  @Override
  protected void doStore(File configDir, String fileName)
  {
    trustStoreFile_  = new File(configDir, fileName + DOT_TRUSTSTORE);
    
    try
    {
      KeyStore  trustStore      = KeyStore.getInstance(PKCS12);
      
      char[]    password        = trustStorePassword_.toCharArray();
      int       certIndex       = 1;
      
      trustStore.load(null, null);
      
      for (X509Certificate cert : trustCerts_)
        trustStore.setCertificateEntry(String.valueOf(certIndex++), cert);
      
      try(OutputStream stream = new FileOutputStream(trustStoreFile_))
      {
        trustStore.store(stream, password);
      }
    }
    catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
    {
      throw new ProgramFault(e);
    }
    
    super.doStore(configDir, fileName);
  }

  @Override
  public String getTypeName()
  {
    return "SSL Server";
  }
}
