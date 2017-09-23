/*
 * Copyright 2017 Symphony Communication Services, LLC.
 *
 * All Rights Reserved
 */

package org.symphonyoss.symphony.tools.rest.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.symphonyoss.symphony.tools.rest.util.IVisitor;
import org.symphonyoss.symphony.tools.rest.util.typeutils.ISetter;

public class ModelObjectContainer extends ModelObject implements IComponentContainer
{
  private Map<String, IVirtualModelObject> componentMap_     = new HashMap<>();
  
  public ModelObjectContainer(IVirtualModelObject parent, Config config)
  {
    super(parent, config);
  }

  @Override
  public IVirtualModelObject getComponent(String name)
  {
    return getComponent(name,
        (parent, componentName) -> new VirtualModelObject(this, GENERIC_COMPONENT, componentName),
        null);
  }

  @Override
  public IVirtualModelObject getComponent(String name,
      IModelObjectConstructor<? extends IVirtualModelObject> constructor,
      @Nullable ISetter<IVirtualModelObject> setExisting)
  {
    while(name.startsWith("_"))
      name = name.substring(1);
    
    while(name.endsWith("_"))
      name = name.substring(0, name.length() - 1);
    
    synchronized (componentMap_)
    {
      IVirtualModelObject component = componentMap_.get(name);
      
      if(component == null)
      {
        IVirtualModelObject vmo = constructor.newInstance(this, name);
        
        componentMap_.put(name, vmo);
        
        addChild(vmo);
        
        return vmo;
      }
      
      setExisting.set(component);
      return component;
    }
  }
  
  public void visitAllComponents(IVisitor<IVirtualModelObject> visitor)
  {
    synchronized (componentMap_)
    {
      for(IVirtualModelObject component : componentMap_.values())
        visitor.visit(component);
    }
  }
}
