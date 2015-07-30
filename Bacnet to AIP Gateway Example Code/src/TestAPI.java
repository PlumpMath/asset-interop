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

//import com.xuvasi.bacnetgateway.Sensor;
import com.xuvasi.bacnetgateway.out.AuthdHttpPost;
//import com.xuvasi.bacnetgateway.out.HttpPost;

public class TestAPI
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		StringBuilder jsonString = new StringBuilder();
		jsonString.append("{\"item-metadata\":[{\"rel\":\"urn:X-tsbiot:rels:isContentType\",\"val\":\"application/vnd.tsbiot.assetalert+json\"},{\"rel\":\"urn:X-tsbiot:rels:hasDescription:en\",\"val\":\"Asset Alert.\"},{\"rel\":\"urn:X-tsbiot:rels:supportsSearch\",\"val\":\"urn:X-tsbiot:search:simple\"}],\"items\":[{\"site-ref\":\"@site\",\"asset-id\":\"TEST\",\"asset-type\":\"WATER\",\"asset-details\":[{\"manufacturer\":\"Test Manufacturer\"},{\"model\":\"Test Model\"},{\"serial-number\":\"594983784\"}],\"asset-status\":\"NORMAL\",\"href\":\"http://localhost\",\"sensors\":[");

		// Holder for sensor details
		StringBuilder sensorDetails = new StringBuilder();
		
		// Set at least one sensor
		sensorDetails.append("{\"type\":\"testSensor\",\"details\":[{\"value\":\"42\"},{\"units\":\"millennia\"}]}"); 
		
		jsonString.append(sensorDetails.toString());
		jsonString.append("]}]}");
		
		System.out.println(jsonString.toString());
		
		//String oldUrl = "http://192.168.0.102:3000/api/";
		String url = "http://dev.xuvasi.com/api/";
		//String oldMethod = "distechUpdate";
		String method = "events";
		String userID = "test@xuvasi.com";
		String password = "qazwsx";
		
		// HttpPost poster = new HttpPost(oldUrl, oldMethod, jsonString.toString());
		AuthdHttpPost poster = new AuthdHttpPost(url, method, jsonString.toString(), userID, password);
		poster.submit();

	} // main()

} // class TestAPI
