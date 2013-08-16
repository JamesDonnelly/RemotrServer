package com.matt.remotr.core.event;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum EventType {
	BROADCAST, JOB, PING, MESSAGE
}
