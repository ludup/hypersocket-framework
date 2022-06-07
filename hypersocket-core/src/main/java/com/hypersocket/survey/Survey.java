package com.hypersocket.survey;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.NotScheduledException;
import com.hypersocket.scheduler.SchedulerResource;
import com.hypersocket.scheduler.SchedulerService;

public class Survey implements Closeable {
	static Logger LOG = LoggerFactory.getLogger(Survey.class);

	static long parseTime(String fulltime) {
		long ms = 0;
		for (String time : fulltime.split("\\s+")) {
			if (time.endsWith("h")) {
				ms += TimeUnit.HOURS.toMillis(Long.parseLong(time.substring(0, time.length() - 1)));
			} else if (time.endsWith("d")) {
				ms += TimeUnit.DAYS.toMillis(Long.parseLong(time.substring(0, time.length() - 1)));
			} else if (time.endsWith("m")) {
				ms += TimeUnit.MINUTES.toMillis(Long.parseLong(time.substring(0, time.length() - 1)));
			} else if (time.endsWith("s")) {
				ms += TimeUnit.SECONDS.toMillis(Long.parseLong(time.substring(0, time.length() - 1)));
			} else if (time.endsWith("M")) {
				ms += 30 * TimeUnit.DAYS.toMillis(Long.parseLong(time.substring(0, time.length() - 1)));
			} else if (time.endsWith("W")) {
				ms += 7 * TimeUnit.DAYS.toMillis(Long.parseLong(time.substring(0, time.length() - 1)));
			} else
				ms += Long.parseLong(time);
		}
		return ms;
	}

	public interface Trigger extends Closeable {
		String getResourceKey();

		void schedule();

		boolean isScheduled();
	}

	public static class AfterInstallTrigger implements Trigger {

		private long delay;
		private long repeat;
		private Survey survey;

		AfterInstallTrigger(Survey survey, JsonObject obj) {
			this.survey = survey;
			delay = parseTime(obj.get("delay").getAsString());
			repeat = parseTime(obj.get("repeat").getAsString());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (delay ^ (delay >>> 32));
			result = prime * result + (int) (repeat ^ (repeat >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AfterInstallTrigger other = (AfterInstallTrigger) obj;
			if (delay != other.delay)
				return false;
			if (repeat != other.repeat)
				return false;
			return true;
		}

		@Override
		public void close() throws IOException {
			unschedule();

		}

		protected void unschedule() {
			try {
				survey.scheduler.deleteResource(getCurrentJob());
			} catch (Exception e) {
			}
		}

		protected SchedulerResource getCurrentJob()
				throws SchedulerException, NotScheduledException, AccessDeniedException {
			return survey.scheduler.getResourceById(getResourceKey());
		}

		@Override
		public String getResourceKey() {
			return survey.resourceKey + getClass().getSimpleName();
		}

		@Override
		public void schedule() {
			JobDataMap jdm = new JobDataMap();
			jdm.put(SurveyTriggerJob.SURVEY_RESOURCE_KEY, survey.resourceKey);
			try {

				/*
				 * Installation date comes from the system realm creation date. If this was more
				 * than 'delay' ago, then we use repeat
				 */
				Date installedDate = survey.realmService.getSystemRealm().getCreateDate();
				Date firstTriggerDate = new Date(installedDate.getTime() + delay);
				Date now = new Date();

				if (now.after(firstTriggerDate)) {
					/*
					 * Past first trigger. If there is an existing job, leave it alone if it's a
					 * repeating job.
					 */
					try {
						SchedulerResource job = getCurrentJob();
						if (job.getNextFire() == null)
							throw new NotScheduledException();
					} catch (NotScheduledException nse) {
						/* Otherwise schedule as repeating */
						survey.scheduler.scheduleAt(SurveyTriggerJob.class, getResourceKey(), jdm, new Date(now.getTime() + repeat), repeat,
								SimpleTrigger.REPEAT_INDEFINITELY);
					}
				} else {
					/*
					 * Before first trigger date, schedule for exactly then (remove any existing
					 * schedule, even if its the same)
					 */
					unschedule();

					survey.scheduler.scheduleAt(SurveyTriggerJob.class, getResourceKey(), jdm, firstTriggerDate, repeat,
							SimpleTrigger.REPEAT_INDEFINITELY);
				}
			} catch (SchedulerException | AccessDeniedException e) {
				throw new IllegalStateException("Failed to schedule survey trigger.", e);
			}
		}

		@Override
		public boolean isScheduled() {
			try {
				survey.scheduler.getResourceById(getResourceKey());
				return true;
			} catch (NotScheduledException nse) {
				return false;
			} catch (SchedulerException | AccessDeniedException e) {
				throw new IllegalStateException("Failed to check if scheduled.", e);
			}

		}
	}

	private String resourceKey;
	private long serial = 0;
	private long popupDelay = 10000;
	private Map<String, List<Trigger>> triggers = new HashMap<>();
	private SchedulerService scheduler;
	private RealmService realmService;
	
	public Survey(String resourceKey, JsonObject obj, RealmService realmService, SchedulerService scheduler) {
		this.resourceKey = resourceKey;
		this.scheduler = scheduler;
		this.realmService = realmService;
		popupDelay = obj.has("popupDelay") ? parseTime(obj.get("popupDelay").getAsString()) : 10000;
		serial = obj.get("serial").getAsLong();
		for (Entry<String, JsonElement> licenseStateElEn : obj.get("licenseState").getAsJsonObject().entrySet()) {
			List<Trigger> triggers = new ArrayList<>();
			JsonObject stateObject = licenseStateElEn.getValue().getAsJsonObject();
			for (JsonElement triggersElEn : stateObject.get("triggers").getAsJsonArray()) {
				JsonObject triggersObj = triggersElEn.getAsJsonObject();
				switch (triggersObj.get("type").getAsString()) {
				case "AFTER_INSTALL":
					triggers.add(new AfterInstallTrigger(this, triggersObj));
					break;
				}
			}
			this.triggers.put(licenseStateElEn.getKey(), triggers);
		}
	}

	public long getPopupDelay() {
		return popupDelay;
	}

	public long getSerial() {
		return serial;
	}

	@Override
	public void close() throws IOException {
		for (List<Trigger> l : triggers.values())
			for (Trigger t : l)
				t.close();

	}

	public String getResourceKey() {
		return resourceKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceKey == null) ? 0 : resourceKey.hashCode());
		result = prime * result + (int) (serial ^ (serial >>> 32));
		result = prime * result + ((triggers == null) ? 0 : triggers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Survey other = (Survey) obj;
		if (resourceKey == null) {
			if (other.resourceKey != null)
				return false;
		} else if (!resourceKey.equals(other.resourceKey))
			return false;
		if (serial != other.serial)
			return false;
		if (triggers == null) {
			if (other.triggers != null)
				return false;
		} else if (!triggers.equals(other.triggers))
			return false;
		return true;
	}

	public void schedule(String licenseState) {
		if (triggers.containsKey(licenseState)) {
			LOG.info("Scheduling survey {} under license state {}", getResourceKey(), licenseState);
			for (Trigger t : triggers.get(licenseState)) {
				t.schedule();
			}
		}

	}

	public boolean isScheduled() {
		for (List<Trigger> l : triggers.values())
			for (Trigger t : l)
				if (t.isScheduled())
					return true;
		return false;
	}

	public List<Trigger> getTriggers(String phase) {
		List<Trigger> l = triggers.get(phase);
		return l == null ? Collections.emptyList() : l;
	}

}
