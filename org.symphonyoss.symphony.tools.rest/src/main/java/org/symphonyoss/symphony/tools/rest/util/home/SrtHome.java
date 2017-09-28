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

package org.symphonyoss.symphony.tools.rest.util.home;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;

import org.symphonyoss.symphony.tools.rest.model.IModelListener;
import org.symphonyoss.symphony.tools.rest.model.IModelObject;
import org.symphonyoss.symphony.tools.rest.model.PodManager;
import org.symphonyoss.symphony.tools.rest.util.CommandLineParserFault;
import org.symphonyoss.symphony.tools.rest.util.Console;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;

public class SrtHome implements ISrtHome
{
  private final File       home_;
  private final String     setBy_;
  private final File       configDir_;
  private final File       sessionDir_;
  private final Console    console_;
  private final IPodManager podManager_;

  public SrtHome(Console console)
  {
    this(console, null, null);
  }
  
  public SrtHome(Console console, String homeStr, String setBy)
  {
    console_ = console;
    
    Builder builder = new Builder(homeStr, setBy);
    
    home_ = builder.builderHome_;
    setBy_ = builder.setBy_;
    
    console_.println(SRT_HOME + " set by " + setBy_);
    
    if (!home_.exists())
      throw new CommandLineParserFault(SRT_HOME + " \"" +
          home_.getAbsolutePath() + "\" set by " + setBy_ + " does not exist.");
    
    configDir_ = new File(home_, "config");
    configDir_.mkdirs();
    
    sessionDir_ = new File(home_, "session");
    sessionDir_.mkdirs();
    
    podManager_ = new PodManager(configDir_);
  }
  
  private class Builder
  {
    private File    builderHome_;
    private String  setBy_;
    
    public Builder(String homeStr, String setBy)
    {
      setHome(homeStr, setBy);
  
      if(builderHome_ == null)
      {
        setHome(System.getProperty(SRT_HOME), "System Property");
      }
      
      if(builderHome_ == null)
      {
        setHome(System.getenv(SRT_HOME), "Environment Variable");
      }
      
      if (builderHome_ == null)
      {
        builderHome_ = new File(new File(System.getProperty("user.home")), ".srt");
        setBy_ = "Default";
  
        if (!builderHome_.exists())
        {
          try
          {
            Path dir = builderHome_.toPath();
  
            Files.createDirectory(dir);
            Files.setPosixFilePermissions(dir, 
                EnumSet.of( PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE));
            
            console_.println("Default home area \"" +
                builderHome_.getAbsolutePath() + "\" created.");
          }
          catch (IOException e)
          {
            throw new CommandLineParserFault("Cannot create default home area \"" +
                builderHome_.getAbsolutePath() + "\"", e);
          }
        }
      }
    }
  
    private void setHome(String homeStr, String setBy)
    {
      if(homeStr != null)
      {
        homeStr = homeStr.trim();
        
        if(!homeStr.isEmpty())
        {
          builderHome_ = new File(homeStr);
          setBy_ = setBy;
        }
      }
    }
  }

  @Override
  public File getHome()
  {
    return home_;
  }

  public String getSetBy()
  {
    return setBy_;
  }
  
  @Override
  public File getConfigDir(String name)
  {
    return new File(configDir_, name);
  }

  @Override
  public void saveSessionToken(String hostName, String tokenName, String token)
  {
    File dir = new File(sessionDir_, hostName);
    
    dir.mkdirs();
    
    File file = new File(dir, tokenName);
    
    try(FileOutputStream fos = new FileOutputStream(file);
        PrintWriter p = new PrintWriter(fos);)
    {
      p.println(token);
    }
    catch (IOException e)
    {
      throw new ProgramFault("Unable to save session token " + file.getAbsolutePath(), e);
    }
  }

  @Override
  public IPodManager getPodManager()
  {
    return podManager_;
  }

  @Override
  public IModelObject[] getElements()
  {
    return podManager_.getElements();
  }

  @Override
  public void addListener(IModelListener listener)
  {
    podManager_.addListener(listener);
  }

  @Override
  public void removeListener(IModelListener listener)
  {
    podManager_.removeListener(listener);
  }

}
