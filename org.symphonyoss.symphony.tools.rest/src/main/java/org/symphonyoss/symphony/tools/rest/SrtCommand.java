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

import java.net.MalformedURLException;
import java.net.URL;

import org.symphonyoss.symphony.jcurl.JCurl;
import org.symphonyoss.symphony.jcurl.JCurl.Builder;
import org.symphonyoss.symphony.tools.rest.console.Console;
import org.symphonyoss.symphony.tools.rest.console.ConsoleDelegate;
import org.symphonyoss.symphony.tools.rest.console.IConsole;
import org.symphonyoss.symphony.tools.rest.model.IPod;
import org.symphonyoss.symphony.tools.rest.util.IObjective;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;
import org.symphonyoss.symphony.tools.rest.util.command.Flag;
import org.symphonyoss.symphony.tools.rest.util.command.Switch;
import org.symphonyoss.symphony.tools.rest.util.home.ISrtHome;
import org.symphonyoss.symphony.tools.rest.util.home.SrtCommandLineHome;

public abstract class SrtCommand extends ConsoleDelegate
{
  private final String  programName_;
  private String        name_;
  private String        domain_;
  private String        fqdn_;
  private int           connectTimeoutMillis_ = 2000;
  private int           readTimeoutMillis_    = 0;

  private ISrtHome      srtHome_;
  private String        keystore_             = "";
  private String        storepass_            = "changeit";
  private String        storetype_            = Srt.DEFAULT_KEYSTORE_TYPE;
  private String        truststore_           = "";
  private String        trustpass_            = "changeit";
  private String        trusttype_            = Srt.DEFAULT_TRUSTSTORE_TYPE;
  //private List<>
  private SrtCommandLineHome parser_;
  
  protected final  Switch verbose_ = new Switch('v', "Verbose", "Set verbose Mode", 3);
  protected final  Switch interactive_ = new Switch('i', "Interactive", "Set interactive Mode", 2);
  private boolean withHostName_;
  
  /**
   * Create an instance with a Console connected to standard I/O.
   * 
   * @param argv Command line arguments.
   */
  public SrtCommand(String programName, String[] argv)
  {
    this(programName, new Console(System.in, System.out, System.err), null);
    
    parser_.process(argv);
  }
  
  public SrtCommand(String programName, IConsole console, ISrtHome srtHome)
  {
    super(console);
    programName_ = programName;
    
    parser_ = new SrtCommandLineHome(programName)
        .withSwitch(verbose_)
        .withSwitch(interactive_);
    
    init();
    
    srtHome_ = srtHome == null ? parser_.createSrtHome(getConsole()) : srtHome;
  }
  
  protected void withHostName(boolean required)
  {
    parser_.withFlag(new Flag<String>("Host Name", String.class, (v) -> name_ = v)
        .withRequired(required)
        .withSelectionType(IPod.class))
        ;
    withHostName_ = true;
  }

  protected void withKeystore(boolean required)
  {
    parser_
      .withFlag(new Flag<String>("Keystore File Name", String.class, (v) -> keystore_ = v)
        .withName("keystore")
        .withRequired(required))
      .withFlag(new Flag<String>("Keystore Type", String.class, (v) -> storetype_ = v, () ->
      getStoreTypeFromName(truststore_, Srt.DEFAULT_KEYSTORE_TYPE))
          .withName("storetype"))
      .withFlag(new Flag<String>("Keystore Password", String.class, (v) -> storepass_ = v, () -> "changeit")
          .withName("storepass"))
    ;
  }
  
  protected void withTruststore(boolean required)
  {
    parser_
      .withFlag(new Flag<String>("Truststore File Name", String.class, (v) -> truststore_ = v)
         .withName("truststore")
        .withRequired(required))
      .withFlag(new Flag<String>("Truststore Type", String.class, (v) -> trusttype_ = v, () ->
      getStoreTypeFromName(truststore_, Srt.DEFAULT_TRUSTSTORE_TYPE))
          .withName("trusttype"))
      .withFlag(new Flag<String>("Truststore Type", String.class, (v) -> trustpass_ = v, () -> "changeit")
          .withName("trustpass"))
    ;
  }
  
  private String getStoreTypeFromName(String fileName, String defaultValue)
  {
    int i = fileName.lastIndexOf('.');
    
    if(i>0)
    {
      String suffix = fileName.substring(i).toLowerCase();
      
      switch(suffix)
      {
        case ".p12":
        case ".pkcs12":
          return "pkcs12";
          
        case "jks":
          return "jks";
      }
    }
    return defaultValue;
  }
  
  protected void init()
  {
    // Sub-classes may override but should call super.init();
  }

  public void run()
  {
    if(name_ == null)
    {
      name_ = getDefaultName();
    }
    
    execute(this);
    println();
    getErr().println();
    
    flush();
   }
   
  public void doExecute()
  {
    if(withHostName_)
    {
      int i = name_.indexOf('.');
  
      if (i == -1)
        domain_ = Srt.DEFAULT_DOMAIN;
      else
      {
        domain_ = name_.substring(i);
        name_ = name_.substring(0, i);
      }
  
      fqdn_ = name_ + domain_;
  
      printfln("name=" + name_);
      printfln("domain=" + domain_);
      println();
    }
    
    try
    {
      execute();
    }
    catch(ProgramFault e)
    {
      error(e, "Command \"%s\" terminated unexpectedly PROGRAM FAULT.", name_);
    }
    catch(Throwable e)
    {
      error(e, "Command \"%s\" terminated unexpectedly.", name_);
    }
    finally
    {
      if(getConsole().hasObjectives())
      {
        title("Objectives");
        for(IObjective objective : getConsole().getObjectives())
        {
          printfln("%-20s %-10s %s", objective.getLabel(), objective.getComponentStatus(), objective.getComponentStatusMessage());
        }
      }
      
      getConsole().flush();
    }
  }
  
  protected String getDefaultName()
  {
    String name = getSrtHome().getPodManager().getDefaultPodName();
      
    return name;
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
  
  public URL createURL(URL urlp, String path)
  {
    try
    {
      String url = urlp.toString();
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
        .extract(Srt.TOKEN, Srt.TOKEN)  // force JCurl to parse JSON
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
    
    if(getTruststore() != null)
    {
      builder.truststore(getTruststore());
      builder.trustpass(getTrustpass());
    
      if(getTrusttype() != null)
        builder.storetype(getTrusttype());
    }
    
    if(verbose_.getCount()>0)
    {
      builder.verbosity(verbose_.getCount());
    }
    return builder;
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

  public String getTruststore()
  {
    return truststore_;
  }

  public String getTrustpass()
  {
    return trustpass_;
  }

  public String getTrusttype()
  {
    return trusttype_;
  }

  public String getProgramName()
  {
    return programName_;
  }

  public SrtCommandLineHome getParser()
  {
    return parser_;
  }

  public Switch getVerbose()
  {
    return verbose_;
  }

  public Switch getInteractive()
  {
    return interactive_;
  }
}
