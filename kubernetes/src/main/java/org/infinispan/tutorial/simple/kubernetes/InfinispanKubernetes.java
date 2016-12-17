package org.infinispan.tutorial.simple.kubernetes;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class InfinispanKubernetes extends ReceiverAdapter {

   private static final String CLUSTER_NAME = "CLUSTER_NAME";

   public static void main(String[] args) throws Exception {

      InputStream configuration = InfinispanKubernetes.class.getResourceAsStream("/fast.xml");
      JChannel channel = new JChannel(configuration);

      channel.connect(CLUSTER_NAME);

      TimeUnit.MINUTES.sleep(10);

      channel.close();
   }

   @Override
   public void viewAccepted(View view) {
      System.out.println("View: " + view);
   }
}
