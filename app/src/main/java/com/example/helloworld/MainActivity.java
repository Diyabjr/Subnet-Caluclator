package com.example.helloworld;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText ipAddressInput;
    private EditText subnetMaskInput;
    private Button addNetworkButton;
    private Button calculateButton;
    private LinearLayout networksInputContainer;
    private LinearLayout networksContainer;

    private EditText additionalIpAddressInput;
    private EditText additionalSubnetMaskInput;
    private Button calculateAdditionalButton;

    private List<EditText> networkInputs;
    private LinkedHashMap<String, Integer> networks;
    private LinkedHashMap<String, SubnetCalculator.Subnet> subnets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddressInput = findViewById(R.id.ip_address_input);
        subnetMaskInput = findViewById(R.id.subnet_mask_input);
        addNetworkButton = findViewById(R.id.add_network_button);
        calculateButton = findViewById(R.id.calculate_button);
        networksInputContainer = findViewById(R.id.networks_input_container);
        networksContainer = findViewById(R.id.networks_container);

        additionalIpAddressInput = findViewById(R.id.additional_ip_address_input);
        additionalSubnetMaskInput = findViewById(R.id.additional_subnet_mask_input);
        calculateAdditionalButton = findViewById(R.id.calculate_additional_button);

        networkInputs = new ArrayList<>();
        networks = new LinkedHashMap<>();
        subnets = new LinkedHashMap<>();

        addNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText networkInput = new EditText(MainActivity.this);
                networkInput.setHint("Required hosts for Network " + (networkInputs.size() + 1));
                networksInputContainer.addView(networkInput);
                networkInputs.add(networkInput);
            }
        });

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InetAddress ipAddress = InetAddress.getByName(ipAddressInput.getText().toString());
                    InetAddress subnetMask = InetAddress.getByName(subnetMaskInput.getText().toString());

                    networks.clear();
                    for (int i = 0; i < networkInputs.size(); i++) {
                        int requiredHosts = Integer.parseInt(networkInputs.get(i).getText().toString());
                        networks.put("Network " + (i + 1), requiredHosts);
                    }

                    subnets = SubnetCalculator.calculateVLSM(ipAddress, subnetMask, networks);
                    displaySubnets();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });

        calculateAdditionalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InetAddress ipAddress = InetAddress.getByName(additionalIpAddressInput.getText().toString());
                    InetAddress subnetMask = InetAddress.getByName(additionalSubnetMaskInput.getText().toString());

                    int subnetMaskInt = SubnetCalculator.bytesToInt(subnetMask.getAddress());
                    int availableHosts = ~subnetMaskInt - 1;

                    SubnetCalculator.Subnet subnet = SubnetCalculator.calculateSubnet(ipAddress, subnetMask, availableHosts);

                    Toast.makeText(MainActivity.this,
                            "Network address: " + subnet.networkAddress.getHostAddress() + "\n" +
                                    "Subnet mask: " + subnet.newSubnetMask.getHostAddress() + "\n" +
                                    "Broadcast address: " + subnet.broadcastAddress.getHostAddress() + "\n" +
                                    "Available hosts: " + subnet.availableHosts,
                            Toast.LENGTH_LONG).show();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void displaySubnets() {
        networksContainer.removeAllViews();
        for (Map.Entry<String, SubnetCalculator.Subnet> entry : subnets.entrySet()) {
            String networkName = entry.getKey();
            SubnetCalculator.Subnet subnet = entry.getValue();

            TextView networkTitle = new TextView(MainActivity.this);
            networkTitle.setText(networkName);
            networksContainer.addView(networkTitle);

            TextView networkAddress = new TextView(MainActivity.this);
            networkAddress.setText("Network address: " + subnet.networkAddress.getHostAddress());
            networksContainer.addView(networkAddress);

            TextView newSubnetMask = new TextView(MainActivity.this);
            newSubnetMask.setText("New Subnet mask: " + subnet.newSubnetMask.getHostAddress());
            networksContainer.addView(newSubnetMask);

            TextView broadcastAddress = new TextView(MainActivity.this);
            broadcastAddress.setText("Broadcast address: " + subnet.broadcastAddress.getHostAddress());
            networksContainer.addView(broadcastAddress);

            TextView availableHosts = new TextView(MainActivity.this);
            availableHosts.setText("Available hosts: " + subnet.availableHosts);
            networksContainer.addView(availableHosts);

            TextView usedHosts = new TextView(MainActivity.this);
            usedHosts.setText("Used hosts: " + networks.get(networkName));
            networksContainer.addView(usedHosts);

            TextView separator = new TextView(MainActivity.this);
            separator.setText("---------------------------------------------------");
            networksContainer.addView(separator);
        }
    }
}




