package com.SystemMoniter.Test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.SystemMonitor.Indexer.LogIndexer;
import com.SystemMonitor.Model.JobInformation;

public class logIndexerTest {

	private LogIndexer _sut;
	private String logFilePath = "/home/web-dev/Documents/eclipse-javaee/SystemMonitorAnalyzer/logs";
	private String logIndexPath = "/home/web-dev/Documents/eclipse-javaee/SystemMonitorAnalyzer/logIndex";
	
	@Before
	public void setUp() throws Exception {
		_sut = new LogIndexer(logFilePath, logIndexPath);
	}

	@Test
	public void testWorkflow() {
		List<JobInformation> jobs = _sut.searchWorkflow("DnM");
		
		for (JobInformation job : jobs)
			System.out.println(job.getJobName());
	}
}
