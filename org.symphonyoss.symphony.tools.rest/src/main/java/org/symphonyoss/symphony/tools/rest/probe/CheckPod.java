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

package org.symphonyoss.symphony.tools.rest.probe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateParsingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.symphonyoss.symphony.jcurl.JCurl;
import org.symphonyoss.symphony.jcurl.JCurl.Response;
import org.symphonyoss.symphony.tools.rest.SrtCommand;
import org.symphonyoss.symphony.tools.rest.model.IComponent;
import org.symphonyoss.symphony.tools.rest.model.IPod;
import org.symphonyoss.symphony.tools.rest.model.IVirtualModelObject;
import org.symphonyoss.symphony.tools.rest.model.VirtualModelObject;
import org.symphonyoss.symphony.tools.rest.util.Console;
import org.symphonyoss.symphony.tools.rest.util.home.ISrtHome;

import com.fasterxml.jackson.databind.JsonNode;

public class CheckPod extends SrtCommand
{
  private static final String      PROGRAM_NAME                = "CheckPod";

  private IPod                     pod_;
  private boolean                  structureChange_;
  private Set<IVirtualModelObject> changedComponents_          = new HashSet<>();
//  private Set<IVirtualModelObject> structureChangedComponents_ = new HashSet<>();
  
  public static void main(String[] argv) throws IOException
  {
    new CheckPod(argv).run();
  }

  public CheckPod(Console console, String name, ISrtHome srtHome)
  {
    super(PROGRAM_NAME, console, name, srtHome);
  }

  public CheckPod(Console console, String[] argv)
  {
    super(PROGRAM_NAME, console, argv);
  }

  public CheckPod(String[] argv)
  {
    super(PROGRAM_NAME, argv);
  }
  


  @Override
  public void execute()
  {
    pod_ = getSrtHome().getPodManager().getPod(getFqdn());

    if(pod_ == null)
    {
      getConsole().error(getFqdn() + " is not a known pod.");
      return;
    }
    
    println("Pod Configuration");
    println("=================");
    
    pod_.print(getConsole().getOut());

    println();
    
    probePod();
  }

  

  private void probePod()
  {
    try
    {
      println("Checking Pod");
      println("=============");
      
      pod_.resetStatus();
      
      URL url = createURL(pod_.getPodConfig().getPodUrl(),
          POD_HEALTHCHECK_PATH);
      
      JCurl jCurl = getJCurl().build();
      HttpURLConnection connection = jCurl.connect(url);
      
      
  
      if(connection.getResponseCode() != 200)
      {
        println("Healthcheck failed.");
        pod_.setComponentStatus(false, "Error " + connection.getResponseCode());
        return;
      }
  
      Response response = jCurl.processResponse(connection);
      JsonNode healthJson = response.getJsonNode();
  
      if (healthJson == null)
      {
        println("This looks a lot like a Symphony Pod, but it isn't");
        return;
      }
  
      if (!healthJson.isObject())
      {
        println("This looks like a Symphony Pod, but the healthcheck returns something other than an object");
        println(healthJson);
        return;
      }
      
      Iterator<String> it = healthJson.fieldNames();
      
      while(it.hasNext())
      {
        String name = it.next();
        boolean healthy = healthJson.get(name).asBoolean();
        
        printf("%30s %s\n", name, healthy);
        
        pod_.getComponent(name, 
            (parent, componentName) -> 
            {
              VirtualModelObject component = new VirtualModelObject(pod_, IComponent.GENERIC_COMPONENT, componentName);
              
              structureChange_ = true;
              return component;
            },
            (existingComponent) -> changedComponents_.add(existingComponent)
        ).setComponentStatus(healthy, "");
      }
      
      if(structureChange_)
      {
        pod_.getManager().modelObjectStructureChanged(pod_);
      }
      else
      {
//        for(IVirtualModelObject component : structureChangedComponents_)
//        {
//          changedComponents_.remove(component);
//          pod_.getManager().modelObjectStructureChanged(component);
//        }
        
        for(IVirtualModelObject component : changedComponents_)
        {
          pod_.getManager().modelObjectChanged(component);
        }
      }
    }
    catch(IOException | CertificateParsingException e)
    {
      e.printStackTrace(getConsole().getErr());
    }
  }
}
