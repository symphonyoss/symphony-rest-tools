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

import java.util.HashMap;
import java.util.Map;

import org.symphonyoss.symphony.tools.rest.util.typeutils.ISetter;

public class CommandLineParser
{
  private Map<String, Flag> flagMap_ = new HashMap<>();
  private ISetter<String>   argSetter_;
  private int               argc_;
  
  public CommandLineParser()
  {
    this(null);
  }
  
  public CommandLineParser(ISetter<String> argSetter)
  {
    argSetter_ = argSetter;
  }
  
  public CommandLineParser  addFlag(ISetter<String> setter, String ...names)
  {
    Flag flag = new Flag(setter);
    
    for(String name : names)
      flagMap_.put(name, flag);

    return this;
  }
  
  public CommandLineParser  addFlag(Flag flag, String ...names)
  {
    for(String name : names)
      flagMap_.put(name, flag);

    return this;
  }
  
  public void process(String[] argv)
  {
    while(argc_ < argv.length)
    {
      String arg = argv[argc_++];
      
      if(arg.startsWith("-"))
      {
        String flagName = arg.substring(1);
        Flag flag = flagMap_.get(flagName);

        if(flag == null)
        {
          throw new CommandLineParserFault("Unrecognized flag \"" + argv + "\"");
        }
        
        for(ISetter<String> setter : flag.getSetterList())
        {
          if(argc_ < argv.length)
            setter.set(argv[argc_++]);
          else
            throw new CommandLineParserFault("Insufficient values for flag \"" + argv + "\"");
        }
      }
      else
      {
        if(argSetter_ == null)
        {
          throw new CommandLineParserFault("Invalid argument \"" + argv + "\"");
        }
        
        argSetter_.set(arg);
      }
    }
  }
}
