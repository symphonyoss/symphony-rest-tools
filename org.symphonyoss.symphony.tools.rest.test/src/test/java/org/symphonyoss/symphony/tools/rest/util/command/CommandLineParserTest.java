/*
 * Copyright 2017 Symphony Communication Services, LLC.
 *
 * All Rights Reserved
 */

package org.symphonyoss.symphony.tools.rest.util.command;

import org.junit.Before;

//import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.symphonyoss.symphony.tools.rest.util.ProgramFault;
import org.symphonyoss.symphony.tools.rest.util.typeutils.ISetter;

public class CommandLineParserTest
{
  private ISetter<String> sink_ = (v) -> v=null;
  
  private Flag flag_ = new Flag("Help me", sink_).withName("h");
  private Switch switch_ = new Switch('h', "Show help");
  private CommandLineParser clp_ = new CommandLineParser("testCommand")
      .withSwitch(switch_)
      .withSwitch(new Switch('q', "Quiet Mode."))
      .withFlag(new Flag("Keystore File Name", sink_).withName("k").withName("keystore"))
      .withFlag(new Flag("FileName", sink_))
      ;
  
  @Test
  public void testArgs0()
  {
    clp_.process(new String[0]);
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
    new CommandLineParser("testCommand").withFlag(new Flag("File names", sink_)).withFlag(new Flag("User names", sink_));
  }
  
  @Test
  public void testUsage()
  {
    CommandLineParser clp = new CommandLineParser("testCommand");
    
    assertEquals("Usage: testCommand", clp.getUsage());
    
    clp.withSwitch(new Switch('q', "Quiet mode."));
    
    assertEquals("Usage: testCommand [-q]", clp.getUsage());
    
    Flag flag = new Flag("Keystore file name", sink_).withName("k").withName("keystore");
    
    clp.withFlag(flag);
    
    assertEquals("Usage: testCommand [-q] [[-k | --keystore] Keystore_file_name]", clp.getUsage());
    
    clp.withSwitch(new Switch('a', "Show all."));
    
    assertEquals("Usage: testCommand [-qa] [[-k | --keystore] Keystore_file_name]", clp.getUsage());
    
    flag.withDuplicatesAllowed(true);
    
    assertEquals("Usage: testCommand [-qa] [[-k | --keystore] Keystore_file_name]...", clp.getUsage());
    
    flag.withRequired(true);
    
    assertEquals("Usage: testCommand [-qa] [-k | --keystore] Keystore_file_name...", clp.getUsage());
    
    Flag arg = new Flag("Filename to process", sink_);
    
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
