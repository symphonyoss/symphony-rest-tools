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

public class Console
{
  private final BufferedReader in_;
  private final PrintWriter    out_;
  private final PrintWriter    err_;
  
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

  public String  promptString(String prompt)
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
    
    line = line.trim();
    
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
  
  public void error(String x)
  {
    err_.println(x);
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
}
