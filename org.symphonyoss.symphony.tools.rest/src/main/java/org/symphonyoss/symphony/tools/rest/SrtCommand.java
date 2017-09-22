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

package org.symphonyoss.symphony.tools.rest;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.symphonyoss.symphony.jcurl.JCurl;
import org.symphonyoss.symphony.jcurl.JCurl.Builder;
import org.symphonyoss.symphony.tools.rest.model.NoSuchObjectException;
import org.symphonyoss.symphony.tools.rest.util.Console;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;
import org.symphonyoss.symphony.tools.rest.util.home.ISrtHome;
import org.symphonyoss.symphony.tools.rest.util.home.SrtCommandLineHome;

public abstract class SrtCommand extends Srt
{
  private final String                programName_;
  private final Console         console_;
  private String                name_;
  private String                domain_;
  private String                fqdn_;
  private int                   connectTimeoutMillis_ = 2000;
  private int                   readTimeoutMillis_    = 0;

  private ISrtHome              srtHome_;
  private String                keystore_;
  private String                storepass_            = "changeit";
  private String                storetype_            = DEFAULT_KEYSTORE_TYPE;
  
  /**
   * Create an instance with a Console connected to standard I/O.
   * 
   * @param argv Command line arguments.
   */
  public SrtCommand(String programName, String[] argv)
  {
    this(programName, new Console(System.in, System.out, System.err), argv);
  }
  
  /**
   * Create an instance with the given console.
   * 
   * @param console A Console for I/O.,
   * @param argv    Command line arguments.
   */
  public SrtCommand(String programName, Console console, String[] argv)
  {
    programName_ = programName;
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
  
  public SrtCommand(String programName, Console console, String name, ISrtHome srtHome)
  {
    programName_ = programName;
    console_ = console;
    name_ = name;
    srtHome_ = srtHome;
  }
  
  public void run()
  {
    if (name_ == null)
    {
      name_ = getDefaultName();
    }

    int i = name_.indexOf('.');

    if (i == -1)
      domain_ = DEFAULT_DOMAIN;
    else
    {
      domain_ = name_.substring(i);
      name_ = name_.substring(0, i);
    }

    fqdn_ = name_ + domain_;

    console_.println("name=" + name_);
    console_.println("domain=" + domain_);
    console_.println();
    
    try
    {
      execute();
    }
    catch(ProgramFault e)
    {
      getConsole().error("PROGRAM FAULT");
      e.printStackTrace(getConsole().getErr());
    }
    catch(Throwable e)
    {
      e.printStackTrace(getConsole().getErr());
    }
    finally
    {
      getConsole().close();
    }
  }
  
  protected String getDefaultName()
  {
    try
    {
      String name = getSrtHome().getPodManager().getDefaultPodName();
      
      if(name == null)
        name = promptForName();
      
      return name;
    }
    catch (NoSuchObjectException e)
    {
      throw new ProgramFault("There are no valid pod configurations. Try probe first.", e);
    }
  }

  protected String promptForName()
  {
    return console_.promptString("Hostname");
  }

  public abstract void execute();
  
  public URL createURL(String url)
  {
    try
    {
      return new URL(url);
    }
    catch (MalformedURLException e)
    {
      throw new ProgramFault(e);
    }
  }
  
  public URL createURL(String url, String path)
  {
    try
    {
      if(path.startsWith("/"))
      {
        while(url.endsWith("/"))
          url = url.substring(0, url.length() - 1);
      }
      return new URL(url + path);
    }
    catch (MalformedURLException e)
    {
      throw new ProgramFault(e);
    }
  }
  
  protected Builder getJCurl()
  {
    Builder builder = JCurl.builder()
        .trustAllHostnames(true)
        .trustAllCertificates(true)
        .header("User-Agent", programName_ + " / 0.1.0 https://github.com/bruceskingle/symphony-rest-tools");

    if (getConnectTimeoutMillis() > 0)
      builder.connectTimeout(getConnectTimeoutMillis());

    if (getReadTimeoutMillis() > 0)
      builder.readTimeout(getReadTimeoutMillis());

    if(getKeystore() != null)
    {
      builder.keystore(getKeystore());
      builder.storepass(getStorepass());
    
      if(getStoretype() != null)
        builder.storetype(getStoretype());
    }
    return builder;
  }

  public Console getConsole()
  {
    return console_;
  }

  public String getName()
  {
    return name_;
  }

  public String getDomain()
  {
    return domain_;
  }

  public String getFqdn()
  {
    return fqdn_;
  }

  public int getConnectTimeoutMillis()
  {
    return connectTimeoutMillis_;
  }

  public int getReadTimeoutMillis()
  {
    return readTimeoutMillis_;
  }

  public ISrtHome getSrtHome()
  {
    return srtHome_;
  }

  public String getKeystore()
  {
    return keystore_;
  }

  public String getStorepass()
  {
    return storepass_;
  }

  public String getStoretype()
  {
    return storetype_;
  }

  public void println()
  {
    console_.println();
  }

  public void println(String x)
  {
    console_.println(x);
  }

  public void println(Object x)
  {
    console_.println(x);
  }

  public PrintWriter printf(String format, Object... args)
  {
    return console_.printf(format, args);
  }
}
