package org.symphonyoss.symphony.tools.rest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

public class CertificateUtils
{
  private static      JcaX509CertificateConverter   x509Converter_  = new JcaX509CertificateConverter().setProvider("BC");

  public static String encode(Collection<X509Certificate> x509CertificateChain)
  {
    try
    {
      ByteArrayOutputStream bos       = new ByteArrayOutputStream();
      try( JcaPEMWriter          pemWriter = new JcaPEMWriter(new OutputStreamWriter(bos))) {
  
        for(X509Certificate cert : x509CertificateChain)
          pemWriter.writeObject(cert);
      }
  
      return bos.toString(StandardCharsets.UTF_8.name());
    }
    catch(IOException e)
    {
      throw new ProgramFault(e);
    }
  }
  
  public static X509Certificate[]  decode(String certData) throws IOException
  {
    try( PEMParser pemReader = new PEMParser(new StringReader(certData)) )
    {
      List<X509Certificate> result        = new ArrayList<>();
      Object                certificate;
      
      while((certificate = pemReader.readObject()) != null)
      {
        if(certificate instanceof X509Certificate)
          result.add((X509Certificate)certificate);
        else if(certificate instanceof X509CertificateHolder)
        {
          synchronized(x509Converter_)
          {
            result.add(x509Converter_.getCertificate((X509CertificateHolder)certificate));
          }
        }
        else
          throw new ProgramFault("Certificate decode resulted in " + certificate.getClass());

      }
      return result.toArray(new X509Certificate[result.size()]);
    }
    catch(CertificateException e)
    {
      throw new IOException("Failed to decode certificate", e);
    }
  }
}
