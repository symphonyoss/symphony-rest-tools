/*
 *
 *
 * Copyright 2017 The Symphony Software Foundation
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

import java.util.ArrayList;
import java.util.List;

import org.symphonyoss.symphony.tools.rest.util.typeutils.ISetter;
import org.symphonyoss.symphony.tools.rest.util.typeutils.TypeConverterFactory;

public class Flag
{
  private List<ISetter<String>>        setterList_ = new ArrayList<>();
  private boolean                      duplicatesAllowed_;
  private int                          count_;
  
  public Flag()
  {
  }
  
  public Flag(ISetter<String> setter)
  {
    addSetter(String.class, setter);
  }
  
  public <T> Flag(Class<T> type, ISetter<T> setter)
  {
    addSetter(type, setter);
  }
  
  public boolean isDuplicatesAllowed()
  {
    return duplicatesAllowed_;
  }

  public Flag setDuplicatesAllowed(boolean duplicatesAllowed)
  {
    duplicatesAllowed_ = duplicatesAllowed;
    
    return this;
  }

  public <T> Flag  addSetter(Class<T> type, ISetter<T> setter)
  {
    @SuppressWarnings("unchecked")
    ISetter<String> stringSetter = type == String.class ? (ISetter<String>) setter :
      new ISetter<String>()
      {
        @Override
        public void set(String value)
        {
          setter.set(TypeConverterFactory.getConverter(String.class, type)
              .convert(value));
        }
      };
    
    setterList_.add(stringSetter);

    return this;
  }
  
  public boolean checkCount()
  {
    count_++;
    
    return duplicatesAllowed_ || count_==1;
  }

  public List<ISetter<String>> getSetterList()
  {
    return setterList_;
  }
  
  
}
