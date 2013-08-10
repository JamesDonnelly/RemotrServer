package com.matt.remotr.core.job;

import org.quartz.JobDetail;
import org.quartz.Trigger;

class DetailTriggerHolder {
	
	private Trigger trigger;
	private JobDetail jobDetail;
	
	public DetailTriggerHolder(JobDetail jobDetail, Trigger trigger){
		this.jobDetail = jobDetail;
		this.trigger = trigger;
	}
	
	public Trigger getTrigger() {
		return trigger;
	}
	public JobDetail getJobDetail() {
		return jobDetail;
	}
	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}
	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}
}
