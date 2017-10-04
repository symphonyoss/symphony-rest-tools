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

package org.symphonyoss.symphony.tools.rest.command;

import java.io.IOException;

import org.symphonyoss.symphony.tools.rest.SrtCommand;
import org.symphonyoss.symphony.tools.rest.model.IPod;
import org.symphonyoss.symphony.tools.rest.util.Console;
import org.symphonyoss.symphony.tools.rest.util.home.ISrtHome;

public class DeletePod extends SrtCommand
{
  private static final String      PROGRAM_NAME                = "DeletePod";

  private IPod                     pod_;
  
  public static void main(String[] argv) throws IOException
  {
    new DeletePod(argv).run();
  }

  public DeletePod(Console console, ISrtHome srtHome)
  {
    super(PROGRAM_NAME, console, srtHome);
  }

  public DeletePod(String[] argv)
  {
    super(PROGRAM_NAME, argv);
  }
  
  @Override
  protected void init()
  {
    super.init();

    withHostName(true);
  }

  @Override
  public void execute()
  {
    pod_ = getSrtHome().getPodManager().getPod(getFqdn());

    if(pod_ == null)
    {
      getConsole().flush();
      getConsole().error(getFqdn() + " is not a known pod.");
      return;
    }

    int totalWork = 1;
    getConsole().beginTask("Deleting " + getFqdn(), totalWork);

    
    try
    {
      pod_.delete();
      getConsole().println("Pod deleted.");
    }
    catch (IOException e)
    {
      getConsole().error("Failed to delete: %s", e.getMessage());
      e.printStackTrace(getConsole().getErr());
    }
    
    getConsole().worked(1);
  }
}
