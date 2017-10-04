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

package org.symphonyoss.symphony.tools.rest.util.command;

import java.util.ArrayList;
import java.util.List;

import org.symphonyoss.symphony.tools.rest.model.IModelObject;
import org.symphonyoss.symphony.tools.rest.util.typeutils.ISetter;
import org.symphonyoss.symphony.tools.rest.util.typeutils.TypeConverterFactory;

public class Flag
{
  private final String          prompt_;
  private final ISetter<String> setter_;

  private String                help_;
  private List<String>          names_ = new ArrayList<>();
  private boolean               required_;
  private boolean               duplicatesAllowed_;
  private int                   count_;
  private String                value_ = "";
  private Class<? extends IModelObject> selectionType_;
  
  public Flag(String prompt, ISetter<String> setter)
  {
    help_ = prompt_ = prompt;
    setter_ = new ISetter<String>()
    {
      @Override
      public void set(String value)
      {
        setter.set(value);
        value_ = value;
      }
    };
  }
  
  public <T> Flag(String prompt, Class<T> type, ISetter<T> setter)
  {
    help_ = prompt_ = prompt;
    setter_ = new ISetter<String>()
    {
      @Override
      public void set(String value)
      {
        setter.set(TypeConverterFactory.getConverter(String.class, type)
            .convert(value));
        value_ = value;
      }
    };
  }
  
  public Flag withSelectionType(Class<? extends IModelObject> selectionType)
  {
    selectionType_ = selectionType;
    return this;
  }


  public Class<? extends IModelObject> getSelectionType()
  {
    return selectionType_;
  }
  
  public boolean isRequired()
  {
    return required_;
  }

  public boolean isDuplicatesAllowed()
  {
    return duplicatesAllowed_;
  }

  public Flag withRequired(boolean required)
  {
    required_ = required;
    
    return this;
  }
  
  public Flag withDuplicatesAllowed(boolean duplicatesAllowed)
  {
    duplicatesAllowed_ = duplicatesAllowed;
    
    return this;
  }

  public Flag  withName(String name)
  {
    names_.add(name);
    
    return this;
  }
  
  public String getHelp()
  {
    return help_;
  }

  public Flag withHelp(String help)
  {
    help_ = help;
    return this;
  }

  public String getPrompt()
  {
    return prompt_;
  }

  public boolean checkCount()
  {
    return duplicatesAllowed_ || count_==0;
  }

  public int getCount()
  {
    return count_;
  }

  public List<String> getNames()
  {
    return names_;
  }

  public String getDescription()
  {
    if(names_.isEmpty())
    {
      return prompt_ + " parameter";
    }
    else
    {
      String name = names_.get(0);
      
      return "flag " + (name.length() > 1 ? "--" : "-") + name + " (" + prompt_ + ")";
    }
  }

  public String getValue()
  {
    return value_;
  }

  public void set(String value)
  {
    count_++;
    setter_.set(value);
  }
}
