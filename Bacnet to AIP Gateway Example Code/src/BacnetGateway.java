/*Copyright 2015, Xuvasi Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.

You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and
limitations under the License.

Author: David Prior, Founder & CTO Xuvasi Ltd
Contact: T - +44 (0) 7811 359 792 | E - david@xuvasi.com
Original Development: 2014
*/

import java.util.ArrayList;
import java.util.List;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import com.xuvasi.bacnetgateway.Sensor;
import com.xuvasi.bacnetgateway.out.AuthdHttpPost;
//import com.xuvasi.bacnetgateway.out.HttpPost;

public class BacnetGateway
{
    static LocalDevice localDevice;
    
    //NB: The parameters for this code are all set in the code at this time.
    // A properties file should be used for production purposes - ideally one that uses an Array loader to allow more than one BACnet device to be
    // read on the network and interrogated for sensors and attributes. You have been told!
    private static final int BOX_39_02 = 89024;
    private static final String BROADCAST_NETWORK = "192.168.1.255";
    private static final int DEFAULT_PORT = 0xBAC0; // == 47808

    public static void main(String[] args) throws Exception
    {
    	// Open our network and create the transport to be used
        IpNetwork network = new IpNetwork(BROADCAST_NETWORK, DEFAULT_PORT);
        Transport transport = new Transport(network);

        // Set ourselves up as a local endpoint
        localDevice = new LocalDevice(1234, transport);
        
        // try and initialise our presence on the network
        try
        {
            localDevice.initialize();
            localDevice.getEventHandler().addListener(new Listener());
            
            // Loop pending interrupt to keep testing the network for reports every 7 seconds
            // In an ideal world, we would simply re-poll a previously discovered device but we don't have time at the moment!!
            while (true)
            {
            	localDevice.sendGlobalBroadcast(new WhoIsRequest());
            	Thread.sleep(7000);
            }
        
        } // try-catch
        
        finally
        {
            localDevice.terminate();
        
        } // finally
    
    } // main()

    static class Listener extends DeviceEventAdapter
    {
        @Override
        public void iAmReceived(RemoteDevice d)
        {
        	
        	// if this is the (or 'one of the'!) BACnet device(s) we are looking for
        	// this code needs adapting to test where d.getInstanceNumber() is contained in the array of devices we want to interrogate - see above comment!
        	if (d.getInstanceNumber() == BOX_39_02)
        	{
	            try
	            {
	                d.setSegmentationSupported(Segmentation.noSegmentation);

	                RequestUtils.getExtendedDeviceInformation(localDevice, d);

	                @SuppressWarnings("unchecked")
					List<ObjectIdentifier> oids = ((SequenceOf<ObjectIdentifier>) RequestUtils.sendReadPropertyAllowNull(
	                        localDevice, d, d.getObjectIdentifier(), PropertyIdentifier.objectList)).getValues();

	                PropertyReferences refs = new PropertyReferences();
	                // add the property references of the "device object" to the list
	                refs.add(d.getObjectIdentifier(), PropertyIdentifier.all);

	                // and now from all objects under the device object >> ai0, ai1,bi0,bi1...
	                for (ObjectIdentifier oid : oids) {
	                    refs.add(oid, PropertyIdentifier.all);
	                }

	                // Get all the property values from the Bacnet device we are interrogating
	                PropertyValues pvs = RequestUtils.readProperties(localDevice, d, refs, null);
	                
	                // Holder array to track all sensor readings from the device
	                ArrayList<Sensor> sensors = new ArrayList<Sensor>();

	                // Holder array to track all parents for the sensors
	                ArrayList<String> devices = new ArrayList<String>();
	                
	                // For every reported object, including the device itself!
	                for (ObjectIdentifier oid : oids)
	                {
            			Sensor s = new Sensor();
            			
	                	// Disregard the Device Record - that's the, er, device
            			// Remember to adapt this if switching to an array of devices to listen to...
	                	if (!oid.toString().equals("Device " + BOX_39_02))
	                	{
                        	// For every reading from this device
	                		for (ObjectPropertyReference opr : pvs)
	                        {
	                			
	                            if (oid.equals(opr.getObjectIdentifier()))
	                            {
	                            	String property = opr.getPropertyIdentifier().toString();
	                            	
	                            	if (property.equals("Description"))
	                            	{
	                            		s.setDescription(pvs.getNoErrorCheck(opr).toString());
	                            	}
	                            	
	                            	if (property.equals("Present value"))
	                            	{
	                            		s.setPresentValue(new Double(pvs.getNoErrorCheck(opr).toString()).doubleValue());
	                            	}

	                            	if (property.equals("Units"))
	                            	{
	                            		s.setUnits(pvs.getNoErrorCheck(opr).toString());
	                            	}

	                            	if (property.equals("Object name"))
	                            	{
	                            		s.setName(pvs.getNoErrorCheck(opr).toString());
	                            	}

	                            } // if oid = PropertyIdentifier
	                            	                            
	                        } // for ObjectPropertyReference
	                	
	                        // Create a parent identifier for this sensor, then add it to the tracker
	                        s.createParentFromName();
	                        sensors.add(s);
	                        
	                        // Add or Update the parent tracker to reflect the new sensor's parent
	                        if (!devices.contains(s.getParent()))
	                        {
	                        	devices.add(s.getParent());
	                        
	                        } // if this parent is not already tracked
		                	
	                	} // if it's not the Box3902
	                
	                } // for every reported object

	                // You can uncomment the block below (to '}') in order to see what is being detected and interrogated
	                //System.out.println("Remote devices done...");
	                //System.out.println("Devices: " + devices.toString());
	                //System.out.println("Sensors: ");
	                //for (Sensor s : sensors)
	                //{
	                //	System.out.println("  " + s.toString());
	                //
	                //}
	                
	                // Submit the updates to the AM endpoint
	                postUpdates(sensors, devices);
	                
	            }
	            catch (BACnetException e)
	            {
	                e.printStackTrace();
	            }
	            
        	} // if it is the Box3902
        	
        } // iAmReceived() override
        
