package com.example.helloworld;

import android.os.Build;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SubnetCalculator {

    public static LinkedHashMap<String, Subnet> calculateVLSM(InetAddress ipAddress, InetAddress subnetMask, LinkedHashMap<String, Integer> networks) throws UnknownHostException {
        // Sort the networks by required hosts in descending order
        LinkedHashMap<String, Integer> sortedNetworks = new LinkedHashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networks.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEachOrdered(entry -> sortedNetworks.put(entry.getKey(), entry.getValue()));
        }

        // Calculate the subnets for each network
        LinkedHashMap<String, Subnet> calculatedSubnets = new LinkedHashMap<>();
        InetAddress currentIpAddress = ipAddress;
        for (Map.Entry<String, Integer> entry : sortedNetworks.entrySet()) {
            Subnet subnet = calculateSubnet(currentIpAddress, subnetMask, entry.getValue());
            calculatedSubnets.put(entry.getKey(), subnet);

            // Update the current IP address for the next network
            currentIpAddress = InetAddress.getByAddress(intToBytes(bytesToInt(subnet.broadcastAddress.getAddress()) + 1));
        }

        return calculatedSubnets;
    }

    public static Subnet calculateSubnet(InetAddress ipAddress, InetAddress subnetMask, int requiredHosts) throws UnknownHostException {
        byte[] ipBytes = ipAddress.getAddress();
        byte[] subnetBytes = subnetMask.getAddress();

        // Calculate the network address
        byte[] networkBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            networkBytes[i] = (byte) (ipBytes[i] & subnetBytes[i]);
        }
        InetAddress networkAddress = InetAddress.getByAddress(networkBytes);

        // Calculate the new subnet mask based on the required hosts
        int newMaskBits = 32 - (int) Math.ceil(Math.log(requiredHosts + 2) / Math.log(2));
        int newSubnetMask = -1 << (32 - newMaskBits);
        InetAddress newSubnetMaskAddress = InetAddress.getByAddress(intToBytes(newSubnetMask));

        // Calculate the broadcast address
        int broadcastInt = bytesToInt(networkBytes) | ~newSubnetMask;
        InetAddress broadcastAddress = InetAddress.getByAddress(intToBytes(broadcastInt));

        // Calculate the number of available hosts
        int availableHosts = (int) Math.pow(2, 32 - newMaskBits) - 2;

        return new Subnet(networkAddress, newSubnetMaskAddress, broadcastAddress, availableHosts);
    }

    public static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }

    public static class Subnet {
        InetAddress networkAddress;
        InetAddress newSubnetMask;
        InetAddress broadcastAddress;
        int availableHosts;

        public Subnet(InetAddress networkAddress, InetAddress newSubnetMask, InetAddress broadcastAddress, int availableHosts) {
            this.networkAddress = networkAddress;
            this.newSubnetMask = newSubnetMask;
            this.broadcastAddress = broadcastAddress;
            this.availableHosts = availableHosts;
        }
    }
}
