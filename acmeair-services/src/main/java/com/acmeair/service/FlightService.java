/*******************************************************************************
* Copyright (c) 2013-2015 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.acmeair.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.acmeair.entities.Flight;
import com.acmeair.entities.FlightSegment;
import com.acmeair.entities.FlightPK;
import com.acmeair.entities.AirportCodeMapping;

public abstract class FlightService {

	//TODO:need to find a way to invalidate these maps
	protected static ConcurrentHashMap<String, FlightSegment> originAndDestPortToSegmentCache = new ConcurrentHashMap<String,FlightSegment>();
	protected static ConcurrentHashMap<String, List<Flight>> flightSegmentAndDataToFlightCache = new ConcurrentHashMap<String,List<Flight>>();
	protected static ConcurrentHashMap<FlightPK, Flight> flightPKtoFlightCache = new ConcurrentHashMap<FlightPK, Flight>();
	
	
	public abstract Flight getFlightByFlightKey(FlightPK key);
	

	public List<Flight> getFlightByAirportsAndDepartureDate(String fromAirport,	String toAirport, Date deptDate) {
		try {
			String originPortAndDestPortQueryString= fromAirport+toAirport;
			FlightSegment segment = originAndDestPortToSegmentCache.get(originPortAndDestPortQueryString);
			
			if (segment == null) {
				segment = getFlightSegment(fromAirport, toAirport);
				originAndDestPortToSegmentCache.putIfAbsent(originPortAndDestPortQueryString, segment);
			}
			
			// cache flights that not available (checks against sentinel value above indirectly)
			if (segment.getFlightName() == null) {
				return new ArrayList<Flight>(); 
			}
			
			String segId = segment.getFlightName();
			String flightSegmentIdAndScheduledDepartureTimeQueryString = segId + deptDate.toString();
			List<Flight> flights = flightSegmentAndDataToFlightCache.get(flightSegmentIdAndScheduledDepartureTimeQueryString);
			
			if (flights == null) {				
				flights = getFlightBySegment(segment, deptDate);
				flightSegmentAndDataToFlightCache.putIfAbsent(flightSegmentIdAndScheduledDepartureTimeQueryString, flights);
			}
			
			return flights;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// NOTE:  This is not cached
	public List<Flight> getFlightByAirports(String fromAirport, String toAirport) {
			FlightSegment segment = getFlightSegment(fromAirport, toAirport);
			if (segment == null) {
				return new ArrayList<Flight>(); 
			}	
			return getFlightBySegment(segment, null);
	}
	
	protected abstract FlightSegment getFlightSegment(String fromAirport, String toAirport);
	
	protected abstract List<Flight> getFlightBySegment(FlightSegment segment, Date deptDate);  
			
	public abstract void storeAirportMapping(AirportCodeMapping mapping);

	public abstract AirportCodeMapping createAirportCodeMapping(String airportCode, String airportName);
	
	public abstract Flight createNewFlight(String flightSegmentId,
			Date scheduledDepartureTime, Date scheduledArrivalTime,
			BigDecimal firstClassBaseCost, BigDecimal economyClassBaseCost,
			int numFirstClassSeats, int numEconomyClassSeats,
			String airplaneTypeId);

	public abstract void storeFlightSegment(FlightSegment flightSeg);
	
	public abstract void storeFlightSegment(String flightName, String origPort, String destPort, int miles);
	
	public abstract Long countFlightSegments();
	
	public abstract Long countFlights();
	
	public abstract Long countAirports();
	
}