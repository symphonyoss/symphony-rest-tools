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

import org.symphonyoss.symphony.tools.rest.util.CommandLineParser;
import org.symphonyoss.symphony.tools.rest.util.Flag;
import org.symphonyoss.symphony.tools.rest.util.typeutils.ISetter;

public class SrtCommandLineHome extends CommandLineParser implements ISrtHome
{
  private String  home_;
  private SrtHome srtHome_;

  public SrtCommandLineHome()
  {
    init();
  }

  public SrtCommandLineHome(ISetter<String> argSetter)
  {
    super(argSetter);
    init();
  }

  private void init()
  {
    addFlag((v) -> home_ =v, SRT_HOME);
  }

  @Override
  public void process(String[] argv)
  {
    super.process(argv);
    srtHome_ = new SrtHome(home_, "Command Line Flag");
  }

  @Override
  public File getHome()
  {
    return srtHome_.getHome();
  }

  @Override
  public SrtCommandLineHome addFlag(ISetter<String> setter, String... names)
  {
    super.addFlag(setter, names);
    return this;
  }

  @Override
  public SrtCommandLineHome addFlag(Flag flag, String... names)
  {
    super.addFlag(flag, names);
    return this;
  }

  @Override
  public File getConfigDir(String name)
  {
    return srtHome_.getConfigDir(name);
  }

  @Override
  public void saveSessionToken(String hostName, String tokenName, String token)
  {
    srtHome_.saveSessionToken(hostName, tokenName, token);
  }

  
}
