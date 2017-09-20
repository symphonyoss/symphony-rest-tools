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

package org.symphonyoss.symphony.tools.rest.model;

public class SimpleModelObject implements IModelObject
{
  private final IModelObject parent_;
  private final String       typeName_;
  private final String       name_;
  private final String       errorText_;

  public SimpleModelObject(IModelObject parent, String typeName, String name)
  {
    this(parent, typeName, name, null);
  }
  
  public SimpleModelObject(IModelObject parent, String typeName, String name, String errorText)
  {
    parent_ = parent;
    typeName_ = typeName;
    name_ = name;
    errorText_ = errorText;
  }

  @Override
  public boolean hasChildren()
  {
    return false;
  }

  @Override
  public IModelObject[] getChildren()
  {
    return null;
  }

  @Override
  public IModelObject getParent()
  {
    return parent_;
  }

  @Override
  public String getTypeName()
  {
    return typeName_;
  }

  @Override
  public String getName()
  {
    return name_;
  }

  @Override
  public String getErrorText()
  {
    return errorText_;
  }

}
