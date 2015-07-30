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

package com.xuvasi.bacnetgateway.out;

import java.net.HttpURLConnection;
import java.net.URL;

public class HttpPost
{
	private String endPoint = "";
	private String method = "";
	private String params = "";
	
	public HttpPost(String ep, String m, String p)
	{
		this.endPoint = ep;
		this.method = m;
		this.params = p;
		
	}
	
	public boolean submit()
	{
		try
		{
			URL url = new URL(this.endPoint + this.method);

			// Convert JSON params string to byte array to be sent
			byte[] postDataBytes = this.params.getBytes("UTF-8");
			
			// Try and open and use the URL connection
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setDoOutput(true);
			
			// Write the POST data
			conn.getOutputStream().write(postDataBytes);
			
			// Check the response
			if (conn.getResponseCode() != 200)
			{
				// if we failed, return false
				System.out.println("Submission Failed!");
				return false;
			}
			
			// if all is well
			//System.out.println("Submitted!");
			return true;
			
		}
		catch (Exception e)
		{
			System.out.println("Submission Error! " + e.getMessage());
			return false;
		
		} // try-catch
	
	} // submit()

} // class HttpPost
