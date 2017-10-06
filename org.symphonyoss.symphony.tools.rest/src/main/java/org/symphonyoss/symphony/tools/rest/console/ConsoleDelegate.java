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

package org.symphonyoss.symphony.tools.rest.console;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import org.symphonyoss.symphony.tools.rest.SrtCommand;
import org.symphonyoss.symphony.tools.rest.util.IObjective;
import org.symphonyoss.symphony.tools.rest.util.home.IDefaultsProvider;

/**
 * A complete delegation of IConsole.
 * 
 * Used as a super class for SrtCommand.
 * 
 * @author Bruce Skingle
 *
 */
public class ConsoleDelegate implements IConsole
{
  private final IConsole  console_;

  public ConsoleDelegate(IConsole console)
  {
    console_ = console;
  }

  public IConsole getConsole()
  {
    return console_;
  }

  @Override
  public String promptString(String prompt, String defaultValue)
  {
    return console_.promptString(prompt, defaultValue);
  }

  @Override
  public String promptString(String prompt)
  {
    return console_.promptString(prompt);
  }

  @Override
  public boolean promptBoolean(String prompt)
  {
    return console_.promptBoolean(prompt);
  }

  @Override
  public void println()
  {
    console_.println();
  }

  @Override
  public String println(Object object)
  {
    return console_.println(object);
  }

  @Override
  public String printf(String format, Object... args)
  {
    return console_.printf(format, args);
  }

  @Override
  public String printf(Locale l, String format, Object... args)
  {
    return console_.printf(l, format, args);
  }

  @Override
  public String printfln(String format, Object... args)
  {
    return console_.printfln(format, args);
  }

  @Override
  public String printfln(Locale l, String format, Object... args)
  {
    return console_.printfln(l, format, args);
  }

  @Override
  public String error(Throwable cause, Locale l, String format, Object... args)
  {
    return console_.error(cause, l, format, args);
  }

  @Override
  public String error(Locale l, String format, Object... args)
  {
    return console_.error(l, format, args);
  }

  @Override
  public String error(Throwable cause, String format, Object... args)
  {
    return console_.error(cause, format, args);
  }

  @Override
  public String error(String format, Object... args)
  {
    return console_.error(format, args);
  }

  @Override
  public void close()
  {
    console_.close();
  }

  @Override
  public void flush()
  {
    console_.flush();
  }

  @Override
  public void execute(SrtCommand srtCommand)
  {
    console_.execute(srtCommand);
  }

  @Override
  public void taskDone()
  {
    console_.taskDone();
  }

  @Override
  public boolean isTaskCanceled()
  {
    return console_.isTaskCanceled();
  }

  @Override
  public String title(Locale l, String format, Object... args)
  {
    return console_.title(l, format, args);
  }

  @Override
  public String title(String format, Object... args)
  {
    return console_.title(format, args);
  }

  @Override
  public String beginTask(int totalWork, String format, Object... args)
  {
    return console_.beginTask(totalWork, format, args);
  }

  @Override
  public String setTaskName(String format, Object... args)
  {
    return console_.setTaskName(format, args);
  }

  @Override
  public String beginSubTask(String format, Object... args)
  {
    return console_.beginSubTask(format, args);
  }

  @Override
  public boolean taskWorked(int work)
  {
    return console_.taskWorked(work);
  }

  @Override
  public IObjective createObjective(String name)
  {
    return console_.createObjective(name);
  }

  @Override
  public Iterable<IObjective> getObjectives()
  {
    return console_.getObjectives();
  }

  @Override
  public Collection<IObjective> copyObjectives()
  {
    return console_.copyObjectives();
  }

  @Override
  public boolean hasObjectives()
  {
    return console_.hasObjectives();
  }

  @Override
  public void setDefaultsProvider(IDefaultsProvider defaultsProvider)
  {
    console_.setDefaultsProvider(defaultsProvider);
  }

  @Override
  public PrintWriter getOut()
  {
    return console_.getOut();
  }

  @Override
  public PrintWriter getErr()
  {
    return console_.getErr();
  }
}
