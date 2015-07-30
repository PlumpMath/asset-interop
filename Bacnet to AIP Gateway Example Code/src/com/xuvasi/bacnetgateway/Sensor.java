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

package com.xuvasi.bacnetgateway;

public class Sensor
{
	private String description = "";
	private double presentValue = 0.0;
	private String units = "";
	private String name = "";
	private String parent = "";
	
	public Sensor()
	{
		// Empty Constructor
	
	} // Default constructor
	
	public Sensor (String d, double pv, String u, String n, String p)
	{
		this.description = d;
		this.presentValue = pv;
		this.units = u;
		this.name = n;
		this.parent = p;
	
	} // Populating constructor

	public void setDescription(String d)
	{
		this.description = d;
	
	} // setDescription()
	
	public String getDescription()
	{
		return this.description;
	
	} // getDescription()
	
	public void setPresentValue(double pv)
	{
		this.presentValue = pv;
	
	} // setPresentValue()
	
	public double getPresentValue()
	{
		return this.presentValue;
	
	} // getPresentValue()

	public void setUnits(String u)
	{
		this.units = u;
	
	} // setUnits()
	
	public String getUnits()
	{
		return this.units;
	
	} // getUnits()

	public void setName(String n)
	{
		this.name = n;
	
	} // setName()
	
	public String getName()
	{
		return this.name;
	
	} // setName()
	
	public void setParent(String p)
	{
		this.parent = p;
	
	} // setParent()
	
	public String getParent()
	{
		return this.parent;
	
	} // setParent()
	
	public void createParentFromName()
	{
		if (this.description.length() > 0)
		{
			String trimmedDescription = this.description.replaceAll(" ", "");
			String stem = "Drivers.BcpBacnetNetwork.";
			int parentStart = this.name.indexOf(stem) + stem.length();
			int parentEnd = this.name.indexOf(".points." + trimmedDescription);
		
			this.parent = this.name.substring(parentStart, parentEnd);
		}
		else
		{
			this.parent = "";
		}
	
	} // createParentFromName()
	
	public String toString()
	{
		return "Sensor - Description: " + this.description + " | Present Value: " + this.presentValue + " | Units: " + this.units + " | Name: " + this.name + " (Parent Device = " + this.parent + ")";
	}

} // class Sensor
