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

import java.io.IOException;
import java.util.Set;

import org.symphonyoss.symphony.tools.rest.model.Agent;
import org.symphonyoss.symphony.tools.rest.model.IModelObject;
import org.symphonyoss.symphony.tools.rest.model.IModelObjectProvider;
import org.symphonyoss.symphony.tools.rest.model.IPod;
import org.symphonyoss.symphony.tools.rest.model.InvalidConfigException;
import org.symphonyoss.symphony.tools.rest.model.NoSuchObjectException;
import org.symphonyoss.symphony.tools.rest.model.Pod;

public interface IPodManager extends IModelObjectProvider
{

  IPod getPod(String hostName);

  IPod createOrUpdatePod(Pod.Builder podConfig, Agent.Builder agent) throws InvalidConfigException, IOException;

  Set<IPod> getAll();

  int getSize();

  /**
   * Return the default configuration name. If there are no Pods then
   * an exception is thrown, if there is no default and more than one
   * valid configuration then returns null.
   * 
   * @return The name of the default Pod configuration.
   * 
   * @throws NoSuchObjectException If there are no valid configurations.
   */
  String getDefaultPodName() throws NoSuchObjectException;

  void modelChanged();

  void modelObjectChanged(IModelObject modelObject);

  void modelObjectStructureChanged(IModelObject modelObject);

}