/* 
* openHAB, the open Home Automation Bus.
* Copyright 2010, openHAB.org
*
* See the contributors.txt file in the distribution for a
* full listing of individual contributors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.openhab.core.internal.events;

import static org.openhab.core.events.EventConstants.TOPIC_PREFIX;
import static org.openhab.core.events.EventConstants.TOPIC_SEPERATOR;

import java.util.Dictionary;
import java.util.Hashtable;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.types.Command;
import org.openhab.core.types.EventType;
import org.openhab.core.types.State;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class EventPublisherImpl implements EventPublisher {
		
	private EventAdmin eventAdmin;

	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public void unsetEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = null;
	}

	/* (non-Javadoc)
	 * @see org.openhab.core.internal.events.EventPublisher#sendCommand(org.openhab.core.items.GenericItem, org.openhab.core.datatypes.DataType)
	 */
	public void sendCommand(String itemName, Command command) {
		if(eventAdmin!=null) eventAdmin.sendEvent(createCommandEvent(itemName, command));
	}

	/* (non-Javadoc)
	 * @see org.openhab.core.internal.events.EventPublisher#postCommand(org.openhab.core.items.GenericItem, org.openhab.core.datatypes.DataType)
	 */
	public void postCommand(String itemName, Command command) {
		if(eventAdmin!=null) eventAdmin.postEvent(createCommandEvent(itemName, command));
	}

	/* (non-Javadoc)
	 * @see org.openhab.core.internal.events.EventPublisher#postUpdate(org.openhab.core.items.GenericItem, org.openhab.core.datatypes.DataType)
	 */
	public void postUpdate(String itemName, State newState) {
		if(eventAdmin!=null) eventAdmin.postEvent(createUpdateEvent(itemName, newState));
	}
	
	private Event createUpdateEvent(String itemName, State newState) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("item", itemName);
		properties.put("state", newState);
		return new Event(createTopic(EventType.UPDATE, itemName), properties);
	}

	private Event createCommandEvent(String itemName, Command command) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("item", itemName);
		properties.put("command", command);
		return new Event(createTopic(EventType.COMMAND, itemName) , properties);
	}

	private String createTopic(EventType type, String itemName) {
		return TOPIC_PREFIX + TOPIC_SEPERATOR + type + TOPIC_SEPERATOR + itemName;
	}
}
