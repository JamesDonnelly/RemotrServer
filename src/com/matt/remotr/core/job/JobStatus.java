package com.matt.remotr.core.job;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum JobStatus {
	CREATED, SCHEDULED, EXECUTING, FINISHED, RECOVERING, INERROR, FAILED, UNKNOWN

}
