/*
 * Copyright 2017 Symphony Communication Services, LLC.
 *
 * All Rights Reserved
 */

package org.symphonyoss.symphony.tools.rest.util.command;

//import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;
import org.symphonyoss.symphony.tools.rest.util.typeutils.ISetter;

public class CommandLineParserTest
{
  private ISetter<String> sink_ = (v) -> v=null;
  
  private Flag<String> flag_ = new Flag<String>("Help me", String.class, sink_).withName("h");
  private Flag<String> def_ = new Flag<String>("Prompt", String.class, sink_, () -> "defaultValue").withName("def");
  private Switch switch_ = new Switch('h', "Help", "Show help");
  private CommandLineParser clp_ = new CommandLineParser("testCommand")
      .withSwitch(switch_)
      .withSwitch(new Switch('q', "Quiet", "Quiet Mode."))
      .withFlag(new Flag<String>("Keystore File Name", String.class, sink_).withName("k").withName("keystore"))
      .withFlag(new Flag<String>("FileName", String.class, sink_))
      .withFlag(def_)
      ;
  
  @Test
  public void testArgs0()
  {
    clp_.process(new String[0]);
    
    assertEquals("defaultValue", def_.getValue());
  }
  
  @Test
  public void testArgs1()
  {
    clp_.process(new String[] {"-q"});
  }
  
  @Test(expected=CommandLineParserFault.class)
  public void testArgs2()
  {
    clp_.process(new String[] {"-qwz"});
  }
  
  @Test
  public void testArgs3()
  {
    clp_.process(new String[] {"-q", "aFile"});
  }
  
  @Test(expected=CommandLineParserFault.class)
  public void testArgs4()
  {
    clp_.process(new String[] {"-q", "aFile", "anotherFile"});
  }
  
  @Test(expected=ProgramFault.class)
  public void testDuplicate()
  {
    new CommandLineParser("testCommand").withFlag(flag_).withFlag(flag_);
  }
  
  @Test(expected=ProgramFault.class)
  public void testClash1()
  {
    new CommandLineParser("testCommand").withFlag(flag_).withSwitch(switch_);
  }
  
  @Test(expected=ProgramFault.class)
  public void testClash2()
  {
    new CommandLineParser("testCommand").withSwitch(switch_).withFlag(flag_);
  }
  
  @Test(expected=ProgramFault.class)
  public void testMultiArgs()
  {
    new CommandLineParser("testCommand").withFlag(new Flag<String>("File names", String.class, sink_)).withFlag(new Flag<String>("User names", String.class, sink_));
  }
  
  @Test
  public void testUsage()
  {
    CommandLineParser clp = new CommandLineParser("testCommand");
    
    assertEquals("Usage: testCommand", clp.getUsage());
    
    clp.withSwitch(new Switch('q', "Quiet", "Quiet mode."));
    
    assertEquals("Usage: testCommand [-q]", clp.getUsage());
    
    Flag<String> flag = new Flag<String>("Keystore file name", String.class, sink_).withName("k").withName("keystore");
    
    clp.withFlag(flag);
    
    assertEquals("Usage: testCommand [-q] [[-k | --keystore] Keystore_file_name]", clp.getUsage());
    
    clp.withSwitch(new Switch('a', "Show All", "Show all."));
    
    assertEquals("Usage: testCommand [-qa] [[-k | --keystore] Keystore_file_name]", clp.getUsage());
    
    flag.withDuplicatesAllowed(true);
    
    assertEquals("Usage: testCommand [-qa] [[-k | --keystore] Keystore_file_name]...", clp.getUsage());
    
    flag.withRequired(true);
    
    assertEquals("Usage: testCommand [-qa] [-k | --keystore] Keystore_file_name...", clp.getUsage());
    
    Flag<String> arg = new Flag<String>("Filename to process", String.class, sink_);
    
    clp.withFlag(arg);
    
    assertEquals("Usage: testCommand [-qa] [-k | --keystore] Keystore_file_name... [Filename_to_process]", clp.getUsage());
    
    arg.withDuplicatesAllowed(true);
    
    assertEquals("Usage: testCommand [-qa] [-k | --keystore] Keystore_file_name... [Filename_to_process]...", clp.getUsage());
    
    arg.withRequired(true);
    
    assertEquals("Usage: testCommand [-qa] [-k | --keystore] Keystore_file_name... Filename_to_process...", clp.getUsage());
  }

  private void assertEquals(String expected, String actual)
  {
    System.out.println("Expected: " + expected);
    System.out.println("Actual  : " + actual);
    System.out.println();
    
    org.junit.Assert.assertEquals(expected, actual);
  }
}
