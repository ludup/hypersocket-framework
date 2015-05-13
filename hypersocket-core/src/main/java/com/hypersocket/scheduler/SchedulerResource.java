package com.hypersocket.scheduler;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.AssignableResource;

@Entity
@Table(name = "scheduler")
public class SchedulerResource extends AssignableResource {

}