    	public void postUpdates(ArrayList<Sensor> sensors, ArrayList<String> devices)
    	{
    		
    		for (String device : devices)
    		{
	    		StringBuilder jsonString = new StringBuilder();
	    		jsonString.append("{\"item-metadata\":[{\"rel\":\"urn:X-tsbiot:rels:isContentType\",\"val\":\"application/vnd.tsbiot.assetalert+json\"},{\"rel\":\"urn:X-tsbiot:rels:hasDescription:en\",\"val\":\"Asset Alert from Intel Gateway.\"},{\"rel\":\"urn:X-tsbiot:rels:supportsSearch\",\"val\":\"urn:X-tsbiot:search:simple\"}],\"items\":[{\"site-ref\":\"@site\",\"asset-id\":\"" + device + "\",\"asset-type\":\"VAV\",\"asset-details\":[{\"manufacturer\":\"Manufacturer\"},{\"model\":\"Model\"},{\"serial-number\":\"594983784\"}],\"asset-status\":\"NORMAL\",\"href\":\"http://xuvasi.com\",\"sensors\":[");

	    		// Holder for sensor details
	    		StringBuilder sensorDetails = new StringBuilder();
	    		
	    		// Loop through sensors and check if it is parented to this device
	    		// Really piss-poor process but, hell, we're in a rush here!!
	    		for (Sensor s : sensors)
	    		{
	    			//System.out.println(s.getName());
	    			if (s.getParent().equals(device))
	    			{
			    		if (sensorDetails.length() != 0) sensorDetails.append(",");
			    		sensorDetails.append("{\"type\":\"" + s.getName() + "\",\"details\":[{\"value\":\"" + s.getPresentValue() + "\"},{\"units\":\"" + s.getUnits() + "\"}]}"); 
	    			
	    			} // If this sensor reading belongs to the current device
	    		
	    		} // For every sensor report we have
	    		
	    		jsonString.append(sensorDetails.toString());
	    		jsonString.append("]}]}");
	    		
	    		//System.out.println(jsonString.toString());
	    		
	    		// Obviously this is all void now, hence the need to switch to a properties file.
	    		String url = "http://dev.xuvasi.com/api/";
	    		String method = "events";
	    		String userID = "test@xuvasi.com";
	    		String password = "qazwsx";
	    		
	    		// First commented line creates an unauthenticated POSTing construct; second uncommented line creates a basic authenticated POSTing construct 
	    		// HttpPost poster = new HttpPost(oldUrl, oldMethod, jsonString.toString());
	    		AuthdHttpPost poster = new AuthdHttpPost(url, method, jsonString.toString(), userID, password);
	    		
	    		// Using the relevant POSTing construct, submit the update(s) to AM
	    		poster.submit();
	    		
    		} // Loop through devices

    	} // postUpdate
    
    } // inner class Listener

} // class BacnetGateway
