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

package org.symphonyoss.symphony.tools.rest.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.symphonyoss.symphony.tools.rest.SrtCommand;
import org.symphonyoss.symphony.tools.rest.util.command.CommandLineParserFault;
import org.symphonyoss.symphony.tools.rest.util.command.Flag;
import org.symphonyoss.symphony.tools.rest.util.home.IDefaultsProvider;
import org.symphonyoss.symphony.tools.rest.util.home.SrtCommandLineHome;

public class Console
{
  private final BufferedReader             in_;
  private final PrintWriter                out_;
  private final PrintWriter                err_;
  private IDefaultsProvider                defaultsProvider_;
  private CopyOnWriteArrayList<IObjective> objectives_ = new CopyOnWriteArrayList<>();
  
  public Console(InputStream in, OutputStream out, OutputStream err)
  {
    in_ = new BufferedReader(new InputStreamReader(in));
    out_ = new PrintWriter(out);
    err_ = new PrintWriter(err);
  }
  
  public Console(BufferedReader in, PrintWriter out, PrintWriter err)
  {
    in_ = in;
    out_ = out;
    err_ = err;
  }

  public IDefaultsProvider getDefaultsProvider()
  {
    return defaultsProvider_;
  }

  public void setDefaultsProvider(IDefaultsProvider defaultsProvider)
  {
    defaultsProvider_ = defaultsProvider;
  }

  public String  promptString(String prompt, String defaultValue)
  {
    String s = doPromptString(prompt + "[" + defaultValue + "]");
    
    return s.equals("") ? defaultValue : s.trim();
  }

  public String  promptString(String prompt)
  {
    return doPromptString(prompt).trim();
  }

  public String  doPromptString(String prompt)
  {
    out_.print(prompt);
    out_.print(": ");
    out_.flush();
    
    String line;

    try
    {
      line = in_.readLine();
    }
    catch (IOException e)
    {
      throw new ProgramFault("Error on console input.", e);
    }
    
    if(line == null)
      throw new ProgramFault("Unexpected end of file on console.");
    
    return line;
  }

  public boolean  promptBoolean(String prompt)
  {
    while(true)
    {
      String line = promptString(prompt + "[n]");
      
      if(line.length()==0)                return false;
      if("y".equalsIgnoreCase(line))      return true;
      if("yes".equalsIgnoreCase(line))    return true;
      if("n".equalsIgnoreCase(line))      return false;
      if("no".equalsIgnoreCase(line))     return false;
      
      out_.println("Invalid input: enter y or n.");
    }
  }

  public PrintWriter getOut()
  {
    return out_;
  }

  public PrintWriter getErr()
  {
    return err_;
  }

  public void printStackTrace(Throwable e)
  {
    e.printStackTrace(err_);
  }

  public void println()
  {
    out_.println();
  }

  public void println(String x)
  {
    out_.println(x);
  }

  public void println(Object x)
  {
    out_.println(x);
  }

  public PrintWriter printf(String format, Object... args)
  {
    return out_.printf(format, args);
  }

  public PrintWriter printf(Locale l, String format, Object... args)
  {
    return out_.printf(l, format, args);
  }
  
  public void error(String format, Object ...args)
  {
    // The Eclipse console does strange things all this flushing seems to work for all cases....
    out_.flush();
    err_.flush();
    err_.format(format, args);
    err_.flush();
  }

  public void close()
  {
    err_.close();
    out_.close();
    try
    {
      in_.close();
    }
    catch (IOException e)
    {
      throw new ProgramFault(e);
    }
  }

  public void flush()
  {
    err_.flush();
    out_.flush();
  }

  public boolean setParameters(SrtCommandLineHome parser, int interactiveCount)
  {
    boolean abort = false;
    boolean promptAll = false;
    
    println("Press RETURN to accept default values");
    println("Enter a space to clear the default value");
    println("Leading and trailing whitespace are deleted");
    
    switch(interactiveCount)
    {
      case 0:
        for(Flag flag : parser.getFlags())
        {
          if(flag.isRequired() && flag.getCount()==0)
          {
            error("A value for " + flag.getDescription() + " is required.\n");
            abort = true;
          }
        }
        break;
      
      case 2:
        promptAll = true;
        // Fall through
        
      default:
        for(Flag flag : parser.getFlags())
        {
          if(promptAll || flag.isRequired())
          {
            boolean doAgain;
            
            do
            {
              doAgain = false;
              
              String defaultValue = flag.getValue();
              
              if(defaultValue.length()==0)
                defaultValue = defaultsProvider_.getDefault(flag.getPrompt());
              
              String value = promptString(flag.getPrompt(), defaultValue);
              
              try
              {
                flag.set(value);
                if(flag.isRequired() && value.length()==0)
                {
                  error("A value is required\n");
                  doAgain=true;
                }
              }
              catch(CommandLineParserFault e)
              {
                doAgain = true;
              }
            } while(doAgain);
          }
        }
    }
    
    if(!abort)
    {
      for(Flag flag : parser.getFlags())
      {
        defaultsProvider_.setDefault(flag.getPrompt(), flag.getValue());
      }
    }
    
    return abort;
  }

  public void execute(SrtCommand srtCommand)
  {
    boolean abort = setParameters(srtCommand.getParser(), srtCommand.getInteractive().getCount());
    
    if(abort)
    {
      error("Aborted.");
    }
    else
    {
      srtCommand.doExecute();
    }
  }

  public void beginTask(String name, int totalWork)
  {
    // TODO Auto-generated method stub
    
  }

  public void done()
  {
    // TODO Auto-generated method stub
    
  }

  public boolean isCanceled()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void setTaskName(String name)
  {
    // TODO Auto-generated method stub
    
  }

  public void subTask(String name)
  {
    // TODO Auto-generated method stub
    
  }

  public void worked(int work)
  {
    // TODO Auto-generated method stub
    
  }
  
  public IObjective createObjective(String name)
  {
    IObjective objective = new Objective(name);
    
    objectives_.add(objective);
    
    return objective;
  }

  public void printObjectives()
  {
    for(IObjective objective : objectives_)
    {
      printf("%20s %s\n", objective.getLabel(), objective.getStatus());
    }
  }

  public CopyOnWriteArrayList<IObjective> getObjectives()
  {
    return objectives_;
  }
}
