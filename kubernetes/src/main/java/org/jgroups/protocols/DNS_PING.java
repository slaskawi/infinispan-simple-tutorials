package org.jgroups.protocols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.jgroups.Address;
import org.jgroups.annotations.Property;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Responses;

public class DNS_PING extends FILE_PING {

   private static final String DEFAULT_DNS_FACTORY = "com.sun.jndi.dns.DnsContextFactory";
   private static final String DEFAULT_DNS_ADDRESS = "10.136.78.18";
   private static final String DEFAULT_DNS_RECORD_TYPE = "A";

   @Property(description = "DNS Context Factory")
   protected String dnsContextFactory = DEFAULT_DNS_FACTORY;

   @Property(description = "DNS Address. This property will be assembed with the 'dns://' prefix")
   protected String dnsAddress = DEFAULT_DNS_ADDRESS;

   @Property(description = "DNS Record type")
   protected String dnsRecordType = DEFAULT_DNS_RECORD_TYPE;


   private InitialDirContext getDnsContext() {
      try {
         Hashtable env = new Hashtable();
         env.put("java.naming.factory.initial", dnsContextFactory);
         env.put("java.naming.provider.url", "dns://" + dnsAddress);
         return new InitialDirContext(env);
      } catch (NamingException e) {
         throw new IllegalStateException("Wrong DNS Context", e);
      }
   }

   @Override
   public boolean isDynamic() {
      return true;
   }

   @Override
   protected void readAll(List<Address> members, String clustername, Responses responses) {
      try {
         Attributes attrs1 = getDnsContext().getAttributes("infinispan-simple-tutori.myproject.svc.cluster.local", new String[]{"A"});
         List<PingData> addresses = getAddresses(attrs1);
         System.out.println("addresses: " + addresses);
         for(PingData data : addresses) {
            if(data == null || (members != null && !members.contains(data.getAddress())))
               continue;
            responses.addResponse(data, false);
            if(local_addr != null && !local_addr.equals(data.getAddress()))
               addDiscoveryResponseToCaches(data.getAddress(), data.getLogicalName(), data.getPhysicalAddr());
         }
      } catch (NamingException e) {
         e.printStackTrace();
      }
   }

   private List<PingData> getAddresses(Attributes attributes) {
      // We are parsing this kind of structure:
      // {a=A: 172.17.0.2, 172.17.0.7}
      // The frst attribute is the type of record. We are not interested in this. Next are addresses.
      try {
         List<PingData> addresses = new ArrayList<>();
         NamingEnumeration<?> namingEnumeration = attributes.get("A").getAll();
         while (namingEnumeration.hasMoreElements()) {
            try {
               IpAddress ipAddress = new IpAddress(namingEnumeration.nextElement().toString());
               PingData pd = new PingData(ipAddress, true);
               addresses.add(pd);
            } catch (Exception e) {
               e.printStackTrace();  // TODO: Customise this generated block
               continue;
            }
         }
         return addresses;
      } catch (NamingException e) {
         e.printStackTrace();
         return Collections.emptyList();
      }
   }
}
